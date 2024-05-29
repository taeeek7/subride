package com.subride.mysub.biz.usecase.inport;

import com.subride.mysub.biz.dto.MySubDTO;

import java.util.List;

public interface IMySubService {
    List<MySubDTO> getMySubList(String userId);
    void cancelSub(Long subId, String userId);
    void subscribeSub(Long subId, String userId);

    boolean checkSubscription(String userId, Long subId);
}