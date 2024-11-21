package com.spring.sap.service;


import com.spring.sap.repository.TransactionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ChartService {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;



    // 원형 차트 데이터를 가져오는 메서드
    public Map<String, Object> getPieChartData() {
        List<Object[]> results = transactionRecordRepository.getSalesDataByParts();

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Object[] result : results) {
            labels.add((String) result[0]);       // parts 값
            data.add((Long) result[1]);            // 판매 수량 합계
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);

        return response;
    }

 // 일일 수익 및 재고 가치를 형식화하여 반환하는 메서드
    public Map<String, Object> getSummaryTilesData() {
    	Map<String, Object> summaryData = new HashMap<>();

        // 수익 및 재고 가치 값을 원화 형식 없이 숫자 그대로 반환
        Double dailyProfit = transactionRecordRepository.calculateTotalProfit();
        Double inventoryValue = transactionRecordRepository.calculateInventoryValue();

        summaryData.put("newOrders", transactionRecordRepository.findLatestSellQuantity().orElse(0));
        summaryData.put("dailyProfit", dailyProfit); // 원화 포맷 없이 반환
        summaryData.put("inventoryValue", inventoryValue); // 원화 포맷 없이 반환
        summaryData.put("inventoryCount", transactionRecordRepository.calculateInventoryCount());

        return summaryData;
    }



 // 각 부품별 최상위 판매 품목을 가져오는 메서드
    public List<Map<String, Object>> getTopSellingItemsByPart() {
        return transactionRecordRepository.findTopSellingItemsByPart();
        
    }

public List<Map<String, Object>> getMonthlyProfitData() {
    List<Object[]> results = transactionRecordRepository.findMonthlyProfit();
    
    List<Map<String, Object>> monthlyProfitList = new ArrayList<>();
    
    for (Object[] result : results) {
        Map<String, Object> monthlyData = new HashMap<>();
        monthlyData.put("month", result[0]);  // 월
        monthlyData.put("totalProfit", result[1]);  // 월별 총 수익
        monthlyProfitList.add(monthlyData);
    }
    
    return monthlyProfitList;
}
   
    
}