package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.repository.UserRepository;
import com.spring.digital_logistics.security.service.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository repo){
        this.userRepository = repo;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        return  UserDetailsImpl.build(user);
    }
}
