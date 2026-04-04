package dev.rubenpari.backend.security;

import dev.rubenpari.backend.model.User;
import dev.rubenpari.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Custom {@link UserDetailsService} that loads users by UUID (from JWT) or by email (for login).
 * Maps the domain {@link User} to Spring Security's {@link UserDetails},
 * locking the account if the email has not been confirmed yet.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by UUID string (JWT subject) or by email (login flow).
     * Attempts UUID parsing first; falls back to email lookup on failure.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            // Try interpreting the username as a UUID (used by the JWT filter)
            UUID userId = UUID.fromString(username);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (IllegalArgumentException ex) {
            // Fall back to email-based lookup (used during login authentication)
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password(user.getPasswordHash())
                .authorities("USER")
                .accountLocked(!user.isEmailConfirmed())
                .build();
    }
}
