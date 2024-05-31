// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/in/web/MySubControllerComponentTest.java
package com.subride.mysub.infra.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.biz.dto.MySubDTO;
import com.subride.mysub.biz.usecase.service.MySubServiceImpl;
import com.subride.mysub.infra.common.config.SecurityConfig;
import com.subride.mysub.infra.common.jwt.JwtTokenProvider;
import com.subride.mysub.infra.common.util.TestDataGenerator;
import com.subride.mysub.infra.out.adapter.MySubProviderImpl;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.feign.SubRecommendFeignClient;
import com.subride.mysub.infra.out.repo.IMySubRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MySubController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class MySubControllerComponentTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private MySubControllerHelper mySubControllerHelper;
    @SpyBean
    private MySubServiceImpl mySubService;

    @MockBean
    private SubRecommendFeignClient subRecommendFeignClient;
    @MockBean
    private MySubProviderImpl mySubProvider;
    @MockBean
    private IMySubRepository mySubRepository;

    @Test
    @WithMockUser
    void getMySubList() throws Exception {
        // Given
        String userId = "user01";
        List<MySubDTO> mySubDTOList = new ArrayList<>();
        mySubDTOList.add(new MySubDTO());

        given(mySubProvider.getMySubList(any())).willReturn(new ArrayList<>());

        // SubRecommendFeignClient의 동작을 Mocking
        ResponseDTO<SubInfoDTO> responseDTO = TestDataGenerator.generateResponseDTO(200, new SubInfoDTO());
        given(subRecommendFeignClient.getSubDetail(any())).willReturn(responseDTO);

        // When, Then
        mockMvc.perform(get("/api/my-subs?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response").isArray());
    }

    @Test
    @WithMockUser
    void cancelSub() throws Exception {
        // Given
        Long subId = 1L;
        String userId = "user01";

        MySubEntity mySubEntity = new MySubEntity();
        given(mySubRepository.findByUserIdAndSubId(any(), any())).willReturn(Optional.of(mySubEntity));
        doNothing().when(mySubRepository).delete(any());

        // When, Then
        mockMvc.perform(delete("/api/my-subs/{subId}?userId={userId}", subId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser
    void subscribeSub() throws Exception {
        // Given
        Long subId = 1L;
        String userId = "user01";

        MySubEntity mySubEntity = new MySubEntity();
        given(mySubRepository.save(any())).willReturn(mySubEntity);

        // When, Then
        mockMvc.perform(post("/api/my-subs/{subId}?userId={userId}", subId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser
    void checkSubscription() throws Exception {
        // Given
        String userId = "user01";
        Long subId = 1L;

        given(mySubProvider.isSubscribed(any(), any())).willReturn(true);

        // When, Then
        mockMvc.perform(get("/api/my-subs/checking-subscribe?userId={userId}&subId={subId}", userId, subId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response").value(true));
    }
}