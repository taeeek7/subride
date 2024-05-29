package com.subride.mysub.infra.out.adapter;

import com.subride.mysub.biz.domain.MySub;
import com.subride.mysub.biz.usecase.outport.IMySubProvider;
import com.subride.mysub.infra.exception.InfraException;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySubProviderImpl implements IMySubProvider {
    private final IMySubRepository mySubRepository;

    @Override
    public List<MySub> getMySubList(String userId) {
        List<MySubEntity> mySubEntityList = mySubRepository.findByUserId(userId);
        return mySubEntityList.stream()
                .map(MySubEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelSub(Long subId, String userId) {
        MySubEntity mySubEntity = mySubRepository.findByUserIdAndSubId(userId, subId)
                .orElseThrow(() -> new InfraException("구독 정보가 없습니다."));
        mySubRepository.delete(mySubEntity);
    }

    @Override
    public void subscribeSub(Long subId, String userId) {
        MySubEntity mySubEntity = MySubEntity.builder()
                .userId(userId)
                .subId(subId)
                .build();
        mySubRepository.save(mySubEntity);
    }

    @Override
    public boolean isSubscribed(String userId, Long subId) {
        return mySubRepository.existsByUserIdAndSubId(userId, subId);
    }
}