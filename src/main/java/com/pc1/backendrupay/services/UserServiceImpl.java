package com.pc1.backendrupay.services;

import com.pc1.backendrupay.domain.TicketModel;
import com.pc1.backendrupay.domain.UserDTO;
import com.pc1.backendrupay.domain.UserModel;
import com.pc1.backendrupay.enums.TypeUser;
import com.pc1.backendrupay.repositories.UserRepository;
import com.pc1.backendrupay.exceptions.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class represents the service layer for managing user-related operations.
 */
@Service
public class UserServiceImpl  implements UserService{
    @Autowired
    final private UserRepository userRepository;

    /**
     * Constructs a new UserService with the given UserRepository.
     * @param userRepository the repository for user data access
     */

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all users.
     * @return a list of UserModel objects representing the users
     */
    @Override
    public Optional<UserModel> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public UserModel getUserId(UUID id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
    }

    @Override
    public List<UserModel> listUsers(TypeUser typeUser, String registration) {
        if (typeUser != null && registration != null) {
            return userRepository.findByTypeUserAndRegistration(typeUser, registration);
        } else if (typeUser != null) {
            return userRepository.findByTypeUser(typeUser);
        } else if (registration != null) {
            return userRepository.findByRegistration(registration);
        } else {
            return userRepository.findAll();
        }
    }
    /**
     * Retrieves a user by their ID.
     * @param id the ID of the user to retrieve
     * @return an Optional containing the UserModel if found, or an empty Optional if not found
     */
    @Override
    public Optional<UserModel> listUserById(UUID id) {
        return userRepository.findById(id);
    }


    /**
     * Edits an existing user.
     * @param id the ID of the user to edit
     * @param userDTO the DTO object containing the updated user data
     * @return the updated UserModel object
     * @throws UserNotFoundException if the user with the given ID is not found
     */
    @Override
    public UserModel editUser(UUID id, UserDTO userDTO) throws UserNotFoundException {
        checkUser(id);

        UserModel user = userRepository.findById(id).get();
        user.setName(userDTO.name());
        user.setEmail(userDTO.email());
        user.setPassword(userDTO.password());
        user.setTypeUser(userDTO.typeUser());
        user.setRegistration(userDTO.registration());

        return userRepository.save(user);
    }

    /**
     * Deletes aLong user by their ID.
     * @param id the ID of the user to delete
     * @throws UserNotFoundException if the user with the given ID is not found
     */
    @Override
    public void deleteUser(UUID id) throws UserNotFoundException{
        checkUser(id);
        userRepository.deleteById(id);
    }

    /**
     * Checks if a user with the given ID exists.
     * @param id the ID of the user to check
     * @throws UserNotFoundException if the user with the given ID is not found
     */

    private void checkUser(UUID id) throws UserNotFoundException{
        if(id == null || !userRepository.existsById(id)){
            throw new UserNotFoundException("User not found");
        }
    }

    @Override
    public Optional<UserModel> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(UserModel user) {
        userRepository.save(user);
    }

    public String insertTicket(UUID id, TicketModel ticket)
            throws UserNotFoundException {
        UserModel user = getUserId(id);
        user.addTicket(ticket);
        saveUser(user);
        return "";
    }

}