package com.pc1.backendrupay.domain;

import com.pc1.backendrupay.enums.TypeUser;
import jakarta.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the model for a user.
 */
@Entity
@Table(name = "tb_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserModel implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private TypeUser typeUser;
    private String registration;// Optional attribute

    private LocalDateTime creationDate;
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<TicketModel> tickets;



    /**
     * Constructor for the UserModel class that takes a UserDTO object as a parameter.
     *
     * @param userDTO the UserDTO object containing the user data
     */
    public UserModel(UserDTO userDTO) {
        this.name = userDTO.name();
        this.email = userDTO.email();
        this.password = userDTO.password();
        this.typeUser = userDTO.typeUser();
        this.registration = userDTO.registration();
        this.creationDate = userDTO.creationDate();
        this.tickets = new ArrayList<TicketModel>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(typeUser.name()));

    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addTicket(TicketModel ticket) {
        tickets.add(ticket);

    }

}