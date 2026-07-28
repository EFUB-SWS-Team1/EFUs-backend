package com.efus.backend.domain.organization.controller;

import com.efus.backend.domain.organization.dto.request.OrganizationCreateRequest;
import com.efus.backend.domain.organization.dto.response.OrganizationCreateResponse;
import com.efus.backend.domain.organization.dto.response.OrganizationDetailResponse;
import com.efus.backend.domain.organization.dto.response.OrganizationListResponse;
import com.efus.backend.domain.organization.service.OrganizationService;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    // 단체 생성
//    @PostMapping
//    public ResponseEntity<ApiResponse<OrganizationCreateResponse>> createOrganization(
//            @Valid @RequestBody OrganizationCreateRequest request
//            ) {
//
//        OrganizationCreateResponse result = organizationService.createOrganization(request);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success(result));
//    }

    // 내 단체 목록 조회
//    @GetMapping
//    public ResponseEntity<ApiResponse<OrganizationListResponse>> getMyOrganizations() {
//        OrganizationListResponse response = organizationService.getMyOrganizations();
//
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

    // 단체 상세 조회
    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponse<OrganizationDetailResponse>> getOrganizationDetail(
            @PathVariable("organizationId") Long organizationId) {

        OrganizationDetailResponse response = organizationService.getOrganizationDetail(organizationId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
