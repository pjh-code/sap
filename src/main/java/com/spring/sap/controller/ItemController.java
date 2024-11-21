package com.spring.sap.controller;

import com.spring.sap.entity.TransactionRecord;
import com.spring.sap.entity.Item;
import com.spring.sap.service.ItemService;
import com.spring.sap.repository.ItemRepository;
import com.spring.sap.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.List;

// @RestController를 사용하여 RESTful API를 제공하는 ItemController 클래스 설정
@RestController
@RequestMapping("/api/items")
public class ItemController {

    // Excel 파일 업로드 및 처리 서비스
    @Autowired
    private ExcelService excelService;

    // Item 엔티티의 데이터 접근을 위한 Repository
    @Autowired
    private ItemRepository itemRepository;

    /**
     * 엑셀 파일을 업로드하여 아이템 데이터를 데이터베이스에 저장하는 메서드
     * @param file 업로드할 엑셀 파일
     * @return 성공 또는 실패 메시지와 HTTP 상태 코드
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadItemsFromExcel(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // 엑셀 데이터 처리 서비스 호출
            excelService.processExcelData(inputStream); 
            return new ResponseEntity<>("Excel file processed and items saved successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to process Excel file for items", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
 // 아이템 관련 비즈니스 로직 처리 서비스
    @Autowired
    private ItemService itemService;
    
    /**
     * 필터 조건에 따라 거래 기록을 조회하는 메서드
     * @param month 조회할 월 (optional)
     * @param parts 부품명 (optional)
     * @param maker 제조사 (optional)
     * @param profitPositive 수익이 양수인지 여부 (optional)
     * @return 필터링된 거래 기록 리스트
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TransactionRecord>> filterTransactions(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String parts,
            @RequestParam(required = false) String maker,
            @RequestParam(required = false) Boolean profitPositive) {

        List<TransactionRecord> transactions = itemService.getFilteredTransactions(month, parts, maker, profitPositive);
        return ResponseEntity.ok(transactions);
    }

    /**
     * 개별 아이템을 등록하는 메서드
     * @param item 클라이언트에서 전송된 아이템 데이터
     * @return 등록된 아이템과 HTTP 상태 코드
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerItem(@RequestBody Item item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            return new ResponseEntity<>("ID 필드는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        
        // 아이템 저장
        Item savedItem = itemRepository.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }
    
    
    
    /**
     * 모든 아이템 목록을 조회하는 메서드
     * @return Item 엔티티 리스트
     */
    @GetMapping("/list")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    /**
     * 아이템 관리 페이지로 이동하는 메서드
     * @return items_home 템플릿을 반환
     */
    @GetMapping("/items")
    public String itemsHome() {
        return "items_home";
    }

    /**
     * 아이템 목록 페이지로 이동하는 메서드
     * @return items 템플릿을 반환
     */
    @GetMapping("/items/list")
    public String viewItems() {
        return "items";
    }

    /**
     * 엑셀 업로드 페이지로 이동하는 메서드
     * @return upload 템플릿을 반환
     */
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }
}
