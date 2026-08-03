package com.efus.backend.domain.organization.dto.response;

import java.util.List;

public record OrganizationListResponse(
        List<OrganizationResponse> organizations
) {
    public static OrganizationListResponse from(List<OrganizationResponse> organizations) {
        return new OrganizationListResponse(organizations);
    }
}