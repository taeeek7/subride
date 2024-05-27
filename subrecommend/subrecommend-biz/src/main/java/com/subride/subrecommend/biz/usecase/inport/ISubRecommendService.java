package com.subride.subrecommend.biz.usecase.inport;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;

import java.util.List;

public interface ISubRecommendService {
    CategoryInfoDTO getRecommendCategoryBySpending(String userId);
    List<SubInfoDTO> getRecommendSubListByCategory(Long categoryId);
}