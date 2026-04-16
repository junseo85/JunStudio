package com.music.JunStudio.repository;

import com.music.JunStudio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring magically writes the SQL for this just by reading the method name!
    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);
}