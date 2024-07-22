package com.pc1.backendrupay.repositories;

import com.pc1.backendrupay.domain.UserModel;
import com.pc1.backendrupay.enums.TypeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
/**
 * This interface represents the repository for user data access.
 */
@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByName(String name);
    Optional<UserModel> findByEmail(String email);
    List<UserModel> findByTypeUser(TypeUser typeUser);
    List<UserModel> findByRegistration(String registration);
    List<UserModel> findByTypeUserAndRegistration(TypeUser typeUser, String registration);
}
