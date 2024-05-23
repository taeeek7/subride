package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUserId(String userId);
    Boolean existsByUserId(String userId);
}