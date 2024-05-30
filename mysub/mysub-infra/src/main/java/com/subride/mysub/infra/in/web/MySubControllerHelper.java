package com.subride.mysub.infra.in.web;

import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySubControllerHelper {
    private final IMySubRepository mySubRepository;

    public List<Long> getMySubIds(String userId) {
        List<MySubEntity> mySubEntityList = mySubRepository.findByUserId(userId);
        return mySubEntityList.stream()
                .map(MySubEntity::getSubId)
                .collect(Collectors.toList());
    }
}