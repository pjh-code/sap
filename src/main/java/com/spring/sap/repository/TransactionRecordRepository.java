package com.spring.sap.repository;

import com.spring.sap.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.Map;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    /**
     * transaction_record 테이블의 AUTO_INCREMENT 값을 초기화하는 메서드 (MySQL 전용).
     */
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE transaction_record AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
    
    /**
     * 거래 내역을 특정 조건에 따라 필터링하여 검색하는 메서드
     * @param month 조회할 월 (null일 경우 무시)
     * @param parts 부품명 (null일 경우 무시)
     * @param maker 제조사 (null일 경우 무시)
     * @param profitPositive 수익 여부 (null일 경우 무시)
     * @return 필터 조건에 맞는 TransactionRecord 목록
     *
     * 필터 조건:
     * - month: 거래 날짜의 월과 일치하는 거래만 조회 (null일 경우 무시)
     * - parts: 아이템의 부품명과 일치하는 거래만 조회 (null일 경우 무시)
     * - maker: 아이템의 제조사와 일치하는 거래만 조회 (null일 경우 무시)
     * - profitPositive: 수익이 양수 또는 음수인 거래만 조회 (null일 경우 무시)
     */
    @Query("SELECT t FROM TransactionRecord t " +
           "WHERE (:month IS NULL OR FUNCTION('MONTH', t.transactionDate) = :month) " +
           "AND (:parts IS NULL OR t.item.parts = :parts) " +
           "AND (:maker IS NULL OR t.item.maker = :maker) " +
           "AND (:profitPositive IS NULL OR " +
           "(:profitPositive = TRUE AND (t.totalPrice > 0)) " +  // 수익 여부 필터링 (양수 조건)
           "OR (:profitPositive = FALSE AND (t.totalPrice <= 0)))") // 수익 여부 필터링 (음수 조건)
    List<TransactionRecord> findFilteredTransactions(
           @Param("month") Integer month,
           @Param("parts") String parts,
           @Param("maker") String maker,
           @Param("profitPositive") Boolean profitPositive);
    
    
    // 최신 거래에서 sellQuantity 값을 조회
    @Query("SELECT t.sellQuantity FROM TransactionRecord t WHERE t.transactionDate = (SELECT MAX(tr.transactionDate) FROM TransactionRecord tr) AND t.sellQuantity IS NOT NULL")
    Optional<Integer> findLatestSellQuantity();

    // 모든 거래의 totalPrice 합계를 계산 (일일 수익)
    @Query("SELECT SUM(t.totalPrice) FROM TransactionRecord t WHERE t.sellQuantity IS NOT NULL")
    Double calculateTotalProfit();

    // 구매 가격과 판매 가격을 기반으로 재고 가치를 계산
    @Query("SELECT COALESCE(SUM(CASE WHEN t.purchaseQuantity IS NOT NULL THEN t.totalPrice ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN t.sellQuantity IS NOT NULL THEN t.totalPrice ELSE 0 END), 0) FROM TransactionRecord t")
    Double calculateInventoryValue();

    // 재고 개수 계산 (구매 수량에서 판매 수량을 뺀 값)
    @Query("SELECT COALESCE(SUM(CASE WHEN t.purchaseQuantity IS NOT NULL THEN t.purchaseQuantity ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN t.sellQuantity IS NOT NULL THEN t.sellQuantity ELSE 0 END), 0) FROM TransactionRecord t")
    Integer calculateInventoryCount();

    /**
     * 아이템별 판매 수량 합계를 구하는 쿼리
     * @return 아이템별 판매 수량 합계
     */
    @Query("SELECT i.parts, SUM(t.sellQuantity) FROM TransactionRecord t " +
    	       "JOIN t.item i " +
    	       "WHERE t.sellQuantity IS NOT NULL " +
    	       "GROUP BY i.parts")
    	List<Object[]> getSalesDataByParts();
    	
	/**
     * 각 부품별 최상위 판매 품목을 가져오는 네이티브 쿼리
     * @return 판매량이 가장 많은 부품별 제품 정보
     */
    	
    
    	// 각 부품의 최상위 판매 품목 조회 쿼리
    	@Query(value = """
    	        SELECT 부품명, 제품명, 제조사, 매입가격, 판매가격, 판매량
    	        FROM (
    	            SELECT 
    	                i.parts AS 부품명,
    	                i.name AS 제품명,
    	                i.maker AS 제조사,
    	                i.purchase_price AS 매입가격,
    	                i.sell_price AS 판매가격,
    	                t.sell_quantity AS 판매량,
    	                ROW_NUMBER() OVER (PARTITION BY i.parts ORDER BY t.sell_quantity DESC) AS row_rank
    	            FROM item i
    	            JOIN transaction_record t ON i.item_id = t.item_id
    	        ) AS ranked_items
    	        WHERE row_rank = 1
    	        ORDER BY 판매량 DESC
    	    """, nativeQuery = true)
    	List<Map<String, Object>> findTopSellingItemsByPart();

@Query("SELECT FUNCTION('MONTH', t.transactionDate) AS month, SUM(t.totalPrice) AS totalProfit " +
       "FROM TransactionRecord t " +
       "WHERE t.sellQuantity IS NOT NULL " +
       "GROUP BY FUNCTION('MONTH', t.transactionDate) " +
       "ORDER BY month")
List<Object[]> findMonthlyProfit();



}
