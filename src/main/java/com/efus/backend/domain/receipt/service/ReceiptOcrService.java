package com.efus.backend.domain.receipt.service;


//OCR Service Contract
public interface ReceiptOcrService {

    Long recognizeAmount(String storageKey);

    Long recognizeAmount(byte[] imageBytes, String filename, String contentType);
}
