package com.subride.subrecommend.biz.usecase.outport;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;

import java.util.List;
import java.util.Map;

public interface ISubRecommendProvider {
    Map<String, Long> getSpendingByCategory(String userId);
    Category getCategoryBySpendingCategory(String spendingCategory);
    List<Sub> getSubListByCategoryId(Long categoryId);
}