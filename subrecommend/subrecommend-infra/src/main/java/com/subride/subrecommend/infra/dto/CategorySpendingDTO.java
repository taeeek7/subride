package com.subride.subrecommend.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CategorySpendingDTO {
    private String category;
    private Long amount;
/*
    public CategorySpendingDTO(@Value("#{target.category}") String category,
                               @Value("#{target.amount}") Long amount) {
        this.category = category;
        this.amount = amount;
    }

 */
}
