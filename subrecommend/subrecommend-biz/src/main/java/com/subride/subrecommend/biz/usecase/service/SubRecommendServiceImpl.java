package com.subride.subrecommend.biz.usecase.service;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.biz.usecase.inport.ISubRecommendService;
import com.subride.subrecommend.biz.usecase.outport.ISubRecommendProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubRecommendServiceImpl implements ISubRecommendService {
    private final ISubRecommendProvider subRecommendProvider;

    @Override
    public CategoryInfoDTO getRecommendCategoryBySpending(String userId) {
        Map<String, Long> spendingByCategory = subRecommendProvider.getSpendingByCategory(userId);
        String maxSpendingCategory = spendingByCategory.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        Category category = subRecommendProvider.getCategoryBySpendingCategory(maxSpendingCategory);

        CategoryInfoDTO categoryInfoDTO = new CategoryInfoDTO();
        categoryInfoDTO.setCategoryId(category.getId());
        categoryInfoDTO.setCategoryName(category.getName());
        categoryInfoDTO.setTotalSpending(spendingByCategory.get(maxSpendingCategory));

        return categoryInfoDTO;
    }

    @Override
    public List<SubInfoDTO> getRecommendSubListByCategory(Long categoryId) {
        List<Sub> subList = subRecommendProvider.getSubListByCategoryId(categoryId);

        return subList.stream()
                .map(this::toSubInfoDTO)
                .collect(Collectors.toList());
    }

    private SubInfoDTO toSubInfoDTO(Sub sub) {
        SubInfoDTO subInfoDTO = new SubInfoDTO();
        subInfoDTO.setId(sub.getId());
        subInfoDTO.setName(sub.getName());
        subInfoDTO.setDescription(sub.getDescription());
        subInfoDTO.setFee(sub.getFee());
        subInfoDTO.setMaxShareNum(sub.getMaxShareNum());

        return subInfoDTO;
    }
}
