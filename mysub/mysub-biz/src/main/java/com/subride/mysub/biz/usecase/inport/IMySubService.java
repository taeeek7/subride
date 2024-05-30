package com.subride.mysub.biz.usecase.inport;

import com.subride.common.dto.MySubInfoDTO;

import java.util.List;

public interface IMySubService {
    List<MySubInfoDTO> getMySubList(String userId);
    void cancelSub(Long subId, String userId);
    void subscribeSub(Long subId, String userId);

    boolean checkSubscription(String userId, Long subId);
}