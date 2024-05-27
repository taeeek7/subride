package com.subride.subrecommend.infra.out.entity;

import com.subride.subrecommend.biz.domain.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String spendingCategory;

    public CategoryEntity(String name, String spendingCategory) {
        this.name = name;
        this.spendingCategory = spendingCategory;
    }

    public Category toDomain() {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSpendingCategory(spendingCategory);
        return category;
    }
}