package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.biz.usecase.service.SubRecommendServiceImpl;
import com.subride.subrecommend.infra.common.config.SecurityConfig;
import com.subride.subrecommend.infra.common.jwt.JwtTokenProvider;
import com.subride.subrecommend.infra.out.adapter.SubRecommendProviderImpl;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
컴포넌트 테스트 예시: Controller 테스트(SpyBean객체 이용)
- 목적:
    - API End point 호출: Http 요청에 매핑된 API가 호출되는지 검증
    - 데이터 처리를 제외한 API 수행
    - 리턴값 검증: API 결과값이 잘 리턴되는지 테스트
- 방법:
    - MockMVC로 Http요청을 모방하고, @SpyBean객체로 데이터 처리를 제외한 API실행
    - 데이터 처리 관련한 객체는 @Mock을 이용하여 모의 객체로 생성
*/
@WebMvcTest(SubRecommendController.class)
/*
추가로 필요한 Bean객체 로딩
- Controller클래스에 Spring security가 적용되므로 SucurtyConfig를 Import하여 필요한 Bean객체를 로딩해야함
- JWT토큰 처리를 위해 JwtTokenProvider객체도 import해야 함
*/
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class SubRecommendControllerComponentTest {
    /*
    모의 Http 객체이며 Bocking방식을 지원함
    @WebMvcTest는 @Controller와 @ControllerAdvice로 생성된 Bean 클래스를 자동으로 생성함
    즉, AuthController 클래스는 자동으로 생성됨
    하지만 @Component, @Service, @Repository 등의 애노테이션이 붙은 클래스는 스캔하지 않기 때문에 자동 생성되지 않음
     */
    @Autowired
    private MockMvc mockMvc;

    //-- Controller 의존 객체 SpyBean으로 생성
    @SpyBean
    private SubRecommendControllerHelper subRecommendControllerHelper;
    @SpyBean
    private SubRecommendServiceImpl subRecommendService;

    //-- SubRecommendProviderImpl 생성을 위한 Mock Bean객체 생성
    @MockBean
    private SubRecommendProviderImpl subRecommendProvider;
    @MockBean
    private ISpendingRepository spendingRepository;
    @MockBean
    private ICategoryRepository categoryRepository;
    @MockBean
    private ISubRepository subRepository;

    /*
    @GetMapping("/category")
     public ResponseEntity<CategoryInfoDTO> getRecommendCategory(@RequestParam String userId) {
        CategoryInfoDTO categoryInfoDTO = subRecommendService.getRecommendCategoryBySpending(userId);
        return ResponseEntity.ok(categoryInfoDTO);
    }
    */
    @Test
    @WithMockUser
    void getRecommendCategory() throws Exception {
        //--Given
        String userId = "user01";

        /*
        public CategoryInfoDTO getRecommendCategoryBySpending(String userId) {
            Map<String, Long> spendingByCategory = subRecommendProvider.getSpendingByCategory(userId);
            ...

            Category category = subRecommendProvider.getCategoryBySpendingCategory(maxSpendingCategory);
            ...

            return categoryInfoDTO;
        }
         */
        Map<String, Long> spendingByCategory = new HashMap<>();
        spendingByCategory.put("Life", 10000L);
        spendingByCategory.put("Food", 20000L);

        Category category = CommonTestUtils.createCategory();

        given(subRecommendProvider.getSpendingByCategory(any())).willReturn(spendingByCategory);
        given(subRecommendProvider.getCategoryBySpendingCategory(any())).willReturn(category);

        //-- When, Then
        mockMvc.perform(get("/api/subrecommend/category?userId="+userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response.categoryId").exists())
                .andExpect(jsonPath("$.response.categoryName").exists())
                .andExpect(jsonPath("$.response.totalSpending").exists());

    }

    @Test
    @WithMockUser
    void getRecommendSubList() throws Exception {
        //Given
        /*
        List<Sub> subList = subRecommendProvider.getSubListByCategoryId(categoryId);
         */
        List<Sub> subList = new ArrayList<>();
        Sub sub = CommonTestUtils.createSub();
        subList.add(sub);
        given(subRecommendProvider.getSubListByCategoryId(any())).willReturn(subList);

        //-- When, Then
        mockMvc.perform(get("/api/subrecommend/list?categoryId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response[0].id").exists())
                .andExpect(jsonPath("$.response[0].name").exists())
                .andExpect(jsonPath("$.response[0].fee").exists());

    }

    @Test
    @WithMockUser
    void getSubDetail() throws Exception {
        //-- Given
        SubEntity subEntity = new SubEntity();
        Sub sub = CommonTestUtils.createSub();
        BeanUtils.copyProperties(sub, subEntity);
        given(subRepository.findById(any())).willReturn(Optional.of(subEntity));

        //--When, Then
        mockMvc.perform(get("/api/subrecommend/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.response.id").exists())
                .andExpect(jsonPath("$.response.name").exists())
                .andExpect(jsonPath("$.response.fee").exists());
    }
}
