package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.out.entity.SubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ISubRepository extends JpaRepository<SubEntity, Long> {
    List<SubEntity> findByCategory_CategoryIdOrderByName(String categoryId);
    Optional<SubEntity> findByName(String name);
}

