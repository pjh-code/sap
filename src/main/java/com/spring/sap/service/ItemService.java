package com.spring.sap.service;

import com.spring.sap.entity.Item;
import com.spring.sap.entity.TransactionRecord;
import com.spring.sap.repository.TransactionRecordRepository;
import com.spring.sap.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    /**
     * 전체 아이템 목록을 반환하는 메서드
     * @return 전체 Item 목록
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
        }
    @Autowired
    private TransactionRecordRepository transactionRepository;

    /**
     * 필터링된 거래 목록을 조회하는 메서드
     * @param month 조회할 월 (null일 경우 모든 월 포함)
     * @param parts 조회할 부품명 (null일 경우 모든 부품 포함)
     * @param maker 조회할 제조사 (null일 경우 모든 제조사 포함)
     * @param profitPositive 수익 여부 (null일 경우 수익 필터링 안 함)
     * @return 필터 조건에 맞는 거래 목록 반환
     */
    public List<TransactionRecord> getFilteredTransactions(Integer month, String parts, String maker, Boolean profitPositive) {
        return transactionRepository.findFilteredTransactions(month, parts, maker, profitPositive);
    }
    }
