// File: subrecommend/subrecommend-infra/build.gradle
dependencies {
    implementation project(':common')
    implementation project(':subrecommend:subrecommend-biz')

    //-- OpenFeign Client: Blocking방식의 Http Client
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'

}

/*
OpenFeign Client는 SpringCloud의 컴포넌트이기 때문에 Spring Cloud 종속성 관리 지정 필요
Spring Boot 버전에 맞는 Spring Cloud 버전을 지정해야 함
https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions#supported-releases
*/
dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2023.0.1"
    }
}

// File: subrecommend/subrecommend-infra/build/resources/main/application-test.yml
server:
  port: ${SERVER_PORT:18081}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:subrecommend-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///subrecommend}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: ${JPA_SHOW_SQL:false}
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: ${JPA_HIBERNATE_FORMAT_SQL:true}
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    com.subride.subrecommend.infra.in: DEBUG
    com.subride.subrecommend.infra.out: DEBUG


// File: subrecommend/subrecommend-infra/build/resources/main/application.yml
server:
  port: ${SERVER_PORT:18081}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:subrecommend-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/subrecommend?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: ${JPA_SHOW_SQL:false}
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: ${JPA_HIBERNATE_FORMAT_SQL:true}
springdoc:
  swagger-ui:
    path: /swagger-ui.html
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    com.subride.subrecommend.infra.in: DEBUG
    com.subride.subrecommend.infra.out: DEBUG
feign:
  mysub:
    url: ${MYSUB_URI:http://localhost:18082}



// File: subrecommend/subrecommend-infra/src/test/java/com/subride/subrecommend/infra/out/adapter/SubRecommendProviderImplComponentTest.java
package com.subride.subrecommend.infra.out.adapter;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.infra.common.config.SecurityConfig;
import com.subride.subrecommend.infra.common.jwt.JwtTokenProvider;
import com.subride.subrecommend.infra.common.util.TestDataGenerator;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
데이터 레포지토리 컨포넌트 테스트 예시
- 목적: 데이터 CRUD 테스트
- 방법: 실제 데이터베이스를 테스트 컨테이너로 실행하여 테스트
 */
@DataJpaTest    //Entity, Repository, JPA관련 설정만 로딩하여 데이터 액세스 테스트를 지원함
//-- @DataJpaTest는 기본으로 내장 데이터베이스인 H2를 사용함. 이 테스트 DB를 사용하지 않겠다는 설정임
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/*
테스트 데이터베이스 설정: 컨테이너로 서비스에 사용하는 DB와 동일한 DB를 이용하도록 설정함
- driver-class-name: 컨테이너화된 DB사용을 위한 DB driver 설정
- url: 'jdbc:tc'뒤의 Mysql:8.0.29는 docker hub에 있는 image이름임.
        '//'뒤에는 hostname을 지정하는데 빈 값이면 랜덤으로 지정됨
        만약 docker hub외의 Image registry를 사용한다면 image path를 지정할 때 full path를 써주면 됨
        전체경로 구성: {registry}/{organization}/{repository}:{tag}
        예) myharbor.io/database/mysql:8.0.29
- username, password: DB에 접속할 계정정보인데 아무거나 지정하면 됨
- jpa.database-platform: DB엔진에 따른 Hibernate 유형 지정
 */
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///subrecommend",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})

/*
@DataJpaTest는 데이터 관련된 Bean만 로딩하므로 추가로 필요한 클래스는 Import 해 줘야 함
먼저 필요한 클래스를 추가하고 실행 시 에러 메시지를 보면서 추가해 나가면 됨
 */
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class SubRecommendProviderImplComponentTest {
    private final ISpendingRepository spendingRepository;
    private final ICategoryRepository categoryRepository;
    private final ISubRepository subRepository;

    private SubRecommendProviderImpl subRecommendProvider;

    @Autowired
    public SubRecommendProviderImplComponentTest(ISpendingRepository spendingRepository, ICategoryRepository categoryRepository, ISubRepository subRepository) {
        this.spendingRepository = spendingRepository;
        this.categoryRepository = categoryRepository;
        this.subRepository = subRepository;
    }

    @BeforeEach
    void setup() {
        subRecommendProvider = new SubRecommendProviderImpl(spendingRepository, categoryRepository, subRepository);

        cleanup();  //테스트 데이터 모두 지움

        List<CategoryEntity> categories = TestDataGenerator.generateCategoryEntities();
        categoryRepository.saveAll(categories);

        List<SubEntity> subs = TestDataGenerator.generateSubEntities(categories);
        subRepository.saveAll(subs);

        //-- Life 소비 카테고리의 지출이 가장 많게 테스트 데이터를 넣음
        SpendingEntity spendingEntity = new SpendingEntity();
        spendingEntity.setUserId("user01");
        spendingEntity.setCategory("Life");
        spendingEntity.setAmount(10000000L);
        spendingRepository.save(spendingEntity);

    }
    @AfterEach
    void cleanup() {
        // 테스트 데이터 삭제
        spendingRepository.deleteAll();
        subRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    //-- 소비가 가장 많은 소비 카테고리와 총액 리턴 테스트
    @Test
    void getSpendingByCategory() {
        //-- Given
        String userId = "user01";

        //-- When
        Map<String, Long> spendingCategory = subRecommendProvider.getSpendingByCategory(userId);

        //-- Then
        assertThat(spendingCategory).containsEntry("Life", 10000000L);
        assertThat(spendingCategory.size()).isEqualTo(1);
    }

    @Test
    void getCategoryBySpendingCategory() {
        //-- Given
        String spendingCategory = "Life";

        //-- When
        Category category = subRecommendProvider.getCategoryBySpendingCategory(spendingCategory);

        //-- Then
        assertThat(category.getCategoryName()).isEqualTo("생필품");
        assertThat(category.getSpendingCategory()).isEqualTo("Life");
    }

    @Test
    void getSubListByCategoryId() {
        //-- Given
        String categoryId = "life";

        //-- When
        List<Sub> subList = subRecommendProvider.getSubListByCategoryId(categoryId);

        //-- Then
        assertThat(subList).isNotEmpty();
        assertThat(subList.get(0).getCategory().getCategoryId()).isEqualTo(categoryId);
    }

}


// File: subrecommend/subrecommend-infra/src/test/java/com/subride/subrecommend/infra/in/web/SubRecommendControllerSystemTest.java
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

// File: subrecommend/subrecommend-infra/src/test/java/com/subride/subrecommend/infra/in/web/SubRecommendControllerComponentTest.java
package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.biz.usecase.service.SubRecommendServiceImpl;
import com.subride.subrecommend.infra.common.config.SecurityConfig;
import com.subride.subrecommend.infra.common.jwt.JwtTokenProvider;
import com.subride.subrecommend.infra.out.adapter.SubRecommendProviderImpl;
import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import org.junit.jupiter.api.Test;
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
        CategoryEntity category = new CategoryEntity();
        SubEntity subEntity = new SubEntity();
        Sub sub = CommonTestUtils.createSub();
        subEntity.setId(sub.getId());
        subEntity.setName(sub.getName());
        subEntity.setFee(sub.getFee());
        subEntity.setLogo(sub.getLogo());
        subEntity.setDescription(sub.getDescription());
        subEntity.setCategory(category);
        subEntity.setMaxShareNum(sub.getMaxShareNum());
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


// File: subrecommend/subrecommend-infra/src/test/java/com/subride/subrecommend/infra/in/web/CommonTestUtils.java
package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;

public class CommonTestUtils {

    public static Category createCategory() {
        Category category = new Category();
        category.setCategoryId("food");
        category.setCategoryName("음식");
        category.setSpendingCategory("Food");
        return category;
    }

    public static Sub createSub() {
        Category category = createCategory();

        Sub sub = new Sub();
        sub.setId(1L);
        sub.setCategory(category);
        sub.setName("넷플릭스");
        sub.setDescription("온세상 미디어");
        sub.setFee(15000L);
        sub.setLogo("netflix.png");
        sub.setMaxShareNum(5);

        return sub;
    }

}


// File: subrecommend/subrecommend-infra/src/main/resources/application-test.yml
server:
  port: ${SERVER_PORT:18081}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:subrecommend-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///subrecommend}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: ${JPA_SHOW_SQL:false}
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: ${JPA_HIBERNATE_FORMAT_SQL:true}
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    com.subride.subrecommend.infra.in: DEBUG
    com.subride.subrecommend.infra.out: DEBUG


// File: subrecommend/subrecommend-infra/src/main/resources/application.yml
server:
  port: ${SERVER_PORT:18081}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:subrecommend-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/subrecommend?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: ${JPA_SHOW_SQL:false}
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: ${JPA_HIBERNATE_FORMAT_SQL:true}
springdoc:
  swagger-ui:
    path: /swagger-ui.html
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    com.subride.subrecommend.infra.in: DEBUG
    com.subride.subrecommend.infra.out: DEBUG
feign:
  mysub:
    url: ${MYSUB_URI:http://localhost:18082}



// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/SubRecommendApplication.java
package com.subride.subrecommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SubRecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubRecommendApplication.class, args);
    }
}


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/dto/CategoryDTO.java
package com.subride.subrecommend.infra.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private Long id;
    private String name;
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/dto/SubDTO.java
package com.subride.subrecommend.infra.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubDTO {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private Long fee;
    private int maxShareNum;
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/dto/CategorySpendingDTO.java
package com.subride.subrecommend.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CategorySpendingDTO {
    private String category;
    private Long amount;
/*
    public CategorySpendingDTO(@Value("#{target.category}") String category,
                               @Value("#{target.amount}") Long amount) {
        this.category = category;
        this.amount = amount;
    }

 */
}


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/feign/MySubFeignClient.java
package com.subride.subrecommend.infra.out.feign;

import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mySubFeignClient", url = "${feign.mysub.url}")
public interface MySubFeignClient {
    @GetMapping("/api/my-subs/subid-list")
    ResponseDTO<List<Long>> getMySubIds(@RequestParam String userId);
}


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/entity/SubEntity.java
package com.subride.subrecommend.infra.out.entity;

import com.subride.subrecommend.biz.domain.Sub;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    private Long fee;
    private int maxShareNum;
    private String logo;

    // toDomain 메서드 수정
    public Sub toDomain() {
        Sub sub = new Sub();
        sub.setId(id);
        sub.setName(name);
        sub.setDescription(description);
        sub.setCategory(category.toDomain());
        sub.setFee(fee);
        sub.setMaxShareNum(maxShareNum);
        sub.setLogo(logo);
        return sub;
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/entity/SpendingEntity.java
package com.subride.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spending")
@Getter
@Setter
public class SpendingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "category")
    private String category;

    @Column(name = "amount")
    private Long amount;

    // 기타 필요한 필드 및 메서드 추가
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/entity/CategoryEntity.java
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

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/adapter/SubRecommendProviderImpl.java
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


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/repo/ICategoryRepository.java
package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByCategoryId(String categoryId);
    Optional<CategoryEntity> findBySpendingCategory(String spendingCategory);
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/repo/ISpendingRepository.java
package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.dto.CategorySpendingDTO;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ISpendingRepository extends JpaRepository<SpendingEntity, Long> {
    /*
    JPQL(Java Persistence Query Language) 사용

    쿼리 결과를 CategorySpendingDTO 객체로 매핑 하기 위해
    CategorySpendingDTO에 @AllArgsConstructor로 생성자를 만들고,
    JPQL에 'new' 키워드로 CategorySpendingDTO 객체가 생성되게 함
    */
    @Query("SELECT new com.subride.subrecommend.infra.dto.CategorySpendingDTO(s.category, SUM(s.amount)) FROM SpendingEntity s WHERE s.userId = :userId GROUP BY s.category")
    List<CategorySpendingDTO> getSpendingByCategory(String userId);
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/repo/ISubRepository.java
package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.out.entity.SubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ISubRepository extends JpaRepository<SubEntity, Long> {
    List<SubEntity> findByCategory_CategoryIdOrderByName(String categoryId);
    Optional<SubEntity> findByName(String name);
}



// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/in/web/SubRecommendController.java
package com.subride.subrecommend.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.biz.usecase.inport.ISubRecommendService;
import com.subride.subrecommend.infra.exception.InfraException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SubRecommend API", description = "구독추천 서비스 API")
@Slf4j
@RestController
@SecurityRequirement(name = "bearerAuth")    //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@RequestMapping("/api/subrecommend")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SubRecommendController {
    private final ISubRecommendService subRecommendService;
    private final SubRecommendControllerHelper subRecommendControllerHelper;

    @Operation(summary = "모든 구독 카테고리 리스트 조회", description = "모든 구독 카테고리 정보를 리턴합니다.")
    @GetMapping("/categories")
    public ResponseEntity<ResponseDTO<List<CategoryInfoDTO>>> getAllCategories() {
        try {
            List<CategoryInfoDTO> categories = subRecommendService.getAllCategories();
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 카테고리 리스트 리턴", categories));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "사용자 소비에 맞는 구독 카테고리 구하기",
            description = "최고 소비 카테고리와 매핑된 구독 카테고리의 Id, 이름, 소비액 총합을 리턴함")
    @Parameters({
      @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/category")
    public ResponseEntity<ResponseDTO<CategoryInfoDTO>> getRecommendCategory(@RequestParam String userId) {
        try {
            CategoryInfoDTO categoryInfoDTO = subRecommendService.getRecommendCategoryBySpending(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "소비성향에 맞는 구독 카테고리 리턴", categoryInfoDTO));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "카테고리별 구독 서비스 리스트 리턴", description = "카테고리ID에 해당하는 구독서비스 정보 리턴")
    @Parameters({
        @Parameter(name = "categoryId", in = ParameterIn.QUERY, description = "카테고리ID", required = true)
    })
    @GetMapping("/list")
    public ResponseEntity<ResponseDTO<List<SubInfoDTO>>> getRecommendSubList(@RequestParam String categoryId) {
        try {
            List<SubInfoDTO> subInfoDTOList = subRecommendService.getRecommendSubListByCategory(categoryId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독서비스 리턴", subInfoDTOList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "카테고리별 미구독 서비스 리스트만 리턴", description = "카테고리ID에 해당하는 미구독서비스 리스트 정보 리턴")
    @Parameters({
            @Parameter(name = "categoryId", in = ParameterIn.QUERY, description = "카테고리ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/non-subscribe-list")
    public ResponseEntity<ResponseDTO<List<SubInfoDTO>>> getNonSubList(@RequestParam String categoryId,
                                                                       @RequestParam String userId) {
        try {
            List<SubInfoDTO> subInfoDTOList = subRecommendService.getRecommendSubListByCategory(categoryId);
            List<SubInfoDTO> nonSubInfoDTOList = subRecommendControllerHelper.getNonSubList(subInfoDTOList, userId);

            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "미구독서비스 리턴", nonSubInfoDTOList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독상세 정보 리턴", description = "구독서비스ID에 해당하는 구독서비스 정보 리턴")
    @Parameters({
        @Parameter(name = "subId", in = ParameterIn.PATH, description = "구독서비스ID", required = true)
    })
    @GetMapping("/detail/{subId}")
    public ResponseEntity<ResponseDTO<SubInfoDTO>> getSubDetail(@PathVariable Long subId) {
        try {
            SubInfoDTO subInfoDTO = subRecommendControllerHelper.getSubDetail(subId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독상세 정보 리턴", subInfoDTO));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독 ID 리스트로 구독 정보 조회", description = "구독 ID 리스트를 받아 구독 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "subIds", in = ParameterIn.QUERY, description = "구독 ID 리스트 (쉼표로 구분)", required = true)
    })
    @GetMapping("/list-by-ids")
    public ResponseEntity<ResponseDTO<List<SubInfoDTO>>> getSubInfoListByIds(@RequestParam List<Long> subIds) {
        List<SubInfoDTO> subInfoDTOList = subRecommendService.getSubInfoListByIds(subIds);
        return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 정보 조회 성공", subInfoDTOList));
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/in/web/SubRecommendControllerHelper.java
package com.subride.subrecommend.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.infra.exception.InfraException;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.feign.MySubFeignClient;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class SubRecommendControllerHelper {
    private final ISubRepository subRepository;
    private final MySubFeignClient mySubFeignClient;

    public SubInfoDTO getSubDetail(Long subId) {
        SubEntity sub = subRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("Sub not found"));
        return toSubInfoDTO(sub);
    }

    public SubInfoDTO toSubInfoDTO(SubEntity sub) {
        SubInfoDTO subInfoDTO = new SubInfoDTO();
        subInfoDTO.setId(sub.getId());
        subInfoDTO.setLogo(sub.getLogo());
        subInfoDTO.setName(sub.getName());
        subInfoDTO.setCategoryName(sub.getCategory().getCategoryName());
        subInfoDTO.setDescription(sub.getDescription());
        subInfoDTO.setFee(sub.getFee());
        subInfoDTO.setMaxShareNum(sub.getMaxShareNum());
        return subInfoDTO;
    }

    public List<SubInfoDTO> toSubInfoDTOList(List<SubEntity> subList) {
        return subList.stream()
                .map(this::toSubInfoDTO)
                .collect(Collectors.toList());
    }

    public List<SubInfoDTO> getNonSubList(List<SubInfoDTO> subList, String userId) {
        ResponseDTO<List<Long>> response = mySubFeignClient.getMySubIds(userId);
        if(response.getCode()==0) {
            throw new InfraException(0, "구독ID 리스트 구하기 실패");
        }
        List<Long> mySubIds = response.getResponse();

        return subList.stream()
                .filter(subInfoDTO -> !mySubIds.contains(subInfoDTO.getId()))
                .collect(Collectors.toList());
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/jwt/JwtAuthenticationInterceptor.java
package com.subride.subrecommend.infra.common.jwt;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
Feign 클라이언트에서 요청 시 Authorization 헤더에 인증토큰 추가하기
 */
@Component
public class JwtAuthenticationInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String token = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
            if (token != null) {
                template.header(AUTHORIZATION_HEADER, token);
            }
        }
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/jwt/JwtAuthenticationFilter.java
// CommonJwtAuthenticationFilter.java
package com.subride.subrecommend.infra.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/jwt/JwtTokenProvider.java
// CommonJwtTokenProvider.java
package com.subride.subrecommend.infra.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.subride.subrecommend.infra.exception.InfraException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private final Algorithm algorithm;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.algorithm = Algorithm.HMAC512(secretKey);
    }

    public Authentication getAuthentication(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String username = decodedJWT.getSubject();
            String[] authStrings = decodedJWT.getClaim("auth").asArray(String.class);
            Collection<? extends GrantedAuthority> authorities = Arrays.stream(authStrings)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UserDetails userDetails = new User(username, "", authorities);

            return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
        } catch (Exception e) {
            throw new InfraException(0, "Invalid refresh token");
        }
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/util/TestDataGenerator.java
package com.subride.subrecommend.infra.common.util;

import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;

import java.util.*;

public class TestDataGenerator {
    public static List<CategoryEntity> generateCategoryEntities() {
        CategoryEntity category1 = new CategoryEntity("life", "생필품", "Life");
        CategoryEntity category2 = new CategoryEntity("pet", "반려동물", "Pet");
        CategoryEntity category3 = new CategoryEntity("ott", "OTT", "OTT");
        CategoryEntity category4 = new CategoryEntity("food", "Food", "Food");
        CategoryEntity category5 = new CategoryEntity("health", "건강", "Health");
        CategoryEntity category6 = new CategoryEntity("culture", "문화", "Culture");

        return Arrays.asList(category1, category2, category3, category4, category5, category6);
    }

    public static List<SubEntity> generateSubEntities(List<CategoryEntity> categoryEntities) {
        SubEntity sub1 = new SubEntity(1L, "런드리고", "빨래구독 서비스", categoryEntities.get(0), 35000L, 2, "laundrygo.png");
        SubEntity sub2 = new SubEntity(2L, "술담화", "인생 전통술 구독 서비스", categoryEntities.get(3), 39000L, 3, "suldamhwa.jpeg");
        SubEntity sub3 = new SubEntity(3L, "필리", "맞춤형 영양제 정기배송", categoryEntities.get(4), 15000L, 3, "pilly.png");
        SubEntity sub4 = new SubEntity(4L, "넷플릭스", "넷플릭스", categoryEntities.get(2), 15000L, 5, "netflix.png");
        SubEntity sub5 = new SubEntity(5L, "티빙", "티빙", categoryEntities.get(2), 15000L, 5, "tving.png");
        SubEntity sub6 = new SubEntity(6L, "쿠팡플레이", "쿠팡플레이", categoryEntities.get(2), 15000L, 5, "coupang.png");
        SubEntity sub7 = new SubEntity(7L, "디즈니플러스", "디즈니플러스", categoryEntities.get(2), 15000L, 5, "disney.png");
        SubEntity sub8 = new SubEntity(8L, "해피문데이", "생리대 배송 서비스", categoryEntities.get(0), 6000L, 2, "happymoonday.jpeg");
        SubEntity sub9 = new SubEntity(9L, "하비인더박스", "취미용 소품 송 서비스", categoryEntities.get(0), 29000L, 3, "hobbyinthebox.jpeg");
        SubEntity sub10 = new SubEntity(10L, "월간가슴", "맞춤형 브라 배송", categoryEntities.get(0), 16000L, 2, "monthlychest.png");
        SubEntity sub11 = new SubEntity(11L, "위클리셔츠", "깔끔하고 다양한 셔츠 3~5장 매주 배송", categoryEntities.get(0), 40000L, 2, "weeklyshirts.jpeg");
        SubEntity sub12 = new SubEntity(12L, "월간과자", "매월 다른 구성의 과자상자 배송", categoryEntities.get(3), 9900L, 3, "monthlysnack.jpeg");
        SubEntity sub13 = new SubEntity(13L, "밀리의서재", "전자책 무제한 구독", categoryEntities.get(5), 9900L, 5, "milibook.jpeg");
        SubEntity sub14 = new SubEntity(14L, "더 반찬", "맛있고 다양한 집밥반찬 5세트", categoryEntities.get(3), 70000L, 3, "sidedishes.jpeg");
        SubEntity sub15 = new SubEntity(15L, "와이즐리", "면도날 구독 서비스", categoryEntities.get(0), 8900L, 4, "wisely.jpeg");
        SubEntity sub16 = new SubEntity(16L, "미하이 삭스", "매달 패션 양말 3종 배송", categoryEntities.get(0), 990L, 3, "mehi.jpeg");
        SubEntity sub17 = new SubEntity(17L, "핀즐", "자취방 꾸미고 싶은 사람들을 위한 그림 구독 서비스", categoryEntities.get(0), 26000L, 3, "pinzle.png");
        SubEntity sub18 = new SubEntity(18L, "꾸까", "2주마다 꽃 배달 서비스", categoryEntities.get(0), 30000L, 3, "kukka.png");
        SubEntity sub19 = new SubEntity(19L, "커피 리브레", "매주 다른 종류의 커피 배달", categoryEntities.get(3), 48000L, 5, "coffeelibre.jpeg");

        return Arrays.asList(sub1, sub2, sub3, sub4, sub5, sub6, sub7, sub8, sub9,
                sub10, sub11, sub12, sub13, sub14, sub15, sub16, sub17, sub18, sub19);
    }

    public static List<SpendingEntity> generateSpendingEntities(String[] userIds, String[] categories) {
        Random random = new Random();
        List<SpendingEntity> spendingEntities = new ArrayList<>();

        for (String userId : userIds) {
            for (int i = 0; i < 50; i++) {
                SpendingEntity spendingEntity = new SpendingEntity();
                spendingEntity.setUserId(userId);
                spendingEntity.setCategory(categories[random.nextInt(categories.length)]);
                spendingEntity.setAmount(random.nextLong(1000, 100000));
                spendingEntities.add(spendingEntity);
            }
        }

        return spendingEntities;
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/config/SecurityConfig.java
package com.subride.subrecommend.infra.common.config;

import com.subride.subrecommend.infra.common.jwt.JwtAuthenticationFilter;
import com.subride.subrecommend.infra.common.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@SuppressWarnings("unused")
public class SecurityConfig {
    protected final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs.yaml", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000",
                    "http://localhost:18082"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/config/DataInitializer.java
package com.subride.subrecommend.infra.common.config;

import com.subride.subrecommend.infra.common.util.TestDataGenerator;
import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISpendingRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
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
        if (categoryRepository.count() == 0 && subRepository.count() == 0 && spendingRepository.count() == 0) {
            List<CategoryEntity> categories = TestDataGenerator.generateCategoryEntities();
            categoryRepository.saveAll(categories);
            List<SubEntity> subs = TestDataGenerator.generateSubEntities(categories);
            subRepository.saveAll(subs);

            String[] userIds = {"user01", "user02", "user03", "user04", "user05"};
            String[] categoryNames = categories.stream().map(CategoryEntity::getSpendingCategory).toArray(String[]::new);
            List<SpendingEntity> spendings = TestDataGenerator.generateSpendingEntities(userIds, categoryNames);
            spendingRepository.saveAll(spendings);
        }
    }
/*
    @PreDestroy
    public void cleanData() {
        spendingRepository.deleteAll();
        subRepository.deleteAll();
        categoryRepository.deleteAll();
    }

 */
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/config/LoggingAspect.java
package com.subride.subrecommend.infra.common.config;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Aspect       //Disable하려면 리마크 함
@Component
@Slf4j
@SuppressWarnings("unused")
public class LoggingAspect {
    private final Gson gson = new Gson();

    @Pointcut("execution(* com.subride..*.*(..))")
    private void loggingPointcut() {}

    @Before("loggingPointcut()")
    public void logMethodStart(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String argString = getArgumentString(args);

        log.info("[START] {}.{} - Args: [{}]", className, methodName, argString);
    }

    @AfterReturning(pointcut = "loggingPointcut()", returning = "result")
    public void logMethodEnd(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String resultString = getResultString(result);

        log.info("[END] {}.{} - Result: {}", className, methodName, resultString);
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.error("[EXCEPTION] {}.{} - Exception: {}", className, methodName, exception.getMessage());
    }

    private String getArgumentString(Object[] args) {
        StringBuilder argString = new StringBuilder();

        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    argString.append(arg).append(", ");
                } else if (arg instanceof Collection) {
                    argString.append(((Collection<?>) arg).size()).append(" elements, ");
                } else if (arg instanceof Map) {
                    argString.append(((Map<?, ?>) arg).size()).append(" entries, ");
                } else {
                    argString.append(arg);
                    /*
                    try {
                        String jsonString = gson.toJson(arg);
                        argString.append(jsonString).append(", ");
                    } catch (Exception e) {
                        log.warn("JSON serialization failed for argument: {}", arg);
                        argString.append("JSON serialization failed, ");
                    }
                    */

                }
            } else {
                argString.append("null, ");
            }
        }

        if (!argString.isEmpty()) {
            argString.setLength(argString.length() - 2);
        }

        return argString.toString();
    }

    private String getResultString(Object result) {
        if (result != null) {
            if (result instanceof String || result instanceof Number || result instanceof Boolean) {
                return result.toString();
            } else if (result instanceof Collection) {
                return ((Collection<?>) result).size() + " elements";
            } else if (result instanceof Map) {
                return ((Map<?, ?>) result).size() + " entries";
            } else {
                return result.toString();
                /*
                try {
                    return gson.toJson(result);
                } catch (Exception e) {
                    log.warn("JSON serialization failed for result: {}", result);
                    return "JSON serialization failed";
                }

                 */
            }
        } else {
            return "null";
        }
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/config/SpringDocConfig.java
package com.subride.subrecommend.infra.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SuppressWarnings("unused")
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("구독추천 서비스 API")
                        .version("v1.0.0")
                        .description("구독추천 서비스 API 명세서입니다. "));
    }
}


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/exception/InfraException.java
package com.subride.subrecommend.infra.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class InfraException extends RuntimeException {
    private int code;

    public InfraException(String message) {
        super(message);
    }

    public InfraException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfraException(int code, String message) {
        super(message);
        this.code = code;
    }
    public InfraException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}


// File: subrecommend/subrecommend-biz/build.gradle
dependencies {
    implementation project(':common')
}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/dto/SubInfoDTO.java
package com.subride.subrecommend.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubInfoDTO {
    private Long id;
    private String name;
    private String categoryName;
    private String logo;
    private String description;
    private Long fee;
    private int maxShareNum;
}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/dto/CategoryInfoDTO.java
package com.subride.subrecommend.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryInfoDTO {
    private String categoryId;
    private String categoryName;
    private String spendingCategory;
    private Long totalSpending;
}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/usecase/inport/ISubRecommendService.java
package com.subride.subrecommend.biz.usecase.inport;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;

import java.util.List;

public interface ISubRecommendService {
    List<CategoryInfoDTO> getAllCategories();
    CategoryInfoDTO getRecommendCategoryBySpending(String userId);
    List<SubInfoDTO> getRecommendSubListByCategory(String categoryId);

    List<SubInfoDTO> getSubInfoListByIds(List<Long> subIds);
}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/usecase/outport/ISubRecommendProvider.java
package com.subride.subrecommend.biz.usecase.outport;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;

import java.util.List;
import java.util.Map;

public interface ISubRecommendProvider {
    List<Category> getAllCategories();
    Map<String, Long> getSpendingByCategory(String userId);
    Category getCategoryBySpendingCategory(String spendingCategory);
    List<Sub> getSubListByCategoryId(String categoryId);
    List<Sub> getSubListByIds(List<Long> subIds);
}


// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/usecase/service/SubRecommendServiceImpl.java
package com.subride.subrecommend.biz.usecase.service;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.biz.usecase.inport.ISubRecommendService;
import com.subride.subrecommend.biz.usecase.outport.ISubRecommendProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubRecommendServiceImpl implements ISubRecommendService {
    private final ISubRecommendProvider subRecommendProvider;

    @Override
    public List<CategoryInfoDTO> getAllCategories() {
        List<Category> categories = subRecommendProvider.getAllCategories();
        return categories.stream()
                .map(this::toCategoryInfoDTO)
                .collect(Collectors.toList());
    }

    private CategoryInfoDTO toCategoryInfoDTO(Category category) {
        CategoryInfoDTO categoryInfoDTO = new CategoryInfoDTO();
        categoryInfoDTO.setCategoryId(category.getCategoryId());
        categoryInfoDTO.setCategoryName(category.getCategoryName());
        categoryInfoDTO.setSpendingCategory(category.getSpendingCategory());
        return categoryInfoDTO;
    }

    @Override
    public CategoryInfoDTO getRecommendCategoryBySpending(String userId) {
        Map<String, Long> spendingByCategory = subRecommendProvider.getSpendingByCategory(userId);
        String maxSpendingCategory = spendingByCategory.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        Category category = subRecommendProvider.getCategoryBySpendingCategory(maxSpendingCategory);

        CategoryInfoDTO categoryInfoDTO = new CategoryInfoDTO();
        categoryInfoDTO.setCategoryId(category.getCategoryId());
        categoryInfoDTO.setCategoryName(category.getCategoryName());
        categoryInfoDTO.setSpendingCategory(maxSpendingCategory);
        categoryInfoDTO.setTotalSpending(spendingByCategory.get(maxSpendingCategory));

        return categoryInfoDTO;
    }

    @Override
    public List<SubInfoDTO> getRecommendSubListByCategory(String categoryId) {
        List<Sub> subList = subRecommendProvider.getSubListByCategoryId(categoryId);

        return subList.stream()
                .map(this::toSubInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubInfoDTO> getSubInfoListByIds(List<Long> subIds) {
        List<Sub> subList = subRecommendProvider.getSubListByIds(subIds);
        return subList.stream()
                .map(this::toSubInfoDTO)
                .collect(Collectors.toList());
    }

    private SubInfoDTO toSubInfoDTO(Sub sub) {
        SubInfoDTO subInfoDTO = new SubInfoDTO();
        subInfoDTO.setId(sub.getId());
        subInfoDTO.setName(sub.getName());
        subInfoDTO.setLogo(sub.getLogo());
        subInfoDTO.setDescription(sub.getDescription());
        subInfoDTO.setFee(sub.getFee());
        subInfoDTO.setMaxShareNum(sub.getMaxShareNum());

        return subInfoDTO;
    }
}


// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/domain/Category.java
package com.subride.subrecommend.biz.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {
    private String categoryId;
    private String categoryName;
    private String spendingCategory;
}



// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/domain/Sub.java
package com.subride.subrecommend.biz.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sub {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private Category category;
    private Long fee;
    private int maxShareNum;

}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/exception/BizException.java
package com.subride.subrecommend.biz.exception;

public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}

// File: /Users/ondal/workspace/subride/settings.gradle
rootProject.name = 'subride'
include 'common'
include 'member:member-biz'
include 'member:member-infra'
include 'subrecommend:subrecommend-infra'
include 'subrecommend:subrecommend-biz'
include 'mysub:mysub-infra'
include 'mysub:mysub-biz'
include 'mygrp:mygrp-infra'
include 'mygrp:mygrp-biz'



// File: /Users/ondal/workspace/subride/build.gradle
/*
Spring Boot 버전에 따라 사용하는 라이브러리의 버전을 맞춰줘야 함
라이브러리 버전을 맞추지 않으면 실행 시 이상한 에러가 많이 발생함.
이를 편하게 하기 위해 Spring Boot는 기본적으로 지원하는 라이브러리 목록을 갖고 있음
이 목록에 있는 라이브러리는 버전을 명시하지 않으면 Spring Boot 버전에 맞는 버전을 로딩함

Spring Boot 지원 라이브러리 목록 확인
1) Spring Boot 공식 사이트 접속: spring.io
2) Projects > Spring Boot 선택 > LEARN 탭 클릭
3) 사용할 Spring Boot 버전의 'Reference Doc' 클릭
4) 좌측 메뉴에서 보통 맨 마지막에 있는 'Dependency Versions' 클릭
5) 사용할 라이브러리를 찾음. 만약 있으면 버전 명시 안해도 됨
*/

plugins {
	id 'java'
	id 'org.springframework.boot' version '3.2.6'
}

allprojects {
	group = 'com.subride'
	version = '0.0.1-SNAPSHOT'

	apply plugin: 'java'
	apply plugin: 'io.spring.dependency-management'

	/*
    Gradle 8.7 부터 자바 버전 지정 방식 변경
    이전 코드는 아래와 같이 Java 항목으로 감싸지 않았고 버전을 직접 지정했음
    sourceCompatibility = '17'
    targetCompatibility = '17'
    */
	java {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	repositories {
		mavenCentral()
	}

	dependencies {
		implementation 'org.springframework.boot:spring-boot-starter-validation'
		implementation 'org.springframework.boot:spring-boot-starter-aop'
		implementation 'com.google.code.gson:gson'               		//Json처리
		compileOnly 'org.projectlombok:lombok'
		annotationProcessor 'org.projectlombok:lombok'

		/*
        TEST를 위한 설정
        */
		//=====================================================
		testImplementation 'org.springframework.boot:spring-boot-starter-test'

		//--JUnit, Mokito Test
		testImplementation 'org.junit.jupiter:junit-jupiter-api'
		testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'

		testImplementation 'org.mockito:mockito-core'
		testImplementation 'org.mockito:mockito-junit-jupiter'

		//-- spring security test
		testImplementation 'org.springframework.security:spring-security-test'

		//-- For mysql System test
		testImplementation 'org.testcontainers:mysql'

		//-- For WebMvc System test
		implementation 'org.springframework.boot:spring-boot-starter-webflux'

		//-- lombok
		// -- @SpringBootTest를 사용하여 전체 애플리케이션 컨텍스트를 로딩되는 테스트 코드에만 사용
		// -- 그 외 단위나 컴포넌트 테스트에 사용하면 제대로 동작안함
		testCompileOnly 'org.projectlombok:lombok'
		testAnnotationProcessor 'org.projectlombok:lombok'
		//=============================================
	}

	//==== Test를 위한 설정 ===
	sourceSets {
		test {
			java {
				srcDirs = ['src/test/java']
			}
		}
	}
	test {
		useJUnitPlatform()
		include '**/*Test.class'		//--클래스 이름이 Test로 끝나는 것만 포함함
	}
	//==========================
}

subprojects {
	apply plugin: 'org.springframework.boot'
}

project(':common') {
	bootJar.enabled = false
	jar.enabled = true
}

configure(subprojects.findAll { it.name.endsWith('-infra') }) {
	dependencies {
		implementation 'org.springframework.boot:spring-boot-starter-web'
		implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
		implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'	//For swagger

		runtimeOnly 'com.mysql:mysql-connector-j'
		implementation 'org.springframework.boot:spring-boot-starter-security'
		implementation 'com.auth0:java-jwt:4.4.0'			//JWT unitlity
	}
}

configure(subprojects.findAll { it.name.endsWith('-biz') }) {
	bootJar.enabled = false
	jar.enabled = true
}

