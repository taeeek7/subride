package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByCategoryId(String categoryId);
    Optional<CategoryEntity> findBySpendingCategory(String spendingCategory);
}