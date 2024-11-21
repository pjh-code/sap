package com.spring.sap.service;

import org.apache.poi.ss.usermodel.*;
import com.spring.sap.entity.Item;
import com.spring.sap.entity.TransactionRecord;
import com.spring.sap.repository.ItemRepository;
import com.spring.sap.repository.TransactionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Excel 파일을 처리하여 데이터베이스에 저장하는 서비스 클래스
@Service
@Transactional
public class ExcelService {

    // 로깅을 위한 Logger 설정
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    // 날짜 형식 설정 (yyyy-MM-dd 형식)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    /**
     * 전체 Excel 파일을 처리하는 메서드
     * @param inputStream Excel 파일의 InputStream
     */
    public void processExcelData(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // 기존 데이터 삭제 및 AUTO_INCREMENT 초기화
            deleteAllData();

            // 엑셀 파일에서 데이터를 읽어와서 저장
            saveItemsFromWorkbook(workbook);         // 첫 번째 시트에서 아이템 데이터 저장
            saveTransactionsFromWorkbook(workbook);  // 두 번째 시트에서 거래 데이터 저장
        } catch (Exception e) {
            logger.error("Error while processing Excel file", e);
        }
    }

    // 기존 데이터를 삭제하고, AUTO_INCREMENT 값을 초기화하는 메서드
    private void deleteAllData() {
        // 거래 기록 삭제 (TransactionRecord 테이블이 아이템 테이블을 참조하므로 먼저 삭제)
        transactionRecordRepository.deleteAll();
        transactionRecordRepository.flush(); // 변경 사항을 즉시 데이터베이스에 반영

        // 아이템 데이터 삭제
        itemRepository.deleteAll();
        itemRepository.flush(); // 변경 사항을 즉시 데이터베이스에 반영

        // AUTO_INCREMENT 값 초기화
        itemRepository.resetAutoIncrement();
        transactionRecordRepository.resetAutoIncrement();

        logger.info("기존 데이터를 모두 삭제하고 AUTO_INCREMENT 값을 초기화했습니다.");
    }


    /**
     * Workbook에서 아이템 데이터를 저장하는 메서드 (첫 번째 시트)
     * @param workbook 엑셀 파일의 Workbook 객체
     */
    public void saveItemsFromWorkbook(Workbook workbook) {
        Sheet itemSheet = workbook.getSheetAt(0); // 첫 번째 시트로 접근
        List<Item> items = new ArrayList<>();

        for (Row row : itemSheet) {
            if (row.getRowNum() == 0) continue; // 헤더 스킵

            String itemId = getStringCellValue(row.getCell(0));
            String name = getStringCellValue(row.getCell(1));
            String parts = getStringCellValue(row.getCell(2));
            String maker = getStringCellValue(row.getCell(3));
            Double purchasePrice = getNumericCellValue(row.getCell(4));
            Double sellPrice = getNumericCellValue(row.getCell(5));
            String performance = getStringCellValue(row.getCell(6));

            // 필수 데이터 유효성 검사
            if (itemId == null || name == null || maker == null) {
                logger.warn("Skipping row {}: Missing required data (ID, Name, or Maker)", row.getRowNum());
                continue;
            }

            try {
                Item item = new Item();
                item.setId(itemId);
                item.setName(name);
                item.setParts(parts);
                item.setMaker(maker);
                item.setPurchasePrice(purchasePrice != null ? purchasePrice : 0.0);
                item.setSellPrice(sellPrice != null ? sellPrice : 0.0);
                item.setPerformance(performance);

                items.add(item);
            } catch (Exception e) {
                logger.warn("Error processing item row: " + row.getRowNum(), e);
            }
        }

        itemRepository.saveAll(items);
        logger.info("Saved {} items to the database.", items.size());
    }

    /**
     * Workbook에서 거래 데이터를 저장하는 메서드 (두 번째 시트)
     * @param workbook 엑셀 파일의 Workbook 객체
     */
    public void saveTransactionsFromWorkbook(Workbook workbook) {
        Sheet transactionSheet = workbook.getSheetAt(1);
        List<TransactionRecord> transactions = new ArrayList<>();

        // 아이템 정보를 미리 로드하여 ID로 빠르게 조회할 수 있도록 Map에 저장
        List<Item> items = itemRepository.findAll();
        Map<String, Item> itemMap = new HashMap<>();
        for (Item item : items) {
            itemMap.put(item.getId(), item);
        }

        for (Row row : transactionSheet) {
            if (row.getRowNum() == 0) continue; // 헤더 스킵

            String itemId = getStringCellValue(row.getCell(1));
            LocalDate transactionDate = parseDate(row.getCell(2));
            String transactionType = getStringCellValue(row.getCell(3));

            // 수량 설정
            Double quantityDouble = getNumericCellValue(row.getCell(4));
            Integer quantity = (quantityDouble != null) ? quantityDouble.intValue() : null;

            // 필수 필드 체크
            if (itemId == null || transactionDate == null || transactionType == null || quantity == null) {
                logger.warn("Skipping row {}: Missing required transaction data", row.getRowNum());
                continue;
            }

            try {
                Item item = itemMap.get(itemId);
                if (item == null) {
                    logger.warn("Item with ID {} not found. Skipping row {}", itemId, row.getRowNum());
                    continue;
                }

                // TransactionRecord 객체 생성
                TransactionRecord transaction = new TransactionRecord();
                transaction.setItem(item);
                transaction.setTransactionDate(transactionDate);
                transaction.setTransactionType(transactionType);

                // 아이템의 purchasePrice와 sellPrice를 그대로 가져옴
                transaction.setPurchasePrice(item.getPurchasePrice());
                transaction.setSellPrice(item.getSellPrice());

                Double totalPrice;

                // 거래 유형에 따라 가격 및 수량 설정
                if ("purchase".equalsIgnoreCase(transactionType)) {
                    transaction.setPurchaseQuantity(quantity);
                    totalPrice = item.getPurchasePrice() * quantity;
                } else if ("sale".equalsIgnoreCase(transactionType)) {
                    transaction.setSellQuantity(quantity);
                    totalPrice = item.getSellPrice() * quantity;
                } else {
                    logger.warn("Unknown transaction type '{}' in row {}", transactionType, row.getRowNum());
                    continue;
                }

                transaction.setTotalPrice(totalPrice);
                transactions.add(transaction);
            } catch (Exception e) {
                logger.warn("Error parsing transaction row at row {}", row.getRowNum(), e);
            }
        }

        // 거래 데이터를 최신 순으로 정렬 (transactionDate 기준)
        transactions.sort((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()));

        if (!transactions.isEmpty()) {
            transactionRecordRepository.saveAll(transactions);
            logger.info("Saved {} transactions to the database.", transactions.size());
        } else {
            logger.warn("No transactions to save.");
        }
    }




    /**
     * 날짜 셀을 LocalDate로 변환하는 메서드
     * @param cell 날짜가 포함된 셀
     * @return LocalDate 객체 (형식이 잘못된 경우 null)
     */
    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else {
            try {
                return LocalDate.parse(getStringCellValue(cell), DATE_FORMATTER);
            } catch (Exception e) {
                logger.warn("Invalid date format: {}", cell);
                return null;
            }
        }
    }

    /**
     * 셀에서 String 값을 추출하는 메서드
     * @param cell 값이 포함된 셀
     * @return 셀의 문자열 값 (null일 경우 null 반환)
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * 셀에서 Numeric 값을 추출하는 메서드
     * @param cell 숫자 값이 포함된 셀
     * @return 셀의 숫자 값 (null일 경우 null 반환)
     */
    private Double getNumericCellValue(Cell cell) {
        if (cell == null) return null;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : null;
    }
}
