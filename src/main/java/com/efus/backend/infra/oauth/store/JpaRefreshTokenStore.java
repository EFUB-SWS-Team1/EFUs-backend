package com.efus.backend.infra.oauth.store;

import com.efus.backend.infra.oauth.entity.RefreshToken;
import com.efus.backend.infra.oauth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("!prod")
@RequiredArgsConstructor
@Transactional
public class JpaRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void saveOrUpdate(Long userId, String token) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.rotateToken(token),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(token)
                                        .build()
                        )
                );
    }

    @Override
    public Optional<String> findTokenByUserId(Long userId) {
        return refreshTokenRepository.findByUserId(userId)
                .map(RefreshToken::getToken);
    }

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
