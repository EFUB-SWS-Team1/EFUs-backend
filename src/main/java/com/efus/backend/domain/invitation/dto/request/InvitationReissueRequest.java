package com.efus.backend.domain.invitation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;

@Getter
@NoArgsConstructor
public class InvitationReissueRequest {

    /**
     * 전달용 초대 코드를 새로 발급할 역할.
     *
     * 재발급 시 해당 역할의 기존 활성 코드는 비활성화되고,
     * 7일간 유효한 새로운 초대 코드가 생성된다.
     */

    @NotNull(message = "재발급할 초대 역할은 필수입니다.")
    private String role;

    public TermMemberRole toRole() {
        try {
            return TermMemberRole.valueOf(role);
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_INVITATION_ROLE);
        }
    }
}
