package com.subride.subrecommend.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryInfoDTO {
    private String categoryId;
    private String categoryName;
    private String spendingCategory;
    private Long totalSpending;
}