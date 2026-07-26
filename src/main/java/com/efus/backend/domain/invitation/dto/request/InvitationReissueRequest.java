package com.efus.backend.domain.invitation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}