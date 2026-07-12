package com.music.JunStudio.model;

import jakarta.persistence.*;
<<<<<<< HEAD
=======
import lombok.AllArgsConstructor;
>>>>>>> origin/master
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
<<<<<<< HEAD
=======
@AllArgsConstructor
>>>>>>> origin/master
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD
    @Column(nullable = false, unique = true)
=======
    @Column(unique = true, nullable = false)
>>>>>>> origin/master
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

<<<<<<< HEAD
    @Column(name = "expiry_date_time", nullable = false)
    private LocalDateTime expiryDateTime;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken(String token, User user, LocalDateTime expiryDateTime) {
        this.token = token;
        this.user = user;
        this.expiryDateTime = expiryDateTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDateTime);
=======
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
>>>>>>> origin/master
    }
}
