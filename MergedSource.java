// File: mysub/mysub-infra/build.gradle
dependencies {
    implementation project(':common')
    implementation project(':mysub:mysub-biz')

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

// File: mysub/mysub-infra/build/resources/main/application-test.yml
server:
  port: ${SERVER_PORT:18082}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:mysub-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///mysub}
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
feign:
  subrecommend:
    url: ${SUBRECOMMEND_URI:http://localhost:18081}



// File: mysub/mysub-infra/build/resources/main/application.yml
server:
  port: ${SERVER_PORT:18082}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:mysub-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/mysub?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
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
    com.subride.mysub.infra.in: DEBUG
    com.subride.mysub.infra.out: DEBUG
    feign:
      codec:
        logger:
          level: DEBUG
feign:
  subrecommend:
    url: ${SUBRECOMMEND_URI:http://localhost:18081}
  mygroup:
    url: ${MYGRP_URI:http://localhost:18083}


// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/out/adapter/MySubProviderImplComponentTest.java
// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/out/adapter/MySubProviderImplComponentTest.java
package com.subride.mysub.infra.out.adapter;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.infra.common.util.TestDataGenerator;
import com.subride.mysub.infra.exception.InfraException;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.feign.MyGroupFeignClient;
import com.subride.mysub.infra.out.feign.SubRecommendFeignClient;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
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
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///mysub",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
public class MySubProviderImplComponentTest {
    @Autowired
    private IMySubRepository mySubRepository;
    @MockBean
    private SubRecommendFeignClient subRecommendFeignClient;
    @MockBean
    private MyGroupFeignClient myGroupFeignClient;

    private MySubProviderImpl mySubProvider;

    @BeforeEach
    void setup() {
        mySubProvider = new MySubProviderImpl(mySubRepository, subRecommendFeignClient, myGroupFeignClient);
        List<MySubEntity> mySubEntities = TestDataGenerator.generateMySubEntities("user01", 1L);
        mySubRepository.saveAll(mySubEntities);
    }

    @AfterEach
    void cleanup() {
        mySubRepository.deleteAll();
    }

    @Test
    void getMySubList_ValidUserId_ReturnMySubList() {
        // Given
        String userId = "user01";

        ResponseDTO<List<GroupSummaryDTO>> myGroupListResponse = ResponseDTO.<List<GroupSummaryDTO>>builder()
                .code(200)
                .response(List.of(new GroupSummaryDTO()))
                .build();
        ResponseDTO<List<SubInfoDTO>> response = ResponseDTO.<List<SubInfoDTO>>builder()
                .code(200)
                .response(List.of(new SubInfoDTO()))
                .build();
        given(myGroupFeignClient.getMyGroupList(any())).willReturn(myGroupListResponse);
        given(subRecommendFeignClient.getSubInfoListByIds(any())).willReturn(response);

        // When
        List<MySubInfoDTO> mySubList = mySubProvider.getMySubList(userId);

        // Then
        assertThat(mySubList).isNotEmpty();
        assertThat(mySubList.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void cancelSub_ValidUserIdAndSubId_DeleteMySub() {
        // Given
        String userId = "user01";
        Long subId = 1L;
        mySubProvider.subscribeSub(subId, userId);  //--테스트 데이터 등록

        ResponseDTO<List<Long>> response = ResponseDTO.<List<Long>>builder()
                .code(200)
                .response(new ArrayList<>())
                .build();
        given(myGroupFeignClient.getJoinSubIds(any())).willReturn(response);

        // When
        mySubProvider.cancelSub(subId, userId);

        // Then
        Optional<MySubEntity> deletedMySub = mySubRepository.findByUserIdAndSubId(userId, subId);
        assertThat(deletedMySub).isEmpty();
    }

    @Test
    void cancelSub_InvalidUserIdAndSubId_ThrowInfraException() {
        // Given
        String userId = "invalidUser";
        Long subId = 999L;

        ResponseDTO<List<Long>> response = ResponseDTO.<List<Long>>builder()
                .code(200)
                .response(new ArrayList<>())
                .build();
        given(myGroupFeignClient.getJoinSubIds(any())).willReturn(response);

        // When, Then
        assertThatThrownBy(() -> mySubProvider.cancelSub(subId, userId))
                .isInstanceOf(InfraException.class)
                .hasMessage("구독 정보가 없습니다.");
    }

    @Test
    void subscribeSub_ValidUserIdAndSubId_SaveMySub() {
        // Given
        String userId = "newUser";
        Long subId = 100L;

        // When
        mySubProvider.subscribeSub(subId, userId);

        // Then
        Optional<MySubEntity> savedMySub = mySubRepository.findByUserIdAndSubId(userId, subId);
        assertThat(savedMySub).isPresent();
        assertThat(savedMySub.get().getUserId()).isEqualTo(userId);
        assertThat(savedMySub.get().getSubId()).isEqualTo(subId);
    }

    @Test
    void isSubscribed_SubscribedUserIdAndSubId_ReturnTrue() {
        // Given
        String userId = "user01";
        Long subId = 900L;
        mySubProvider.subscribeSub(subId, userId);  //--테스트 데이터 등록

        // When
        boolean isSubscribed = mySubProvider.isSubscribed(userId, subId);

        // Then
        assertThat(isSubscribed).isTrue();
    }

    @Test
    void isSubscribed_NotSubscribedUserIdAndSubId_ReturnFalse() {
        // Given
        String userId = "user01";
        Long subId = 999L;

        // When
        boolean isSubscribed = mySubProvider.isSubscribed(userId, subId);

        // Then
        assertThat(isSubscribed).isFalse();
    }
}

// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/in/web/MySubControllerSystemTest.java
// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/in/web/MySubControllerSystemTest.java
package com.subride.mysub.infra.in.web;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.infra.common.util.TestDataGenerator;
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
    private Long testSubId = 1L;

    @BeforeEach
    void setup() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();
        mySubProvider = new MySubProviderImpl(mySubRepository, subRecommendFeignClient, myGroupFeignClient);

        cleanup();  //테스트 데이터 모두 지움

        List<MySubEntity> mySubEntities = TestDataGenerator.generateMySubEntities(testUserId, testSubId);
        mySubRepository.saveAll(mySubEntities);

        //testSubId = mySubRepository.findByUserId(testUserId).get(0).getSubId();
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
        ResponseDTO<List<GroupSummaryDTO>> myGroupListResponse = TestDataGenerator.generateResponseDTO(200, List.of(new GroupSummaryDTO()));
        myGroupListResponse.getResponse().get(0).setSubId(testSubId);
        ResponseDTO<List<SubInfoDTO>> response = TestDataGenerator.generateResponseDTO(200, List.of(new SubInfoDTO()));
        response.getResponse().get(0).setSubId(testSubId);
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

// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/in/web/MySubControllerComponentTest.java
// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/in/web/MySubControllerComponentTest.java
package com.subride.mysub.infra.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.biz.dto.MySubDTO;
import com.subride.mysub.biz.usecase.service.MySubServiceImpl;
import com.subride.mysub.infra.common.config.SecurityConfig;
import com.subride.mysub.infra.common.jwt.JwtTokenProvider;
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
        ResponseDTO<SubInfoDTO> responseDTO = ResponseDTO.<SubInfoDTO>builder()
                .code(200)
                .response(new SubInfoDTO())
                .build();
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

// File: mysub/mysub-infra/src/test/java/com/subride/mysub/infra/common/util/TestDataGenerator.java
// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/util/TestDataGenerator.java
package com.subride.mysub.infra.common.util;

import com.subride.common.dto.ResponseDTO;
import com.subride.mysub.infra.out.entity.MySubEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {
    public static List<MySubEntity> generateMySubEntities(String userId, Long subId) {
        List<MySubEntity> mySubEntities = new ArrayList<>();
        Random random = new Random();
        /*
        for (int i = 0; i < 5; i++) {
            MySubEntity mySubEntity = new MySubEntity();
            mySubEntity.setUserId(userId);
            mySubEntity.setSubId(random.nextLong(1, 100));
            mySubEntities.add(mySubEntity);
        }
        */
        MySubEntity mySubEntity = new MySubEntity();
        mySubEntity.setUserId(userId);
        mySubEntity.setSubId(subId);
        mySubEntities.add(mySubEntity);
        return mySubEntities;
    }

    public static <T> ResponseDTO<T> generateResponseDTO(int code, T response) {
        return ResponseDTO.<T>builder()
                .code(code)
                .response(response)
                .build();
    }
}

// File: mysub/mysub-infra/src/main/resources/application-test.yml
server:
  port: ${SERVER_PORT:18082}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:mysub-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///mysub}
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
feign:
  subrecommend:
    url: ${SUBRECOMMEND_URI:http://localhost:18081}



// File: mysub/mysub-infra/src/main/resources/application.yml
server:
  port: ${SERVER_PORT:18082}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:mysub-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/mysub?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
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
    com.subride.mysub.infra.in: DEBUG
    com.subride.mysub.infra.out: DEBUG
    feign:
      codec:
        logger:
          level: DEBUG
feign:
  subrecommend:
    url: ${SUBRECOMMEND_URI:http://localhost:18081}
  mygroup:
    url: ${MYGRP_URI:http://localhost:18083}


// File: mysub/mysub-infra/src/main/java/com/subride/mysub/MySubApplication.java
package com.subride.mysub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MySubApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySubApplication.class, args);
    }
}

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/out/feign/SubRecommendFeignClient.java
package com.subride.mysub.infra.out.feign;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "subRecommendFeignClient", url = "${feign.subrecommend.url}")
public interface SubRecommendFeignClient {
    @GetMapping("/api/subrecommend/detail/{subId}")
    ResponseDTO<SubInfoDTO> getSubDetail(@PathVariable("subId") Long subId);

    @GetMapping("/api/subrecommend/list-by-ids")
    ResponseDTO<List<SubInfoDTO>> getSubInfoListByIds(@RequestParam List<Long> subIds);
}


// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/out/feign/MyGroupFeignClient.java
package com.subride.mysub.infra.out.feign;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "myGroupFeignClient", url = "${feign.mygroup.url}")
public interface MyGroupFeignClient {
    @GetMapping("/api/my-groups")
    ResponseDTO<List<GroupSummaryDTO>> getMyGroupList(@RequestParam String userId);
    @GetMapping("/sub-id-list")
    ResponseDTO<List<Long>> getJoinSubIds(@RequestParam String userId);
}


// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/out/entity/MySubEntity.java
package com.subride.mysub.infra.out.entity;

import com.subride.mysub.biz.domain.MySub;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "my_sub")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "sub_id")
    private Long subId;

    public MySub toDomain() {
        MySub mySub = new MySub();
        mySub.setUserId(userId);
        mySub.setSubId(subId);
        return mySub;
    }
}

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/out/adapter/MySubProviderImpl.java
package com.subride.mysub.infra.out.adapter;

import com.subride.common.dto.GroupSummaryDTO;
import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mysub.biz.usecase.outport.IMySubProvider;
import com.subride.mysub.infra.exception.InfraException;
import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.feign.MyGroupFeignClient;
import com.subride.mysub.infra.out.feign.SubRecommendFeignClient;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySubProviderImpl implements IMySubProvider {
    private final IMySubRepository mySubRepository;
    private final SubRecommendFeignClient subRecommendFeignClient;
    private final MyGroupFeignClient myGroupFeignClient;

    @Override
    public List<MySubInfoDTO> getMySubList(String userId) {
        List<MySubEntity> mySubEntityList = mySubRepository.findByUserId(userId);
        if (mySubEntityList.isEmpty()) {
            // userId에 해당하는 MySubEntity가 없는 경우 처리
            throw new InfraException(0, "해당 사용자의 구독 정보가 없습니다.");
        }

        List<Long> mySubIds = mySubEntityList.stream()
                .map(MySubEntity::getSubId)
                .collect(Collectors.toList());

        ResponseDTO<List<GroupSummaryDTO>> myGroupListResponse = myGroupFeignClient.getMyGroupList(userId);
        List<GroupSummaryDTO> myGroupList = myGroupListResponse.getResponse();

        ResponseDTO<List<SubInfoDTO>> response = subRecommendFeignClient.getSubInfoListByIds(mySubIds);
        List<SubInfoDTO> subInfoList = response.getResponse();

        return mySubEntityList.stream()
                .map(mySubEntity -> {
                    MySubInfoDTO mySubInfoDTO = new MySubInfoDTO();
                    mySubInfoDTO.setUserId(mySubEntity.getUserId());
                    mySubInfoDTO.setSubId(mySubEntity.getSubId());

                    //구독정보 찾기
                    SubInfoDTO subInfo = subInfoList.stream()
                            .filter(dto -> dto.getSubId().equals(mySubEntity.getSubId()))
                            .findFirst()
                            .orElse(null);

                    if(subInfo != null) {
                        mySubInfoDTO.setSubName(subInfo.getSubName());
                        mySubInfoDTO.setCategoryName(subInfo.getCategoryName());
                        mySubInfoDTO.setLogo(subInfo.getLogo());
                        mySubInfoDTO.setDescription(subInfo.getDescription());
                        mySubInfoDTO.setFee(subInfo.getFee());
                        mySubInfoDTO.setMaxShareNum(subInfo.getMaxShareNum());

                        // joinGroupMemberCountList에서 동일한 subId를 가진 객체 찾기
                        GroupSummaryDTO groupSummaryDTO = myGroupList.stream()
                                .filter(dto -> dto.getSubId().equals(mySubEntity.getSubId()))
                                .findFirst()
                                .orElse(null);

                        if (groupSummaryDTO != null) {
                            mySubInfoDTO.setJoinGroup(true);
                            mySubInfoDTO.setPayedFee(subInfo.getFee() / groupSummaryDTO.getMemberCount());
                            mySubInfoDTO.setDiscountedFee(0L);
                        } else {
                            mySubInfoDTO.setJoinGroup(false);
                            mySubInfoDTO.setPayedFee(subInfo.getFee());
                            mySubInfoDTO.setDiscountedFee(subInfo.getFee() - subInfo.getFee() / subInfo.getMaxShareNum());
                        }
                    }
                    return mySubInfoDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void cancelSub(Long subId, String userId) {
        MySubEntity mySubEntity = mySubRepository.findByUserIdAndSubId(userId, subId)
                .orElseThrow(() -> new InfraException("구독 정보가 없습니다."));

        ResponseDTO<List<Long>> response = myGroupFeignClient.getJoinSubIds(userId);
        List<Long> joinSubIds = response.getResponse();

        // joinGroupMemberCountList에서 해당 subId를 가진 그룹이 있는지 확인
        boolean isJoinedGroup = joinSubIds.contains(subId);

        if (isJoinedGroup) {
            throw new InfraException(0, "썹 그룹에 참여중인 서비스는 가입취소 할 수 없습니다.");
        }

        mySubRepository.delete(mySubEntity);
    }

    @Override
    public void subscribeSub(Long subId, String userId) {

        MySubEntity mySubEntity = MySubEntity.builder()
                .userId(userId)
                .subId(subId)
                .build();
        mySubRepository.save(mySubEntity);
    }

    @Override
    public boolean isSubscribed(String userId, Long subId) {
        return mySubRepository.existsByUserIdAndSubId(userId, subId);
    }
}

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/out/repo/IMySubRepository.java
package com.subride.mysub.infra.out.repo;

import com.subride.mysub.infra.out.entity.MySubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IMySubRepository extends JpaRepository<MySubEntity, Long> {
    List<MySubEntity> findByUserId(String userId);
    Optional<MySubEntity> findByUserIdAndSubId(String userId, Long subId);
    boolean existsByUserIdAndSubId(String userId, Long subId);
}

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/in/web/MySubController.java
package com.subride.mysub.infra.in.web;

import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.mysub.biz.usecase.inport.IMySubService;
import com.subride.mysub.infra.exception.InfraException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "마이구독 서비스 API")
@RestController
@RequestMapping("/api/my-subs")
@SecurityRequirement(name = "bearerAuth")    //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class MySubController {
    private final IMySubService mySubService;
    private final MySubControllerHelper mySubControllerHelper;

    @Operation(summary = "사용자 가입 구독서비스 목록 리턴", description = "구독추천 서비스에 요청하여 구독서비스 정보를 모두 담아 리턴함")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<MySubInfoDTO>>> getMySubList(@RequestParam String userId) {
        try {
            List<MySubInfoDTO> mySubInfoDTOList = mySubService.getMySubList(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 목록 조회 성공", mySubInfoDTOList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "가입 구독서비스 중 썹그룹에 참여하지 않은 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/not-join-group")
    public ResponseEntity<ResponseDTO<List<MySubInfoDTO>>> getNotJoinGroupSubList(@RequestParam String userId) {
        try {
            List<MySubInfoDTO> mySubInfoDTOList = mySubService.getMySubList(userId);
            List<MySubInfoDTO> notJoinGroupSubList = mySubInfoDTOList.stream()
                    .filter(mySubInfoDTO -> !mySubInfoDTO.isJoinGroup())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 목록 조회 성공", notJoinGroupSubList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독 취소", description = "구독서비스를 삭제합니다.")
    @Parameters({
            @Parameter(name = "subId", in = ParameterIn.PATH, description = "구독서비스 ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자 ID", required = true)
    })
    @DeleteMapping("/{subId}")
    public ResponseEntity<ResponseDTO<Void>> cancelSub(@PathVariable Long subId, @RequestParam String userId) {
        try {
            mySubService.cancelSub(subId, userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 취소 성공", null));
        }  catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독 등록", description = "구독서비스를 등록합니다.")
    @Parameters({
            @Parameter(name = "subId", in = ParameterIn.PATH, description = "구독서비스 ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자 ID", required = true)
    })
    @PostMapping("/{subId}")
    public ResponseEntity<ResponseDTO<Void>> subscribeSub(@PathVariable Long subId, @RequestParam String userId) {
        try {
            mySubService.subscribeSub(subId, userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 추가 성공", null));
        }  catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "구독여부 리턴", description = "사용자가 구독서비스를 가입했는지 여부를 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자 ID", required = true),
            @Parameter(name = "subId", in = ParameterIn.QUERY, description = "구독서비스 ID", required = true)
    })
    @GetMapping("/checking-subscribe")
    public ResponseEntity<ResponseDTO<Boolean>> checkSubscription(
            @RequestParam String userId,
            @RequestParam Long subId) {
        try {
            boolean isSubscribed = mySubService.checkSubscription(userId, subId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "구독 여부 확인 성공", isSubscribed));
        }  catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "사용자의 가입 서비스ID 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping("/sub-id-list")
    public ResponseEntity<ResponseDTO<List<Long>>> getMySubIds(@RequestParam String userId) {
        try {
            List<Long> mySubIds = mySubControllerHelper.getMySubIds(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "사용자 가입 서비스 ID 리턴", mySubIds));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }
}

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/in/web/MySubControllerHelper.java
package com.subride.mysub.infra.in.web;

import com.subride.mysub.infra.out.entity.MySubEntity;
import com.subride.mysub.infra.out.repo.IMySubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySubControllerHelper {
    private final IMySubRepository mySubRepository;

    public List<Long> getMySubIds(String userId) {
        List<MySubEntity> mySubEntityList = mySubRepository.findByUserId(userId);
        return mySubEntityList.stream()
                .map(MySubEntity::getSubId)
                .collect(Collectors.toList());
    }
}

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/jwt/JwtAuthenticationInterceptor.java
package com.subride.mysub.infra.common.jwt;

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

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/jwt/JwtAuthenticationFilter.java
// CommonJwtAuthenticationFilter.java
package com.subride.mysub.infra.common.jwt;

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

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/jwt/JwtTokenProvider.java
// CommonJwtTokenProvider.java
package com.subride.mysub.infra.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.subride.mysub.infra.exception.InfraException;
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

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/config/SecurityConfig.java
package com.subride.mysub.infra.common.config;

import com.subride.mysub.infra.common.jwt.JwtAuthenticationFilter;
import com.subride.mysub.infra.common.jwt.JwtTokenProvider;
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
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/config/LoggingAspect.java
package com.subride.mysub.infra.common.config;

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

// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/common/config/SpringDocConfig.java
package com.subride.mysub.infra.common.config;

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
                        .title("마이구독 서비스 API")
                        .version("v1.0.0")
                        .description("마이구독 서비스 API 명세서입니다. "));
    }
}


// File: mysub/mysub-infra/src/main/java/com/subride/mysub/infra/exception/InfraException.java
package com.subride.mysub.infra.exception;

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

    public int getCode() {
        return code;
    }
}

// File: mysub/mysub-biz/build.gradle
dependencies {
    implementation project(':common')
}

// File: mysub/mysub-biz/src/main/java/com/subride/mysub/biz/dto/MySubDTO.java
package com.subride.mysub.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySubDTO {
    private String userId;
    private Long subId;
}

// File: mysub/mysub-biz/src/main/java/com/subride/mysub/biz/usecase/inport/IMySubService.java
package com.subride.mysub.biz.usecase.inport;

import com.subride.common.dto.MySubInfoDTO;

import java.util.List;

public interface IMySubService {
    List<MySubInfoDTO> getMySubList(String userId);
    void cancelSub(Long subId, String userId);
    void subscribeSub(Long subId, String userId);

    boolean checkSubscription(String userId, Long subId);
}

// File: mysub/mysub-biz/src/main/java/com/subride/mysub/biz/usecase/outport/IMySubProvider.java
package com.subride.mysub.biz.usecase.outport;

import com.subride.common.dto.MySubInfoDTO;

import java.util.List;

public interface IMySubProvider {

    List<MySubInfoDTO> getMySubList(String userId);
    void cancelSub(Long subId, String userId);
    void subscribeSub(Long subId, String userId);

    boolean isSubscribed(String userId, Long subId);
}

// File: mysub/mysub-biz/src/main/java/com/subride/mysub/biz/usecase/service/MySubServiceImpl.java
package com.subride.mysub.biz.usecase.service;

import com.subride.common.dto.MySubInfoDTO;
import com.subride.mysub.biz.domain.MySub;
import com.subride.mysub.biz.dto.MySubDTO;
import com.subride.mysub.biz.usecase.inport.IMySubService;
import com.subride.mysub.biz.usecase.outport.IMySubProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MySubServiceImpl implements IMySubService {
    private final IMySubProvider mySubProvider;

    @Override
    public List<MySubInfoDTO> getMySubList(String userId) {
        return mySubProvider.getMySubList(userId);
    }

    @Override
    public void cancelSub(Long subId, String userId) {
        mySubProvider.cancelSub(subId, userId);
    }

    @Override
    public void subscribeSub(Long subId, String userId) {
        mySubProvider.subscribeSub(subId, userId);
    }

    private MySubDTO toMySubDTO(MySub mySub) {
        MySubDTO mySubDTO = new MySubDTO();
        mySubDTO.setUserId(mySub.getUserId());
        mySubDTO.setSubId(mySub.getSubId());
        return mySubDTO;
    }

    @Override
    public boolean checkSubscription(String userId, Long subId) {
        return mySubProvider.isSubscribed(userId, subId);
    }
}

// File: mysub/mysub-biz/src/main/java/com/subride/mysub/biz/domain/MySub.java
package com.subride.mysub.biz.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySub {
    private String userId;
    private Long subId;
}

// File: mysub/mysub-biz/src/main/java/com/subride/mysub/biz/exception/BizException.java
package com.subride.mysub.biz.exception;

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

// Define a custom task to build all infra projects
tasks.register('buildAll') {
	dependsOn ':member:member-infra:build',
			':subrecommend:subrecommend-infra:build',
			':mysub:mysub-infra:build',
			':mygrp:mygrp-infra:build'
}
tasks.register('ms1') {
	dependsOn ':member:member-infra:build'
}
tasks.register('ms2') {
	dependsOn ':subrecommend:subrecommend-infra:build'
}
tasks.register('ms3') {
	dependsOn ':mysub:mysub-infra:build'
}
tasks.register('ms4') {
	dependsOn ':mygrp:mygrp-infra:build'
}
tasks.register('ms5') {
	dependsOn ':transfer:transfer-infra:build'
}



