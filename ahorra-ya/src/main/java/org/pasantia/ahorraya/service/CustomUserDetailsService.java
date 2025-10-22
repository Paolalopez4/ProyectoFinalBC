package org.pasantia.ahorraya.service;

import lombok.RequiredArgsConstructor;
import org.pasantia.ahorraya.repository.AdminRepository;
import org.pasantia.ahorraya.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service that loads user details for Spring Security authentication.
 *
 * <p>This implementation resolves a principal first by username against the application's
 * User repository and, if not found, tries the Admin repository using the supplied identifier
 * (which may be an email). The returned {@link UserDetails} contains the username, password
 * and granted authorities required by Spring Security.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository used to look up regular application users by username.
     */
    public final UserRepository userRepository;

    /**
     * Repository used to look up administrators by email.
     */
    public final AdminRepository adminRepository;


    /**
     * Load a {@link UserDetails} by username or admin email.
     *
     * <p>First attempts to find a user by username. If not found, attempts to find an admin by email.
     * When a principal is found, builds a Spring Security {@code User} with the appropriate role.</p>
     *
     * @param username the username or admin email to look up
     * @return a populated {@link UserDetails} for authentication
     * @throws UsernameNotFoundException if no user or admin is found with the provided identifier
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_USER")
                        .build()
                )
                .orElseGet(() ->
                        adminRepository.findByEmail(username)
                                .map(admin -> org.springframework.security.core.userdetails.User.builder()
                                        .username(admin.getEmail())
                                        .password(admin.getPassword())
                                        .authorities("ROLE_ADMIN")
                                        .build()
                                )
                                .orElseThrow(() -> new UsernameNotFoundException(
                                        "User not found with username or email: " + username
                                ))
                );
    }

}
