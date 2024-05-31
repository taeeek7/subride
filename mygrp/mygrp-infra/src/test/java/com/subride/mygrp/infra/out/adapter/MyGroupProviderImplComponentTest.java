// File: mygrp/mygrp-infra/src/test/java/com/subride/mygrp/infra/out/adapter/MyGroupProviderImplComponentTest.java
package com.subride.mygrp.infra.out.adapter;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.infra.common.util.TestDataGenerator;
import com.subride.mygrp.infra.exception.InfraException;
import com.subride.mygrp.infra.out.entity.GroupEntity;
import com.subride.mygrp.infra.out.feign.MemberFeignClient;
import com.subride.mygrp.infra.out.feign.MySubFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///mygrp",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
public class MyGroupProviderImplComponentTest {
    @Autowired
    private IMyGroupRepository myGroupRepository;
    @MockBean
    private SubRecommendFeignClient subRecommendFeignClient;
    @MockBean
    private MySubFeignClient mySubFeignClient;
    @MockBean
    private MemberFeignClient memberFeignClient;

    private MyGroupProviderImpl myGroupProvider;
    private Long testGroupId;

    @BeforeEach
    void setup() {
        cleanup();

        myGroupProvider = new MyGroupProviderImpl(myGroupRepository, subRecommendFeignClient, mySubFeignClient, memberFeignClient);
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
    void getMyGroupByGroupId_ValidGroupId_ReturnGroup() {
        // Given
        Long groupId = testGroupId;
        String userId = "user01";

        ResponseDTO<SubInfoDTO> subInfoResponse = TestDataGenerator.generateResponseDTO(200, new SubInfoDTO());
        subInfoResponse.getResponse().setSubId(1L);     //썹그룹의 구독서비스ID를 셋팅해야 함
        ResponseDTO<List<MemberInfoDTO>> memberInfoResponse = TestDataGenerator.generateResponseDTO(200, List.of(new MemberInfoDTO()));
        memberInfoResponse.getResponse().get(0).setUserId("user01");

        given(subRecommendFeignClient.getSubDetail(any())).willReturn(subInfoResponse);
        given(memberFeignClient.getMemberInfoList(any())).willReturn(memberInfoResponse);

        // When
        myGroupProvider.getMyGroupByGroupId(groupId);

        // Then
        Optional<GroupEntity> groupEntity = myGroupRepository.findById(groupId);
        assertThat(groupEntity).isPresent();
        assertThat(groupEntity.get().getGroupId()).isEqualTo(groupId);
    }

    @Test
    void getMyGroupListByUserId_ValidUserId_ReturnGroupList() {
        // Given
        String userId = "user01";

        ResponseDTO<List<SubInfoDTO>> subInfoResponse = TestDataGenerator.generateResponseDTO(200, List.of(new SubInfoDTO()));
        given(subRecommendFeignClient.getSubInfoListByIds(any())).willReturn(subInfoResponse);

        // When
        List<GroupEntity> groupEntities = myGroupRepository.findByMemberIdsContaining(userId);

        // Then
        assertThat(groupEntities).isNotEmpty();
        assertThat(groupEntities.get(0).getMemberIds()).contains(userId);
    }

    @Test
    void createMyGroup_ValidGroup_SaveGroup() {
        // Given
        GroupEntity groupEntity = TestDataGenerator.generateGroupEntity();

        // When
        String inviteCode = myGroupProvider.createMyGroup(groupEntity.toDomain());

        // Then
        Optional<GroupEntity> savedGroupEntity = myGroupRepository.findByInviteCode(inviteCode);
        assertThat(savedGroupEntity).isPresent();
        assertThat(savedGroupEntity.get().getGroupName()).isEqualTo(groupEntity.getGroupName());
    }

    @Test
    void joinMyGroup_ValidGroupJoinDTO_SaveGroup() {
        // Given
        GroupJoinDTO groupJoinDTO = new GroupJoinDTO();
        groupJoinDTO.setInviteCode(TestDataGenerator.generateRandomInviteCode());
        groupJoinDTO.setUserId("newUser");

        GroupEntity groupEntity = TestDataGenerator.generateGroupEntity();
        groupEntity.setInviteCode(groupJoinDTO.getInviteCode());
        myGroupRepository.save(groupEntity);

        ResponseDTO<Boolean> subscriptionResponse = TestDataGenerator.generateResponseDTO(200, true);
        ResponseDTO<Void> subscribeResponse = TestDataGenerator.generateResponseDTO(200, null);

        given(mySubFeignClient.checkSubscription(any(), any())).willReturn(subscriptionResponse);
        given(mySubFeignClient.subscribeSub(any(), any())).willReturn(subscribeResponse);

        // When
        myGroupProvider.joinMyGroup(groupJoinDTO);

        // Then
        Optional<GroupEntity> updatedGroupEntity = myGroupRepository.findByInviteCode(groupJoinDTO.getInviteCode());
        assertThat(updatedGroupEntity).isPresent();
        assertThat(updatedGroupEntity.get().getMemberIds()).contains(groupJoinDTO.getUserId());
    }

    @Test
    void leaveMyGroup_ValidGroupIdAndUserId_RemoveUserFromGroup() {
        // Given
        String userId = "user01";

        GroupEntity groupEntity = TestDataGenerator.generateGroupEntity();
        groupEntity.getMemberIds().add(userId);
        GroupEntity savedGroupEntity = myGroupRepository.save(groupEntity);

        Long groupId = savedGroupEntity.getGroupId();

        // When
        myGroupProvider.leaveMyGroup(groupId, userId);

        // Then
        Optional<GroupEntity> updatedGroupEntity = myGroupRepository.findById(groupId);
        assertThat(updatedGroupEntity).isPresent();
        assertThat(updatedGroupEntity.get().getMemberIds()).doesNotContain(userId);
    }

    @Test
    void leaveMyGroup_InvalidGroupId_ThrowInfraException() {
        // Given
        Long groupId = 999L;
        String userId = "user01";

        // When, Then
        assertThatThrownBy(() -> myGroupProvider.leaveMyGroup(groupId, userId))
                .isInstanceOf(InfraException.class)
                .hasMessage("썹 그룹을 찾을 수 없습니다.");
    }
}