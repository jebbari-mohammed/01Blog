package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Tells Spring this is a Repository bean
public interface UserRepository extends JpaRepository<User, Long> {

    // JpaRepository<User, Long> gives us:
    // .save(User user)
    // .findById(Long id)
    // .findAll()
    // .deleteById(Long id)
    // ...and many more!

    // We add this custom method because Spring Security needs to find a user by
    // their email (which we defined as their 'username')
    Optional<User> findByEmail(String email);
}