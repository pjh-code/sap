package com.spring.sap.repository;

import com.spring.sap.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {

    /**
     * 특정 이름과 제조사를 기반으로 아이템 존재 여부를 확인하는 메서드
     * @param name 아이템 이름
     * @param maker 제조사
     * @return 아이템이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByNameAndMaker(String name, String maker);

    /**
     * AUTO_INCREMENT를 초기화하는 메서드 (MySQL 전용)
     * 아이템 테이블의 AUTO_INCREMENT 값을 1로 리셋하여 테이블의 ID 증가값을 초기화함
     */
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE item AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}
