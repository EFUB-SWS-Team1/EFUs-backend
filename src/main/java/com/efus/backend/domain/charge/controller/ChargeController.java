package com.efus.backend.domain.charge.controller;

import com.efus.backend.domain.charge.dto.request.ChargeCreateRequest;
import com.efus.backend.domain.charge.dto.request.ChargeMemberListRequest;
import com.efus.backend.domain.charge.dto.request.ChargePreviewRequest;
import com.efus.backend.domain.charge.dto.request.ChargeUpdateRequest;
import com.efus.backend.domain.charge.dto.request.ChargePaymentRequest;
import com.efus.backend.domain.charge.dto.response.ChargeCreateResponse;
import com.efus.backend.domain.charge.dto.response.ChargeDetailResponse;
import com.efus.backend.domain.charge.dto.response.ChargeMemberListResponse;
import com.efus.backend.domain.charge.dto.response.ChargePreviewResponse;
import com.efus.backend.domain.charge.dto.response.ChargePaymentResponse;
import com.efus.backend.domain.charge.service.ChargeService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChargeController {

    private final ChargeService chargeService;

    @PostMapping("/terms/{termId}/charges")
    public ResponseEntity<ApiResponse<ChargeCreateResponse>> create(
            @PathVariable Long termId,
            @Valid @RequestBody ChargeCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(chargeService.createCharge(termId, request)));
    }

    @PostMapping("/terms/{termId}/charges/preview")
    public ResponseEntity<ApiResponse<ChargePreviewResponse>> preview(
            @PathVariable Long termId,
            @Valid @RequestBody ChargePreviewRequest request
    ) {
        ChargePreviewResponse response = chargeService.preview(termId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/charges/{chargeId}")
    public ResponseEntity<ApiResponse<ChargeDetailResponse>> getChargeDetail(
            @PathVariable Long chargeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(chargeService.getChargeDetail(chargeId)));
    }

    @GetMapping("/charges/{chargeId}/members")
    public ResponseEntity<ApiResponse<ChargeMemberListResponse>> getChargeMembers(
            @PathVariable Long chargeId,
            @Valid @ModelAttribute ChargeMemberListRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(chargeService.getChargeMembers(chargeId, request)));
    }

    @PatchMapping("/charges/{chargeId}")
    public ResponseEntity<ApiResponse<ChargeDetailResponse>> update(
            @PathVariable Long chargeId, @RequestBody ChargeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(chargeService.updateCharge(chargeId, request)));
    }

    @DeleteMapping("/charges/{chargeId}")
    public ResponseEntity<Void> delete(@PathVariable Long chargeId) {
        chargeService.deleteCharge(chargeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/charges/{chargeId}/members/{chargeMemberId}/payment")
    public ResponseEntity<ApiResponse<ChargePaymentResponse>> pay(
            @PathVariable Long chargeId,
            @PathVariable Long chargeMemberId,
            @RequestBody(required = false) ChargePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                chargeService.pay(chargeId, chargeMemberId, request)));
    }
}
