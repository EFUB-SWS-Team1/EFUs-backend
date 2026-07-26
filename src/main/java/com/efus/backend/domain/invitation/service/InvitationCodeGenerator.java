package com.efus.backend.domain.invitation.service;

import com.efus.backend.domain.invitation.repository.InvitationRepository;
import com.efus.backend.domain.member.entity.TermMemberRole;
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

    private static final int RANDOM_CODE_LENGTH = 6;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final InvitationRepository invitationRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(TermMemberRole role) {
        for (int attempt = 0;
             attempt < MAX_GENERATION_ATTEMPTS;
             attempt++) {

            String code = createPrefix(role)
                    + generateRandomCode();

            if (!invitationRepository.existsByCode(code)) {
                return code;
            }
        }

        throw new CustomException(
                ErrorCode.INVITATION_CODE_GENERATION_FAILED
        );
    }

    private String createPrefix(TermMemberRole role) {
        return switch (role) {
            case STAFF -> "EFUS-S-";
            case MEMBER -> "EFUS-M-";
        };
    }

    private String generateRandomCode() {
        StringBuilder codeBuilder =
                new StringBuilder(RANDOM_CODE_LENGTH);

        for (int index = 0;
             index < RANDOM_CODE_LENGTH;
             index++) {

            int randomIndex =
                    secureRandom.nextInt(CHARACTERS.length());

            codeBuilder.append(
                    CHARACTERS.charAt(randomIndex)
            );
        }

        return codeBuilder.toString();
    }
}