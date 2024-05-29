package com.subride.mysub.biz.usecase.outport;

import com.subride.mysub.biz.domain.MySub;

import java.util.List;

public interface IMySubProvider {

    List<MySub> getMySubList(String userId);
    void cancelSub(Long subId, String userId);
    void subscribeSub(Long subId, String userId);

    boolean isSubscribed(String userId, Long subId);
}