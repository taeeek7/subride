package com.subride.subrecommend.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryInfoDTO {
    private Long categoryId;
    private String categoryName;
    private Long totalSpending;
}