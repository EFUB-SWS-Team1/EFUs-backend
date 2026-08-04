package com.efus.backend.domain.invitation.dto.request;

import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor
public class InvitationCodeRequest {

    private String code;

    public String requireCode() {
        if (!StringUtils.hasText(code)) {
            throw new CustomException(ErrorCode.INVITATION_CODE_REQUIRED);
        }

        return code;
    }
}
