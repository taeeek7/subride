package com.subride.mysub.biz.usecase.service;

import com.subride.common.dto.MySubInfoDTO;
import com.subride.mysub.biz.domain.MySub;
import com.subride.mysub.biz.dto.MySubDTO;
import com.subride.mysub.biz.usecase.inport.IMySubService;
import com.subride.mysub.biz.usecase.outport.IMySubProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MySubServiceImpl implements IMySubService {
    private final IMySubProvider mySubProvider;

    @Override
    public List<MySubInfoDTO> getMySubList(String userId) {
        return mySubProvider.getMySubList(userId);
    }

    @Override
    public void cancelSub(Long subId, String userId) {
        mySubProvider.cancelSub(subId, userId);
    }

    @Override
    public void subscribeSub(Long subId, String userId) {
        mySubProvider.subscribeSub(subId, userId);
    }

    private MySubDTO toMySubDTO(MySub mySub) {
        MySubDTO mySubDTO = new MySubDTO();
        mySubDTO.setUserId(mySub.getUserId());
        mySubDTO.setSubId(mySub.getSubId());
        return mySubDTO;
    }

    @Override
    public boolean checkSubscription(String userId, Long subId) {
        return mySubProvider.isSubscribed(userId, subId);
    }
}