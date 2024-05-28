package com.subride.subrecommend.infra.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.infra.common.util.TestDataGenerator;
import com.subride.subrecommend.infra.exception.InfraException;
import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SubRecommendControllerSystemTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private ISubRepository subRepository;

    @Autowired
    private ISpendingRepository spendingRepository;

    private WebTestClient webClient;

    private Long testSubId;

    @BeforeEach
    void setup() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();

       cleanup();  //테스트 데이터 모두 지움

        // 테스트 데이터 생성
        List<CategoryEntity> categories = TestDataGenerator.generateCategoryEntities();
        categoryRepository.saveAll(categories);

        List<SubEntity> subs = TestDataGenerator.generateSubEntities(categories);
        subRepository.saveAll(subs);
        SubEntity subEntity = subRepository.findByName("넷플릭스")
                .orElseThrow(() -> new InfraException("Category not found"));
        testSubId = subEntity.getId();

        // 지출 데이터 생성
        String[] userIds = {"user01", "user02", "user03", "user04", "user05"};
        String[] categoryNames = categories.stream().map(CategoryEntity::getSpendingCategory).toArray(String[]::new);
        List<SpendingEntity> spendings = TestDataGenerator.generateSpendingEntities(userIds, categoryNames);
        spendingRepository.saveAll(spendings);

    }

    @AfterEach
    void cleanup() {
        // 테스트 데이터 삭제
        spendingRepository.deleteAll();
        subRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void getRecommendCategory_success() {
        // Given
        String userId = "user01";

        // When & Then
        webClient.get().uri("/api/subrecommend/category?userId=" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDTO.class)
                .value(response -> {
                    assert response.getCode() == 200;

                    CategoryInfoDTO categoryInfo = objectMapper.convertValue(response.getResponse(), CategoryInfoDTO.class);
                    assert categoryInfo.getCategoryId() != null;
                    assert categoryInfo.getCategoryName() != null;
                    assert categoryInfo.getTotalSpending() != null;
                });
    }

    @Test
    @WithMockUser
    void getRecommendSubList_success() {
        // Given
        String categoryId = "life";

        // When & Then
        webClient.get().uri("/api/subrecommend/list?categoryId=" + categoryId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDTO.class)
                .value(response -> {
                    assert response.getCode() == 200;
                    //assert response.getMessage().equals("추천 구독 목록 조회 성공");

                    List<SubInfoDTO> subList = objectMapper.convertValue(response.getResponse(), List.class);
                    assert subList.size() > 0;
                });
    }

    @Test
    @WithMockUser
    void getSubDetail_success() {
        // Given
        Long subId = testSubId;

        // When & Then
        webClient.get().uri("/api/subrecommend/detail/" + subId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDTO.class)
                .value(response -> {
                    assert response.getCode() == 200;
                    //assert response.getMessage().equals("구독 상세 정보 조회 성공");

                    SubInfoDTO subInfo = objectMapper.convertValue(response.getResponse(), SubInfoDTO.class);
                    assert subInfo.getId().equals(subId);
                    assert subInfo.getName() != null;
                    assert subInfo.getDescription() != null;
                    assert subInfo.getFee() != null;
                    assert subInfo.getMaxShareNum() > 0;
                });
    }
}