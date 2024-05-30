package com.subride.mygrp.biz.usecase.outport;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupJoinDTO;

import java.util.List;

public interface IMyGroupProvider {
    Group getMyGroupByGroupId(Long groupId);
    List<Group> getMyGroupListByUserId(String userId);
    String createMyGroup(Group group);
    void joinMyGroup(GroupJoinDTO groupJoinDTO);
    void leaveMyGroup(Long groupId, String userId);
}