package com.spring.digital_logistics.config;

import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByRole(Role.ADMIN).isEmpty()){
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@logistics.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // Mot de passe par défaut
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println(">>>>>>>>>> Compte Administrateur créé avec succès ! <<<<<<<<<<");
        } else {
            System.out.println(">>>>>>>>>> Compte Administrateur déjà existant. <<<<<<<<<<");
        }
    }
}
