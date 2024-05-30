package com.subride.mygrp.infra.out.repo;

import com.subride.mygrp.infra.out.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMyGroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByMemberIdsContaining(String userId);
    boolean existsByGroupIdAndMemberIdsContaining(Long myGroupId, String userId);
    void deleteByGroupId(Long groupId);
    GroupEntity findByInviteCode(String inviteCode);
    boolean existsByGroupNameAndSubId(String groupName, Long subId);
}