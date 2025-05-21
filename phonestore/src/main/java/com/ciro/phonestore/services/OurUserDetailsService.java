package com.ciro.phonestore.services;

import com.ciro.phonestore.models.OurUsers;
import com.ciro.phonestore.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OurUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(OurUserDetailsService.class);

    @Autowired
    private UsersRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for username: {}", username);

        OurUsers user = usersRepo.findByEmail(username)
                .orElseThrow(() -> {
                    logger.debug("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        logger.debug("User found - email: {}, role: {}", user.getEmail(), user.getRole());
        return user;
    }
}
