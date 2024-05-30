package com.subride.subrecommend.biz.usecase.inport;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;

import java.util.List;

public interface ISubRecommendService {
    List<CategoryInfoDTO> getAllCategories();
    CategoryInfoDTO getRecommendCategoryBySpending(String userId);
    List<SubInfoDTO> getRecommendSubListByCategory(String categoryId);

    List<SubInfoDTO> getSubInfoListByIds(List<Long> subIds);
}