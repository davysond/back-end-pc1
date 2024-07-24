package com.pc1.backendrupay.controllers;

import com.pc1.backendrupay.domain.UserDTO;
import com.pc1.backendrupay.domain.UserModel;
import com.pc1.backendrupay.enums.TypeUser;
import com.pc1.backendrupay.services.UserService;
import com.pc1.backendrupay.services.UserServiceImpl;
import com.pc1.backendrupay.exceptions.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the controller layer for managing user-related operations.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    final private UserService userService;

    /**
     * Constructor for UserController.
     * 
     * @param userService the UserService instance to be used
     */
    public UserController(UserServiceImpl userService){
        this.userService = userService;
    }


    @GetMapping("/list")
    public List<UserModel> listUsers(@RequestParam(required = false) TypeUser typeUser,
                                     @RequestParam(required = false) String registration,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime creationDate) {
        return userService.listUsers(typeUser, registration, creationDate);
    }

    /**
     * Get a user by their ID.
     * 
     * @param id the ID of the user to retrieve
     * @return an Optional containing the UserModel object if found, or an empty Optional if not found
     */
    @GetMapping("/{id}")
    public <Optional> java.util.Optional<UserModel> listUserById(@PathVariable("id") UUID id){
        return userService.listUserById(id);
    }

    /**
     * Edit a user by their ID.
     * 
     * @param id the ID of the user to edit
     * @param userDTO the UserDTO object containing the updated user data
     * @return a ResponseEntity with the edited UserModel object if successful, 
     * or a ResponseEntity with an error message if the user is not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editUser(@PathVariable UUID id, @RequestBody UserDTO userDTO) {
        UserModel user;

        try {
            user = userService.editUser(id, userDTO);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok().body(user);
    }

    /**
     * Delete a user by their ID.
     * 
     * @param id the ID of the user to delete
     * @return a ResponseEntity with the ID of the deleted user if successful, 
     * or a ResponseEntity with an error message if the user is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") UUID id) {
        try {
            userService.deleteUser(id);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok().body(id);
    }
    @GetMapping("/email/{email}")
    public <Optional> java.util.Optional<UserModel> listUserByEmail(@PathVariable("email") String email){
        return userService.getUserByEmail(email);
    }
    
}
