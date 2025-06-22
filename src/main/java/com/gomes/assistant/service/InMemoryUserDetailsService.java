package com.gomes.assistant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    @Value("${app.auth.username}")
    private String username;

    @Value("${app.auth.password}")
    private String password;

    @Value("${app.auth.roles}")
    private String roles;

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (!this.username.equals(username)) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return User.builder()
                .username(username)
                .password(password)
                .roles(roles.split(","))
                .build();
    }
}
