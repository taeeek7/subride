package com.subride.mygrp.biz.usecase.outport;

import com.subride.mygrp.biz.domain.MyGroup;

import java.util.List;

public interface IMyGroupProvider {
    MyGroup getMyGroupByMyGroupId(Long myGroupId);
    MyGroup getMyGroupByInviteCode(String invideCode);
    List<MyGroup> getMyGroupListByUserId(String userId);
    boolean existsByMyGroupIdAndUserId(Long myGroupId, String userId);
    void saveMyGroup(MyGroup myGroup);
    void deleteMyGroupUser(Long myGroupId, String userId);
    Long calculateTotalSubscriptionAmount(String userId);
    Long calculateMaxDiscountAmount(String userId);
    boolean isSubscribed(String userId, Long subId);
    boolean subscribeSub(Long subId, String userId);
}