// File: mygrp/mygrp-infra/src/test/java/com/subride/mygrp/infra/in/web/MyGroupControllerComponentTest.java
package com.subride.mygrp.infra.in.web;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.biz.usecase.service.MyGroupServiceImpl;
import com.subride.mygrp.biz.usecase.service.RandomValueGenerator;
import com.subride.mygrp.infra.common.config.SecurityConfig;
import com.subride.mygrp.infra.common.jwt.JwtTokenProvider;
import com.subride.mygrp.infra.common.util.TestDataGenerator;
import com.subride.mygrp.infra.out.adapter.MyGroupProviderImpl;
import com.subride.mygrp.infra.out.feign.MemberFeignClient;
import com.subride.mygrp.infra.out.feign.MySubFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@WebMvcTest(MyGroupController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class MyGroupControllerComponentTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private MyGroupControllerHelper myGroupControllerHelper;
    @SpyBean
    private MyGroupServiceImpl myGroupService;
    @SpyBean
    private RandomValueGenerator randomValueGenerator;

    @MockBean
    private MyGroupProviderImpl myGroupProvider;

    @MockBean
    private IMyGroupRepository myGroupRepository;
    @MockBean
    private SubRecommendFeignClient subRecommendFeignClient;
    @MockBean
    private MySubFeignClient mySubFeignClient;
    @MockBean
    private MemberFeignClient memberFeignClient;

    @Test
    @WithMockUser
    void getMyGroupList() throws Exception {
        // Given
        String userId = "user01";
        List<Group> groupList = new ArrayList<>();
        Group group = TestDataGenerator.createGroup();
        groupList.add(group);

        given(myGroupProvider.getMyGroupListByUserId(any())).willReturn(groupList);

        // When, Then
        mockMvc.perform(get("/api/my-groups?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response").isArray());
    }

    @Test
    @WithMockUser
    void getMyGroupDetail() throws Exception {
        // Given
        Long groupId = 1L;
        Group group = TestDataGenerator.createGroup();
        group.setGroupId(groupId);

        given(myGroupProvider.getMyGroupByGroupId(any())).willReturn(group);

        // When, Then
        mockMvc.perform(get("/api/my-groups/{groupId}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response.groupId").value(groupId));
    }

    @Test
    @WithMockUser
    void createMyGroup() throws Exception {
        // Given
        GroupCreateDTO groupCreateDTO = TestDataGenerator.generateGroupCreateDTO();

        given(myGroupProvider.createMyGroup(any())).willReturn("inviteCode");

        // When, Then
        mockMvc.perform(post("/api/my-groups")
                        .contentType("application/json")
                        .content("{\"groupName\":\"새로운 그룹\",\"subId\":1,\"leaderId\":\"newLeader\",\"bankName\":\"새 은행\",\"bankAccount\":\"1234-5678\",\"paymentDay\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("썹그룹 생성 완료"));
    }

    @Test
    @WithMockUser
    void joinMyGroup() throws Exception {
        // Given
        GroupJoinDTO groupJoinDTO = new GroupJoinDTO();
        groupJoinDTO.setInviteCode("inviteCode");
        groupJoinDTO.setUserId("newUser");

        doNothing().when(myGroupProvider).joinMyGroup(any());

        // When, Then
        mockMvc.perform(post("/api/my-groups/join")
                        .contentType("application/json")
                        .content("{\"inviteCode\":\"inviteCode\",\"userId\":\"newUser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("마이그룹 참여 성공"));
    }

    @Test
    @WithMockUser
    void leaveMyGroup() throws Exception {
        // Given
        Long groupId = 1L;
        String userId = "user01";

        doNothing().when(myGroupProvider).leaveMyGroup(any(), any());

        // When, Then
        mockMvc.perform(delete("/api/my-groups/{groupId}?userId={userId}", groupId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("마이그룹 탈퇴 성공"));
    }

    @Test
    @WithMockUser
    void getJoinSubIds() throws Exception {
        // Given
        String userId = "user01";
        List<Long> joinSubIds = List.of(1L, 2L, 3L);

        given(myGroupControllerHelper.getJoinSubIds(any())).willReturn(joinSubIds);

        // When, Then
        mockMvc.perform(get("/api/my-groups/sub-id-list?userId={userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("참여중인 썹그룹의 구독서비스ID 목록"))
                .andExpect(jsonPath("$.response").isArray());
    }
}