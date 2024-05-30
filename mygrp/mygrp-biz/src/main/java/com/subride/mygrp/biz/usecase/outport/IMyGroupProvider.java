package com.subride.mygrp.biz.usecase.outport;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupJoinDTO;

import java.util.List;

public interface IMyGroupProvider {
    Group getMyGroupByGroupId(Long groupId, String userId);
    List<Group> getMyGroupListByUserId(String userId);
    void createMyGroup(Group group);
    void joinMyGroup(GroupJoinDTO groupJoinDTO);
    void leaveMyGroup(Long groupId, String userId);
}