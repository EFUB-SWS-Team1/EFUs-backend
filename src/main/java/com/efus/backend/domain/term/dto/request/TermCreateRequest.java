package com.efus.backend.domain.term.dto.request;

import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TermCreateRequest(
        @NotBlank(message = "새 기수명은 필수입니다.")
        String name,

        @NotNull(message = "새 기수 시작일은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate startDate
) {
    public OrganizationTerm toEntity(Organization organization, User user) {
        return OrganizationTerm.builder()
                .organization(organization)
                .createdByUser(user)
                .name(this.name())
                .startDate(this.startDate())
                .termStatus(TermStatus.ACTIVE)
                .build();
    }
}