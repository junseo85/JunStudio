package com.music.JunStudio.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Find the user in MySQL by their email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Wrap your User entity into a UserDetails object that Spring Security understands
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(), // This must match the getter for your hashed password!
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}