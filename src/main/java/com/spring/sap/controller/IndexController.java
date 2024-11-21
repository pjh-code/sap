package com.spring.sap.controller;

import com.spring.sap.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

@RestController
public class IndexController {

    @Autowired
    private ChartService chartService;

    @GetMapping("/getPieChartData")
    public Map<String, Object> getPieChartData() {
        return chartService.getPieChartData();
    }

    @GetMapping("/getSummaryTilesData")
    public Map<String, Object> getSummaryTilesData() {
        Map<String, Object> summaryData = chartService.getSummaryTilesData();
        System.out.println("Summary Data: " + summaryData); // 콘솔 로그로 확인
        return summaryData;
    }

    @GetMapping("/top-selling-items")
    public List<Map<String, Object>> getTopSellingItems() {
        return chartService.getTopSellingItemsByPart();
    }
    
    @GetMapping("/getMonthlyProfitData")
    public List<Map<String, Object>> getMonthlyProfitData() {
        return chartService.getMonthlyProfitData();
    }
    
    
    
}
