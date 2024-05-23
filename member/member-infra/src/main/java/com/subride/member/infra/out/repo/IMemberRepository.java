package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IMemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUserId(String userId);
    List<MemberEntity> findByUserIdIn(List<String> userIdList);
}