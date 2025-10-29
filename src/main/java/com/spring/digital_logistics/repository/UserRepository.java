package com.spring.digital_logistics.repository;

import com.spring.digital_logistics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
