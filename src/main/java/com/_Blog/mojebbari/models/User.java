package com._Blog.mojebbari.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data // Lombok: Generates getters, setters, toString(), etc.
@Builder // Lombok: Helps build objects (User.builder().username("...").build())
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
@Entity // JPA: Marks this class as a database entity
@Table(name = "_user") // JPA: Specifies the table name (using "user" can cause issues in SQL)
public class User implements UserDetails {

    @Id // JPA: Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // JPA: Auto-increments the ID
    private Long id;

    @Column(nullable = false, unique = true) // JPA: Username must be unique and not null
    private String username;
    
    // You can add other fields like firstName, lastName, etc.
    // private String firstName;
    // private String lastName;

    @Email // hadi katkhdam f registration 9bal mn hna f controllers o khasni ndeclari @valid tma bach tkhdem
    @NotBlank // hadi katkhdam f registration 9bal mn hna f controllers o khasni ndeclari @valid tma bach tkhdem
    @Column(nullable = false, unique = true) // JPA: Email must be unique and not null
    private String email;

    @Column(nullable = false) // JPA: Password cannot be null
    private String password;

    @Enumerated(EnumType.STRING) // JPA: Stores the enum as a string ("USER" or "ADMIN")
    private Role role;

    // --- UserDetails Methods (Required by Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This tells Spring Security what role this user has
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security will use this (our email) to authenticate
        // You could also use the 'username' field
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You could add logic for this later
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You could add logic for this later
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You could add logic for this later
    }

    @Override
    public boolean isEnabled() {
        return true; // You could add logic for this later
    }
}