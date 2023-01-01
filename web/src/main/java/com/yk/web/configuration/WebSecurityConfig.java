package com.yk.web.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserDetailsManager userDetailsManager;

    public WebSecurityConfig(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
        setupUsers();
    }

    @Bean
    public SecurityFilterChain filterChain(@NotNull HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeHttpRequests().anyRequest().authenticated().and().formLogin().and().httpBasic();
        return http.build();
    }

    private void setupUsers() {
        UserDetails user = new User("yahor", "{noop}nstest", new ArrayList<>());
        userDetailsManager.createUser(user);
    }

}
