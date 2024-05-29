package com.subride.mysub.infra.out.repo;

import com.subride.mysub.infra.out.entity.MySubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IMySubRepository extends JpaRepository<MySubEntity, Long> {
    List<MySubEntity> findByUserId(String userId);
    Optional<MySubEntity> findByUserIdAndSubId(String userId, Long subId);
    boolean existsByUserIdAndSubId(String userId, Long subId);
}