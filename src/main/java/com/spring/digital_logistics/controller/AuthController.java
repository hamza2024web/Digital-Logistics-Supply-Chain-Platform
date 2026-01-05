package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.login.LoginDTO;
import com.spring.digital_logistics.dto.request.user.UserCreateDTO;
import com.spring.digital_logistics.dto.response.login.LoginResponseDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.entity.RefreshToken;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.exception.TokenRefreshException;
import com.spring.digital_logistics.payload.request.TokenRefreshRequest;
import com.spring.digital_logistics.payload.response.TokenRefreshResponse;
import com.spring.digital_logistics.security.jwt.JwtUtils;
import com.spring.digital_logistics.service.RefreshTokenService;
import com.spring.digital_logistics.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    public AuthController (UserService userService, RefreshTokenService refreshTokenService, JwtUtils jwtUtils){
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Inscritpion d'un nouvel utilisateur ")
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserCreateDTO userCreateDTO){
        UserDTO createUser = userService.register(userCreateDTO);
        return ResponseEntity.ok(createUser);
    }

    @Operation(summary = "Authentification d'un utilisateur")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO){
        LoginResponseDTO response = userService.login(loginDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request){
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();

                    // 1. Suppression de l'ancien token (Rotation)
                    refreshTokenService.deleteByUserId(user.getId());

                    // 2. Création du nouveau Refresh Token
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());

                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }
}
