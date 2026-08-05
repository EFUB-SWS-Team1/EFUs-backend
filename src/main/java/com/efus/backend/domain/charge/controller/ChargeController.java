package com.efus.backend.domain.charge.controller;

import com.efus.backend.domain.charge.dto.request.ChargePreviewRequest;
import com.efus.backend.domain.charge.dto.response.ChargePreviewResponse;
import com.efus.backend.domain.charge.service.ChargeService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms/{termId}/charges")
public class ChargeController {

    private final ChargeService chargeService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<ChargePreviewResponse>> preview(
            @PathVariable Long termId,
            @Valid @RequestBody ChargePreviewRequest request
    ) {
        ChargePreviewResponse response = chargeService.preview(termId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
