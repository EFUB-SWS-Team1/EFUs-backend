package com.efus.backend.domain.organization.dto.request;

import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrganizationCreateRequest(
        @NotBlank(message = "단체명은 필수입니다.")
        String name,

        @NotBlank(message = "첫 기수명은 필수입니다.")
        String initialTermName,

        @NotNull(message = "첫 기수 시작일은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate initialTermStartDate
) {
        public Organization toEntity(User user) {
                return Organization.builder()
                        .name(this.name())
                        .createdByUser(user)
                        .build();
        }

        public OrganizationTerm toTermEntity(Organization organization, User user) {
                return OrganizationTerm.builder()
                        .organization(organization)
                        .createdByUser(user)
                        .name(this.initialTermName())
                        .startDate(this.initialTermStartDate())
                        .endDate(null) // 생성 직후에는 null
                        .termStatus(TermStatus.ACTIVE) // 생성 직후에는 ACTIVE
                        .build();
        }
}
