package com.pc1.backendrupay.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pc1.backendrupay.configs.JwtService;
import com.pc1.backendrupay.enums.TypeUser;
import com.pc1.backendrupay.exceptions.InvalidTokenException;
import com.pc1.backendrupay.exceptions.RegistrationInUseException;
import com.pc1.backendrupay.exceptions.UserNotFoundException;
import com.pc1.backendrupay.repositories.UserRepository;
import com.pc1.backendrupay.services.EmailService;
import com.pc1.backendrupay.token.Token;
import com.pc1.backendrupay.token.TokenRepository;
import com.pc1.backendrupay.token.TokenType;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pc1.backendrupay.domain.UserModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Autowired
    private JavaMailSender mailSender;

    public AuthenticationResponse register(RegisterRequest request) throws RegistrationInUseException{
        checkRegistration(request.getRegistration());

        TypeUser typeuser = switch (request.getTypeUser()) {
            case "STUDENT" -> TypeUser.STUDENT;
            case "ADMIN" -> TypeUser.ADMIN;
            case "SCHOLARSHIP_STUDENT" -> TypeUser.SCHOLARSHIP_STUDENT;

            default -> TypeUser.EXTERNAL;
        };

        var user = UserModel.builder()
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .typeUser(typeuser)
                .registration(request.getRegistration())
                .creationDate(LocalDateTime.now())
                .build();

        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new IllegalStateException(e);
        }
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)

                .build();
    }

    public boolean resetPassword(String email) throws UserNotFoundException{
        var user = repository.findByEmail(email)
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(user, jwtToken);

        //mailSender.send(emailService.constructResetTokenEmail("http://localhost:8081", jwtToken, email));
        try {
            emailService.constructResetTokenEmail("http://localhost:8081", jwtToken, email);
        } catch (FileNotFoundException | MessagingException e) {
            return false;
        }

        return true;
    }

    public void changePassword(String token, String password) throws UserNotFoundException, InvalidTokenException{
        var email = jwtService.extractUsername(token.toString());
        if (email == null) throw new UserNotFoundException("User not found");

        var user = repository.findByEmail(email)
                .orElseThrow();
        var jwt = tokenRepository.findByToken(token) 
                    .orElse(null);

        if(jwt != null && !jwt.isRevoked() && jwtService.isTokenValid(token, user)) {
            user.setPassword(passwordEncoder.encode(password));
            revokeAllUserTokens(user);
            repository.save(user);
        }
        else throw new InvalidTokenException("Invalid token");
    }

    private void saveUserToken(UserModel user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(UserModel user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String email;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        email = jwtService.extractUsername(refreshToken);
        if (email != null) {
            var user = this.repository.findByEmail(email)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }

    private void checkRegistration(String registration) throws RegistrationInUseException{
        for (UserModel user : repository.findAll()) {
            if (!user.getRegistration().equals("") && user.getRegistration().replaceAll("\\s", "").equals(registration.replaceAll("\\s", ""))) {
                throw new RegistrationInUseException("Registration already in use");
            }
        }
    }
}
