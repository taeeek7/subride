package com.subride.mysub.biz.usecase.outport;

import com.subride.common.dto.MySubInfoDTO;

import java.util.List;

public interface IMySubProvider {

    List<MySubInfoDTO> getMySubList(String userId);
    void cancelSub(Long subId, String userId);
    void subscribeSub(Long subId, String userId);

    boolean isSubscribed(String userId, Long subId);
}