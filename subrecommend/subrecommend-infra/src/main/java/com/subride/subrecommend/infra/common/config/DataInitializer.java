package com.subride.subrecommend.infra.common.config;

import com.subride.subrecommend.infra.common.util.TestDataGenerator;
import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@SuppressWarnings("unused")
public class DataInitializer implements ApplicationRunner {
    private final ICategoryRepository categoryRepository;
    private final ISubRepository subRepository;
    private final ISpendingRepository spendingRepository;

    public DataInitializer(ICategoryRepository categoryRepository, ISubRepository subRepository, ISpendingRepository spendingRepository) {
        this.categoryRepository = categoryRepository;
        this.subRepository = subRepository;
        this.spendingRepository = spendingRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<CategoryEntity> categories = TestDataGenerator.generateCategoryEntities();
        categoryRepository.saveAll(categories);
        List<SubEntity> subs = TestDataGenerator.generateSubEntities(categories);
        subRepository.saveAll(subs);

        String[] userIds = {"user01", "user02", "user03", "user04", "user05"};
        String[] categoryNames = categories.stream().map(CategoryEntity::getSpendingCategory).toArray(String[]::new);
        List<SpendingEntity> spendings = TestDataGenerator.generateSpendingEntities(userIds, categoryNames);
        spendingRepository.saveAll(spendings);
    }

    @PreDestroy
    public void cleanData() {
        spendingRepository.deleteAll();
        subRepository.deleteAll();
        categoryRepository.deleteAll();
    }
}