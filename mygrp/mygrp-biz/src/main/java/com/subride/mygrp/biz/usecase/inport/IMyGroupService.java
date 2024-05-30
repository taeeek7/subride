package com.subride.mygrp.biz.usecase.inport;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;

import java.util.List;

public interface IMyGroupService {
    List<Group> getMyGroupSummaryList(String userId);
    Group getMyGroupDetail(Long myGroupId, String userId);
    void createMyGroup(GroupCreateDTO groupCreateDTO);
    void joinMyGroup(GroupJoinDTO groupJoinDTO);
    void leaveMyGroup(Long myGroupId, String userId);
}