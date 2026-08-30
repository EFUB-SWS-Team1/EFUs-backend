package com.efus.backend.infra.oauth.store;

import java.util.Optional;

public interface RefreshTokenStore {

    void saveOrUpdate(Long userId, String token);

    Optional<String> findTokenByUserId(Long userId);

    void deleteByUserId(Long userId);
}
