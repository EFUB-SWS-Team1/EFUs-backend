package com.efus.backend.domain.receipt.service;


//OCR Service Contract
public interface ReceiptOcrService {

    Long recognizeAmount(String storageKey);
}