package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRole(Role role);
}
