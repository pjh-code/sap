package com.spring.sap.controller;

import com.spring.sap.entity.TransactionRecord;
import com.spring.sap.repository.TransactionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    /**
     * 모든 거래 기록을 조회하는 메서드
     * @return 전체 거래 기록 목록
     */
    @GetMapping("/list")
    public ResponseEntity<List<TransactionRecord>> getAllTransactions() {
        List<TransactionRecord> transactions = transactionRecordRepository.findAll();
        return ResponseEntity.ok(transactions);
    }
}
