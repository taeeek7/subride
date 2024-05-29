package com.subride.mygrp.biz.usecase.inport;

import com.subride.mygrp.biz.dto.MyGroupCreateDTO;
import com.subride.mygrp.biz.dto.MyGroupDetailDTO;
import com.subride.mygrp.biz.dto.MyGroupJoinDTO;
import com.subride.mygrp.biz.dto.MyGroupSummaryDTO;

import java.util.List;

public interface IMyGroupService {
    List<MyGroupSummaryDTO> getMyGroupSummaryList(String userId);
    MyGroupDetailDTO getMyGroupDetail(Long myGroupId);
    void createMyGroup(MyGroupCreateDTO myGroupCreateDTO);
    void joinMyGroup(MyGroupJoinDTO myGroupJoinDTO);
    void leaveMyGroup(Long myGroupId, String userId);
    Long getTotalSubscriptionAmount(String userId);
    Long getMaxDiscountAmount(String userId);
}