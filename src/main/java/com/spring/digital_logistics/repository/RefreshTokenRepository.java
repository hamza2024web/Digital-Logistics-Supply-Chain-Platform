package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.RefreshToken;
import com.spring.digital_logistics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    void deleteByUser(User user);
}
