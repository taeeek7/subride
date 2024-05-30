package com.subride.subrecommend.infra.out.adapter;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.infra.exception.InfraException;
import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import com.subride.subrecommend.biz.usecase.outport.ISubRecommendProvider;
import com.subride.subrecommend.infra.dto.CategorySpendingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubRecommendProviderImpl implements ISubRecommendProvider {
    private final ISpendingRepository spendingRepository;
    private final ICategoryRepository categoryRepository;
    private final ISubRepository subRepository;

    @Override
    public List<Category> getAllCategories() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        return categoryEntities.stream()
                .map(CategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getSpendingByCategory(String userId) {
        return spendingRepository.getSpendingByCategory(userId)
                .stream()
                .collect(Collectors.toMap(CategorySpendingDTO::getCategory, CategorySpendingDTO::getAmount));
    }

    @Override
    public Category getCategoryBySpendingCategory(String spendingCategory) {
        CategoryEntity categoryEntity = categoryRepository.findBySpendingCategory(spendingCategory)
                .orElseThrow(() -> new InfraException("Category not found"));
        return categoryEntity.toDomain();
    }

    @Override
    public List<Sub> getSubListByCategoryId(String categoryId) {
        List<SubEntity> subEntities = subRepository.findByCategory_CategoryIdOrderByName(categoryId);
        return subEntities.stream()
                .map(SubEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sub> getSubListByIds(List<Long> subIds) {
        List<SubEntity> subEntities = subRepository.findAllById(subIds);
        return subEntities.stream()
                .map(SubEntity::toDomain)
                .collect(Collectors.toList());
    }
}
