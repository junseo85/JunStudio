package com.music.JunStudio.repository;

import com.music.JunStudio.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
=======
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

>>>>>>> origin/master
    Optional<PasswordResetToken> findByToken(String token);
}
