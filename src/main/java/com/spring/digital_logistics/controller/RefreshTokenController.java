package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.entity.RefreshToken;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.exception.TokenRefreshException;
import com.spring.digital_logistics.payload.request.TokenRefreshRequest;
import com.spring.digital_logistics.payload.response.TokenRefreshResponse;
import com.spring.digital_logistics.security.jwt.JwtUtils; // Assurez-vous du bon import
import com.spring.digital_logistics.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Bonne pratique : préfixer l'URL
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils; // <--- Ajouté

    // Injection via le constructeur
    public RefreshTokenController(RefreshTokenService refreshTokenService, JwtUtils jwtUtils) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
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

                    // 3. Création du nouveau Access Token JWT
                    // Note : adaptez "generateTokenFromUsername" selon votre JwtUtils existant
                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());

                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }
}