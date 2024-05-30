// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/in/web/MySubControllerSystemTest.java
package com.subride.mysub.infra.in.web;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.mysub.infra.common.util.TestDataGenerator;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.infra.out.adapter.MySubProviderImpl;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.feign.MyGroupFeignClient;
import com.subride.mysub.infra.out.feign.SubRecommendFeignClient;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@WithMockUser
public class MySubControllerSystemTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private IMySubRepository mySubRepository;

    private WebTestClient webClient;

    @MockBean
    private SubRecommendFeignClient subRecommendFeignClient;
    @MockBean
    private MyGroupFeignClient myGroupFeignClient;

    private MySubProviderImpl mySubProvider;
    private String testUserId = "user01";

    @BeforeEach
    void setup() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();
        mySubProvider = new MySubProviderImpl(mySubRepository, subRecommendFeignClient, myGroupFeignClient);

        cleanup();  //테스트 데이터 모두 지움

        List<MySubEntity> mySubEntities = TestDataGenerator.generateMySubEntities(testUserId);
        mySubRepository.saveAll(mySubEntities);
    }

    @AfterEach
    void cleanup() {
        mySubRepository.deleteAll();
    }

    @Test
    void getMySubList_ValidUser_ReturnMySubList() {
        // Given
        String url = "/api/my-subs?userId=" + testUserId;

        //-- Feign Client로 구독추천에 요청하는 수행을 Stubbing함
        ResponseDTO<List<GroupSummaryDTO>> myGroupListResponse = ResponseDTO.<List<GroupSummaryDTO>>builder()
                .code(200)
                .response(new ArrayList<>())
                .build();
        ResponseDTO<List<SubInfoDTO>> response = ResponseDTO.<List<SubInfoDTO>>builder()
                .code(200)
                .response(new ArrayList<>())
                .build();
        given(myGroupFeignClient.getMyGroupList(any())).willReturn(myGroupListResponse);
        given(subRecommendFeignClient.getSubInfoListByIds(any())).willReturn(response);

        // When & Then
        webClient.get().uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("구독 목록 조회 성공");
    }

    @Test
    void cancelSub_ValidUserAndSub_Success() {
        mySubProvider.subscribeSub(1L, testUserId);  //--테스트 데이터 등록

        // Given
        MySubEntity mySubEntity = mySubRepository.findByUserId(testUserId).get(0);
        Long subId = mySubEntity.getSubId();
        String url = "/api/my-subs/" + subId + "?userId=" + testUserId;
        ResponseDTO<List<Long>> response = ResponseDTO.<List<Long>>builder()
                .code(200)
                .response(new ArrayList<>())
                .build();
        given(myGroupFeignClient.getJoinSubIds(any())).willReturn(response);

        // When & Then
        webClient.delete().uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("구독 취소 성공");
    }

    @Test
    void subscribeSub_NewSub_Success() {
        // Given
        Long subId = 100L;
        String url = "/api/my-subs/" + subId + "?userId=" + testUserId;

        // When & Then
        webClient.post().uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("구독 추가 성공");
    }

    @Test
    void checkSubscription_SubscribedUser_ReturnTrue() {
        // Given
        MySubEntity mySubEntity = mySubRepository.findByUserId(testUserId).get(0);
        Long subId = mySubEntity.getSubId();
        String url = "/api/my-subs/checking-subscribe?userId=" + testUserId + "&subId=" + subId;

        // When & Then
        webClient.get().uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("구독 여부 확인 성공")
                .jsonPath("$.response").isEqualTo(true);
    }
}