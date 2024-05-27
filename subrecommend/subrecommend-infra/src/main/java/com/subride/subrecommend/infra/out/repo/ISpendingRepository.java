package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.dto.CategorySpendingDTO;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ISpendingRepository extends JpaRepository<SpendingEntity, Long> {
    /*
    JPQL(Java Persistence Query Language) 사용

    쿼리 결과를 CategorySpendingDTO 객체로 매핑 하기 위해
    CategorySpendingDTO에 @AllArgsConstructor로 생성자를 만들고,
    JPQL에 'new' 키워드로 CategorySpendingDTO 객체가 생성되게 함
    */
    @Query("SELECT new com.subride.subrecommend.infra.dto.CategorySpendingDTO(s.category, SUM(s.amount)) FROM SpendingEntity s WHERE s.userId = :userId GROUP BY s.category")
    List<CategorySpendingDTO> getSpendingByCategory(String userId);
}