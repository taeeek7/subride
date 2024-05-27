package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository     //생략해도 됨. Spring Data JPA가 자동으로 Bin객체로 생성해 줌
public interface IAccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUserId(String userId);
    Boolean existsByUserId(String userId);
}