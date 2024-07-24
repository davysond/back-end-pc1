package com.pc1.backendrupay.services;

import com.pc1.backendrupay.domain.TicketModel;
import com.pc1.backendrupay.domain.UserDTO;
import com.pc1.backendrupay.domain.UserModel;
import com.pc1.backendrupay.enums.TypeUser;
import com.pc1.backendrupay.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    public Optional<UserModel> getUserByName(String name);
    public UserModel getUserId(UUID id) throws UserNotFoundException;
    public List<UserModel> listUsers(TypeUser typeUser, String registration, LocalDateTime creationDate);
    public Optional<UserModel> listUserById(UUID id);
    public UserModel editUser(UUID id, UserDTO userDTO) throws UserNotFoundException;
    public void deleteUser(UUID id) throws UserNotFoundException;
    public Optional<UserModel> getUserByEmail(String email);
    public void saveUser(UserModel user);
    public String insertTicket(UUID id, TicketModel ticket)
            throws UserNotFoundException;
}