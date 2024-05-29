package com.subride.mygrp.infra.out.repo;

import com.subride.mygrp.infra.out.entity.MyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMyGroupRepository extends JpaRepository<MyGroupEntity, Long> {
    List<MyGroupEntity> findByMemberIdsContaining(String userId);
    boolean existsByMyGroupIdAndMemberIdsContaining(Long myGroupId, String userId);
    void deleteById(Long id);
    MyGroupEntity findByInviteCode(String inviteCode);
}