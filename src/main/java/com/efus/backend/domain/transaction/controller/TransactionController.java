// TODO: funding,term 연관관계 + FundingQueryService,MemberQueryService,TermQueryService 병합 후 주석 해제
//package com.efus.backend.domain.transaction.controller;
//
//import com.efus.backend.domain.transaction.dto.request.TransactionCreateRequest;
//import com.efus.backend.domain.transaction.dto.request.TransactionUpdateRequest;
//import com.efus.backend.domain.transaction.dto.response.TransactionDetailResponse;
//import com.efus.backend.domain.transaction.dto.response.TransactionResponse;
//import com.efus.backend.domain.transaction.service.TransactionService;
//import com.efus.backend.global.response.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PatchMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/terms/{termId}/transactions")
//public class TransactionController {
//
//    private final TransactionService transactionService;
//
//    @PostMapping
//    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
//            @PathVariable Long termId,
//            @Valid @RequestBody TransactionCreateRequest request
//    ) {
//        TransactionResponse response = transactionService.createTransaction(termId, request);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ApiResponse.success(response, "거래가 등록되었습니다."));
//    }
//    @GetMapping("/{transactionId}")
//    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransaction(
//            @PathVariable Long termId,
//            @PathVariable Long transactionId
//    ) {
//        TransactionDetailResponse response = transactionService.getTransaction(termId, transactionId);
//
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    @PatchMapping("/{transactionId}")
//    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
//            @PathVariable Long termId,
//            @PathVariable Long transactionId,
//            @Valid @RequestBody TransactionUpdateRequest request
//    ) {
//        TransactionResponse response = transactionService.updateTransaction(termId, transactionId, request);
//
//        return ResponseEntity.ok(ApiResponse.success(response, "거래가 수정되었습니다."));
//    }
//    @DeleteMapping("/{transactionId}")
//    public ResponseEntity<Void> deleteTransaction(
//            @PathVariable Long termId,
//            @PathVariable Long transactionId
//    ) {
//        transactionService.deleteTransaction(termId, transactionId);
//
//        return ResponseEntity.noContent().build();
//    }
//}
//
