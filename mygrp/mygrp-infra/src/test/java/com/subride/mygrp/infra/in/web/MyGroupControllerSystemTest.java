// File: mygrp/mygrp-infra/src/test/java/com/subride/mygrp/infra/in/web/MyGroupControllerSystemTest.java
package com.subride.mygrp.infra.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.infra.common.util.TestDataGenerator;
import com.subride.mygrp.infra.out.entity.GroupEntity;
import com.subride.mygrp.infra.out.feign.MemberFeignClient;
import com.subride.mygrp.infra.out.feign.MySubFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@WithMockUser
public class MyGroupControllerSystemTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private IMyGroupRepository myGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubRecommendFeignClient subRecommendFeignClient;
    @MockBean
    private MySubFeignClient mySubFeignClient;
    @MockBean
    private MemberFeignClient memberFeignClient;

    private WebTestClient webClient;

    private Long testGroupId;

    @BeforeEach
    void setup() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())     //인증과 권한 부여 처리
                .configureClient()
                .build();

        cleanup();  //테스트 데이터 모두 지움

        List<GroupEntity> groupEntities = TestDataGenerator.generateGroupEntities();
        myGroupRepository.saveAll(groupEntities);
        GroupEntity groupEntity = myGroupRepository.findByGroupName("테스트그룹")
                .orElseThrow(() -> new RuntimeException("Group not found"));
        testGroupId = groupEntity.getGroupId();
    }

    @AfterEach
    void cleanup() {
        myGroupRepository.deleteAll();
    }

    @Test
    void getMyGroupList_ValidUser_ReturnMyGroupList() {
        // Given
        String userId = "user01";
        ResponseDTO<List<SubInfoDTO>> responseListSubInfoDTO = TestDataGenerator.generateResponseDTO(200, List.of(new SubInfoDTO()));
        responseListSubInfoDTO.getResponse().get(0).setSubId(1L);
        given(subRecommendFeignClient.getSubInfoListByIds(any())).willReturn(responseListSubInfoDTO);
        // When & Then
        webClient.get().uri("/api/my-groups?userId=" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("마이그룹 목록 조회 성공");
    }

    @Test
    void getMyGroupDetail_ValidGroupId_ReturnMyGroupDetail() {
        // Given
        Long groupId = testGroupId;

        ResponseDTO<SubInfoDTO> responseSubInfoDTO = TestDataGenerator.generateResponseDTO(200, new SubInfoDTO());
        responseSubInfoDTO.getResponse().setSubId(1L);
        given(subRecommendFeignClient.getSubDetail(any())).willReturn(responseSubInfoDTO);

        ResponseDTO<List<MemberInfoDTO>> responseListMemberInfoDTO = TestDataGenerator.generateResponseDTO(200, List.of(new MemberInfoDTO()));
        responseListMemberInfoDTO.getResponse().get(0).setUserId("user01");
        given(memberFeignClient.getMemberInfoList(any())).willReturn(responseListMemberInfoDTO);

        // When & Then
        webClient.get().uri("/api/my-groups/" + groupId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("마이그룹 상세 조회 성공")
                .jsonPath("$.response.groupId").isEqualTo(groupId);
    }

    @Test
    void createMyGroup_ValidGroupCreateDTO_Success() {
        // Given
        GroupCreateDTO groupCreateDTO = TestDataGenerator.generateGroupCreateDTO();

        // When & Then
        webClient.post().uri("/api/my-groups")
                .bodyValue(groupCreateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("썹그룹 생성 완료");
    }

    @Test
    void joinMyGroup_ValidGroupJoinDTO_Success() {
        // Given
        GroupJoinDTO groupJoinDTO = new GroupJoinDTO();
        groupJoinDTO.setInviteCode("inviteCode");
        groupJoinDTO.setUserId("newUser");

        GroupEntity groupEntity = TestDataGenerator.generateGroupEntity();
        groupEntity.setInviteCode(groupJoinDTO.getInviteCode());
        myGroupRepository.save(groupEntity);

        //-- 썹그룹 참여 시 해당 구독서비스 미가입했는지 체크하여 자동 등록하는 부분 스터빙
        ResponseDTO<Boolean> response1 = TestDataGenerator.generateResponseDTO(200, false);
        given(mySubFeignClient.checkSubscription(any(), any())).willReturn(response1);
        ResponseDTO<Void> response2 = TestDataGenerator.generateResponseDTO(200, null);
        given(mySubFeignClient.subscribeSub(any(), any())).willReturn(response2);

        // When & Then
        webClient.post().uri("/api/my-groups/join")
                .bodyValue(groupJoinDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("마이그룹 참여 성공");
    }

    @Test
    void leaveMyGroup_ValidGroupIdAndUserId_Success() {
        // Given
        Long groupId = testGroupId;
        String userId = "user01";

        // When & Then
        webClient.delete().uri("/api/my-groups/" + groupId + "?userId=" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("마이그룹 탈퇴 성공");
    }

    @Test
    void getJoinSubIds_ValidUserId_ReturnJoinSubIds() {
        // Given
        String userId = "user01";

        // When & Then
        webClient.get().uri("/api/my-groups/sub-id-list?userId=" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("참여중인 썹그룹의 구독서비스ID 목록")
                .jsonPath("$.response").isArray();
    }
}