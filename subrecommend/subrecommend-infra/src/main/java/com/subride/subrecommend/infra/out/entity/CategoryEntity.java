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

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "category_name")
    private String categoryName;

    private String spendingCategory;

    public CategoryEntity(String categoryId, String categoryName, String spendingCategory) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.spendingCategory = spendingCategory;
    }

    public Category toDomain() {
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName(categoryName);
        category.setSpendingCategory(spendingCategory);
        return category;
    }
}