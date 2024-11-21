package com.spring.sap.controller;

import com.spring.sap.entity.TransactionRecord;
import com.spring.sap.repository.TransactionRecordRepository;
import org.springframework.format.annotation.DateTimeFormat;
import com.spring.sap.entity.Item;
import com.spring.sap.repository.ItemRepository;
import com.spring.sap.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.io.InputStream;

// Controller 클래스, 페이지 이동 및 데이터를 처리하는 역할
@Controller
public class PageController {

    // Excel 파일 처리를 위한 서비스
    @Autowired
    private ExcelService excelService;

    // Item 엔티티 데이터 접근을 위한 Repository
    @Autowired
    private ItemRepository itemRepository;

    // TransactionRecord 엔티티 데이터 접근을 위한 Repository
    @Autowired
    private TransactionRecordRepository transactionRepository;

    /**
     * 메인 페이지로 이동하는 메서드
     * @return index 템플릿 이름
     */
    @GetMapping("/")
    public String mainPage() {
        return "index";
    }

    /**
     * 아이템 관리 페이지로 이동하는 메서드
     * @return items_home 템플릿 이름
     */
    @GetMapping("/items")
    public String itemsHome() {
        return "items_home";
    }

    /**
     * 아이템 목록을 조회하고 모델에 추가하여 반환
     * @param model 템플릿으로 데이터 전달을 위한 모델 객체
     * @return items 템플릿 이름
     */
    @GetMapping("/items/list")
    public String listItems(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "items";
    }

    /**
     * 파일 업로드 페이지로 이동
     * @param model 메시지 출력을 위한 모델 객체
     * @return upload 템플릿 이름
     */
    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("message", "엑셀 파일을 선택해 주세요.");
        return "upload";
    }

    /**
     * 파일 업로드 후 Excel 데이터를 처리하고 저장
     * @param file 업로드된 파일
     * @param model 메시지 출력을 위한 모델 객체
     * @return 성공/실패 메시지와 상태 코드 반환
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file, Model model) {
        try (InputStream inputStream = file.getInputStream()) {
            excelService.processExcelData(inputStream);
            model.addAttribute("message", "Excel file processed successfully!");
            return new ResponseEntity<>("Excel file processed successfully!", HttpStatus.OK);
        } catch (Exception e) {
            model.addAttribute("message", "Failed to process Excel file.");
            return new ResponseEntity<>("Failed to process Excel file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 재고 관리 페이지로 이동
     * @return stock_management 템플릿 이름
     */
    @GetMapping("/stock_management")
    public String stockManagementPage() {
        return "stock_management";
    }

    /**
     * 거래 목록 페이지로 이동하며, 모든 거래 기록을 모델에 추가하여 반환
     * @param model 거래 목록을 추가할 모델 객체
     * @return transaction_list 템플릿 이름
     */
    @GetMapping("/transaction_list")
    public String transactionListPage(Model model) {
        List<TransactionRecord> transactions = transactionRepository.findAll();
        model.addAttribute("transactions", transactions);
        return "transaction_list";
    }
    
    /**
     * 필터 조건에 따라 거래 목록을 조회하고, 모델에 추가하여 반환
     * @param month 월
     * @param parts 부품명
     * @param maker 제조사
     * @param profitPositive 수익 여부
     * @param model 필터링된 거래 목록을 추가할 모델 객체
     * @return transaction_list 템플릿 이름
     */
    @GetMapping("/items/filter")
    public String filterTransactions(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "parts", required = false) String parts,
            @RequestParam(name = "maker", required = false) String maker,
            @RequestParam(name = "profitPositive", required = false) Boolean profitPositive,
            Model model) {

        List<TransactionRecord> transactions = transactionRepository.findFilteredTransactions(month, parts, maker, profitPositive);
        model.addAttribute("transactions", transactions);
        return "transaction_list";
    }

    /**
     * 수동 거래 등록 페이지로 이동
     * @return manual_transaction 템플릿 이름
     */
    @GetMapping("/manual_transaction")
    public String manualTransactionPage() {
        return "manual_transaction";
    }

    /**
     * 새로운 거래를 추가하고 저장
     * @param itemId 아이템 ID
     * @param transactionType 거래 유형 (구매 또는 판매)
     * @param quantity 거래 수량
     * @param transactionDate 거래 날짜
     * @return 거래 목록 페이지로 리다이렉트
     */
    @PostMapping("/addTransaction")
    public String addTransaction(@RequestParam String itemId,
                                 @RequestParam String transactionType,
                                 @RequestParam Integer quantity,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate transactionDate) {
        // Item을 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Item ID"));

        // 새로운 거래 기록 생성 및 설정
        TransactionRecord transaction = new TransactionRecord();
        transaction.setItem(item);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(transactionDate);

        // 거래 유형에 따른 가격 및 수량 설정
        if (transactionType.equals("purchase")) {
            if (item.getPurchasePrice() != null && quantity != null) {
                transaction.setPurchaseQuantity(quantity);
                transaction.setPurchasePrice(item.getPurchasePrice());
                transaction.setTotalPrice(item.getPurchasePrice() * quantity);
            } else {
                System.out.println("Purchase Price or Quantity is NULL for Item ID: " + itemId);
            }
        } else if (transactionType.equals("sale")) {
            if (item.getSellPrice() != null && quantity != null) {
                transaction.setSellQuantity(quantity);
                transaction.setSellPrice(item.getSellPrice());
                transaction.setTotalPrice(item.getSellPrice() * quantity);
            } else {
                System.out.println("Sell Price or Quantity is NULL for Item ID: " + itemId);
            }
        }

        // 거래 정보를 DB에 저장
        transactionRepository.save(transaction);
        return "redirect:/transaction_list";
    }

    /**
     * 아이템과 거래 목록을 전체 삭제하고 AUTO_INCREMENT 초기화
     * @return 아이템 목록 페이지로 리다이렉트
     */
    @PostMapping("/items/deleteAll")
    public String deleteAllItems() {
        transactionRepository.deleteAll();
        transactionRepository.resetAutoIncrement();
        itemRepository.deleteAll();
        itemRepository.resetAutoIncrement();
        return "redirect:/items/list";
    }
}
