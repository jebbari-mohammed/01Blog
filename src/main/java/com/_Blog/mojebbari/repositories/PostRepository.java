package com._Blog.mojebbari.repositories;

import com._Blog.mojebbari.models.Post;
import com._Blog.mojebbari.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // JpaRepository gives us .save(), .findById(), .delete(), etc.
    
    // We can add custom methods just by naming them:
    
    // Finds all posts written by a specific user
    List<Post> findAllByUser(User user);
    
    // Finds all posts by a user, ordered by when they were created
    List<Post> findAllByUserOrderByTimestampDesc(User user);
}