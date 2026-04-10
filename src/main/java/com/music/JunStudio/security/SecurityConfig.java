package com.music.JunStudio.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService; // Add this import!
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {

        // 1. Pass the userDetailsService DIRECTLY into the constructor parenthesis
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // 2. You no longer need the authProvider.setUserDetailsService(...) line!

        // 3. Keep the password encoder setter
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/css/**", "/js/**").permitAll() // Allow everyone to see sign up & styles
                        .anyRequest().authenticated() // Everything else requires login
                )
                .formLogin(form -> form
                        .loginPage("/login") // Tells Spring to look for your new login.html
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    //use default log in page- does not have create an account
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Keep this disabled for now so our HTML forms work easily
//                .authorizeHttpRequests(auth -> auth
//                        // OPEN THE GATES for the registration page and static files
//                        .requestMatchers("/register", "/css/**").permitAll()
//                        // Lock down everything else
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .defaultSuccessUrl("/dashboard", true)
//                        .permitAll()
//                );
//
//        return http.build();
//    }

    //permit all version
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Disables CSRF protection so we can test forms easily later
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll() // OPEN THE GATES: Every single page is now public
//                );
//
//        return http.build();
//    }


    //original version
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // These URLs are open to the public
//                        .requestMatchers("/", "/home", "/about").permitAll()
//                        // Allow static files (CSS, JS, images) to load without logging in
//                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
//                        // EVERY other URL requires you to be logged in
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        // Uses Spring's default login page, but we tell it where to go after success
//                        .defaultSuccessUrl("/dashboard", true)
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        // Where to go when the user logs out
//                        .logoutSuccessUrl("/")
//                        .permitAll()
//                );
//
//        return http.build();
//    }

    // This is CRITICAL. Spring requires passwords to be hashed for security.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
//        UserDetails admin = User.builder()
//                .username("testjun@email.com")
//                .password(encoder.encode("password123")) // This automatically hashes it for you!
//                .roles("ADMIN") // Spring automatically adds the "ROLE_" prefix here
//                .build();
//
//        return new InMemoryUserDetailsManager(admin);
//    }
}