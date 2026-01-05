package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.RefreshToken;
import com.spring.digital_logistics.exception.TokenRefreshException;
import com.spring.digital_logistics.repository.RefreshTokenRepository;
import com.spring.digital_logistics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Création d'un nouveau Refresh Token
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        // On récupère l'utilisateur (Attention: assurez-vous que l'utilisateur existe)
        refreshToken.setUser(userRepository.findById(userId).get());

        // On définit la date d'expiration (Maintenant + Durée configurée)
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        // On génère un token unique (UUID)
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    // Vérification de l'expiration
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    // Suppression par User ID (Rotation)
    @Transactional // Très important pour que la suppression fonctionne
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}