package com.efus.backend.domain.invitation.service;

import com.efus.backend.domain.invitation.repository.InvitationRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class InvitationCodeGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int CODE_LENGTH = 10;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final InvitationRepository invitationRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        for (int attempt = 0;
             attempt < MAX_GENERATION_ATTEMPTS;
             attempt++) {

            String code = generateRandomCode();

            if (!invitationRepository.existsByCode(code)) {
                return code;
            }
        }

        throw new CustomException(
                ErrorCode.INVITATION_CODE_GENERATION_FAILED
        );
    }

    private String generateRandomCode() {
        StringBuilder codeBuilder =
                new StringBuilder(CODE_LENGTH);

        for (int index = 0; index < CODE_LENGTH; index++) {
            int randomIndex =
                    secureRandom.nextInt(CHARACTERS.length());

            codeBuilder.append(
                    CHARACTERS.charAt(randomIndex)
            );
        }

        return codeBuilder.toString();
    }
}