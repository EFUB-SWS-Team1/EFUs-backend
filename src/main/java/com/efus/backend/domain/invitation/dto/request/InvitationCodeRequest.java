package com.efus.backend.domain.invitation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InvitationCodeRequest {

    @NotBlank(message = "초대 코드는 필수입니다.")
    private String code;
}