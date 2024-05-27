// File: common/build.gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}

// File: common/src/main/java/com/subride/common/dto/ResponseDTO.java
package com.subride.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO<T> {
    private int code;
    private String message;
    private T response;
}


// File: common/src/main/java/com/subride/common/util/CommonUtils.java
package com.subride.common.util;

import com.subride.common.dto.*;

public class CommonUtils {

    //controller에서 성공 시 리턴 객체 반환
    public static <T> ResponseDTO<T> createSuccessResponse(int code, String message, T response) {
        return ResponseDTO.<T>builder()
                .code(code)
                .message(message)
                .response(response)
                .build();
    }

    //controller에서 실패 시 리턴 객체 반환
    public static <T> ResponseDTO<T> createFailureResponse(int code, String message) {
        return ResponseDTO.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}


// File: member/member-infra/build.gradle
dependencies {
    implementation project(':common')
    implementation project(':member:member-biz')
}

// File: member/member-infra/build/resources/main/application-test.yml
server:
  port: ${SERVER_PORT:18080}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:member-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///member}
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
  expiration-time: ${JWT_EXPIRATION_TIME:3600}
  refresh-token-expiration-time: ${REFRESH_TOKEN_EXPIRATION_TIME:36000}

# Logging
logging:
  level:
    root: INFO
    com.subride.member.infra.in: DEBUG
    com.subride.member.infra.out: DEBUG



// File: member/member-infra/build/resources/main/application.yml
server:
  port: ${SERVER_PORT:18080}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:member-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/member?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
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
  expiration-time: ${JWT_EXPIRATION_TIME:3600}
  refresh-token-expiration-time: ${REFRESH_TOKEN_EXPIRATION_TIME:36000}

# Logging
logging:
  level:
    root: INFO
    com.subride.member.infra.in: DEBUG
    com.subride.member.infra.out: DEBUG



// File: member/member-infra/src/test/java/com/subride/member/infra/out/adapter/AuthProviderImplComponentTest.java
package com.subride.member.infra.out.adapter;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.common.config.SecurityConfig;
import com.subride.member.infra.common.jwt.CustomUserDetailsService;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.in.web.CommonTestUtils;
import com.subride.member.infra.out.entity.AccountEntity;
import com.subride.member.infra.out.entity.MemberEntity;
import com.subride.member.infra.out.repo.IAccountRepository;
import com.subride.member.infra.out.repo.IMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

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
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///member",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})

/*
@DataJpaTest는 데이터 관련된 Bean만 로딩하므로 추가로 필요한 클래스는 Import 해 줘야 함
먼저 필요한 클래스를 추가하고 실행 시 에러 메시지를 보면서 추가해 나가면 됨
 */
@Import({SecurityConfig.class, JwtTokenProvider.class, CustomUserDetailsService.class})
class AuthProviderImplComponentTest {
    private final IMemberRepository memberRepository;
    private final IAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private AuthProviderImpl authProvider;

    @Autowired
    public AuthProviderImplComponentTest(IMemberRepository memberRepository, IAccountRepository accountRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @BeforeEach
    void setup() {
        authProvider = new AuthProviderImpl(authenticationManager, memberRepository, passwordEncoder, accountRepository);
    }

    @Test
    void signup_ValidInput_SavesMemberAndAccount() {
        // given
        Member member = CommonTestUtils.createMember();
        Account account = CommonTestUtils.createAccount();

        // when
        authProvider.signup(member, account);

        // then
        MemberEntity savedMember = memberRepository.findByUserId("testuser")
                .orElseThrow(() -> new InfraException("Member not found"));

        assertThat(savedMember.getUserId()).isEqualTo(member.getUserId());
        assertThat(savedMember.getUserName()).isEqualTo(member.getUserName());
        assertThat(savedMember.getBankName()).isEqualTo(member.getBankName());
        assertThat(savedMember.getBankAccount()).isEqualTo(member.getBankAccount());

        AccountEntity savedAccount = accountRepository.findByUserId("testuser")
                .orElseThrow(() -> new InfraException("Account not found"));

        assertThat(savedAccount.getUserId()).isEqualTo(account.getUserId());
        assertThat(passwordEncoder.matches(account.getPassword(), savedAccount.getPassword())).isTrue();
    }
}


// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/MemberControllerUnitTest.java
package com.subride.member.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.member.infra.dto.MemberInfoDTO;
import com.subride.member.infra.exception.InfraException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/*
단위 테스트 예시: Controller 테스트
주요 핵심
- 목적:
*/
@ExtendWith(MockitoExtension.class)
class MemberControllerUnitTest {

    @Mock
    private MemberControllerHelper memberControllerHelper;

    @InjectMocks
    private MemberController memberController;

    /* -- JUnit5의 새로운 기능인 @ExtendWith(MockitoExtension.class)을 사용하면 아래 강제 주입은 하면 안됨
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
     */

    //-- getMemberInfo 동작 테스트
    @Test
    void getMemberInfo_ValidUserId_ReturnsMemberInfoDTO() {
        // given
        String userId = "testUser";
        MemberInfoDTO expectedMemberInfo = new MemberInfoDTO();
        expectedMemberInfo.setUserId(userId);

        given(memberControllerHelper.getMemberInfo(userId)).willReturn(expectedMemberInfo);

        // when
        ResponseEntity<ResponseDTO<MemberInfoDTO>> response = memberController.getMemberInfo(userId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseDTO<MemberInfoDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedMemberInfo, responseBody.getResponse());

        verify(memberControllerHelper, times(1)).getMemberInfo(userId);
    }

    //-- 에러 처리 테스트
    @Test
    void getMemberInfo_InvalidUserId_ThrowsInfraException() {
        // given
        String userId = "invalidUser";

        given(memberControllerHelper.getMemberInfo(userId)).willThrow(new InfraException("User not found"));

        // when
        ResponseEntity<ResponseDTO<MemberInfoDTO>> response = memberController.getMemberInfo(userId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ResponseDTO<MemberInfoDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNull(responseBody.getResponse());

        verify(memberControllerHelper, times(1)).getMemberInfo(userId);
    }

    // Add more test cases for other methods and scenarios
}

// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerWithSpyComponentTest.java
// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerWithSpyComponentTest.java
package com.subride.member.infra.in.web;

import com.google.gson.Gson;
import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.service.AuthServiceImpl;
import com.subride.member.infra.common.config.SecurityConfig;
import com.subride.member.infra.dto.LoginRequestDTO;
import com.subride.member.infra.dto.SignupRequestDTO;
import com.subride.member.infra.common.jwt.CustomUserDetailsService;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.out.adapter.AuthProviderImpl;
import com.subride.member.infra.out.entity.AccountEntity;
import com.subride.member.infra.out.repo.IAccountRepository;
import com.subride.member.infra.out.repo.IMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    - 데이터 처리 관련한 객체는 @Mock을 이용하여 모의 객체로 생성(AuthProviderImpl객체 생성 위해 필요함)
*/
@WebMvcTest(AuthController.class)
/*
추가로 필요한 Bean객체 로딩
- Controller클래스에 Spring security가 적용되므로 SucurtyConfig를 Import하여 필요한 Bean객체를 로딩해야함
- JWT토큰 처리를 위해 JwtTokenProvider객체도 import해야 함
*/
@Import({SecurityConfig.class, JwtTokenProvider.class})
class AuthControllerWithSpyComponentTest {
    /*
    모의 Http 객체이며 Bocking방식을 지원함
    @WebMvcTest는 @Controller와 @ControllerAdvice로 생성된 Bean 클래스를 자동으로 생성함
    즉, AuthController 클래스는 자동으로 생성됨
    하지만 @Component, @Service, @Repository 등의 애노테이션이 붙은 클래스는 스캔하지 않기 때문에 자동 생성되지 않음
     */
    @Autowired
    private MockMvc mockMvc;

    /*
    AuthController가 의존하는 객체 생성. 실제 객체를 생성하므로 @SpyBean 어노테이션 사용
    Spy Bean객체는 인터페이스 객체가 아닌 실제 객체에 대해서만 생성할 수 있음
    */
    @SpyBean
    private AuthControllerHelper authControllerHelper;
    @SpyBean
    private AuthServiceImpl authService;
    //------------------------

    /*
    AuthServiceImpl객체가 직접 의존하는 객체는 IAuthProvider이지만 실제 AuthServiceImpl객체가 생성되기 위해서는
    구현 객체인 AuthProviderImpl객체가 필요함
    그리고 AuthProviderImpl객체 생성에는 아래와 같은 객체가 필요하기 때문에 모의 객체로 생성해 줘야 함

    private final AuthenticationManager authenticationManager;
    private final IMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAccountRepository accountRepository;

    */
    @MockBean
    private AuthProviderImpl authProvider;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private IMemberRepository memberRepository;
    @MockBean
    private IAccountRepository accountRepository;

    private final Gson gson = new Gson();

    @Test
    void signup_ValidInput_ReturnsSuccessResponse() throws Exception {
        // given
        SignupRequestDTO signupRequestDTO = CommonTestUtils.createSignupRequest();

        willDoNothing().given(authProvider).signup(any(Member.class), any(Account.class));

        // when, then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(signupRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.response").value("회원 가입 되었습니다."));
    }

    @Test
    void login_ValidCredentials_ReturnsJwtTokenDTO() throws Exception {
        // given
        LoginRequestDTO loginRequestDTO = CommonTestUtils.createLoginRequestDTO();

        //-- authService.login 수행 시 authProvider.validateToken을 호출하므로 스티빙 필요
        Member member = CommonTestUtils.createMember();
        given(authProvider.validateAuth(any(), any())).willReturn(member);

        //-- jwtTokenProvider.createToken할 때 DB접근 스터빙
        Account account = CommonTestUtils.createAccount();
        given(accountRepository.findByUserId(any())).willReturn(Optional.of(AccountEntity.fromDomain(account)));

        // when, then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.response.accessToken").exists())
                .andExpect(jsonPath("$.response.refreshToken").exists());
    }
}

// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerSystemTest.java
package com.subride.member.infra.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.dto.LoginRequestDTO;
import com.subride.member.infra.dto.SignupRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")     //application-test.yml의 설정을 적용
public class AuthControllerSystemTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient webClient;

    @BeforeEach
    void setup() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();
    }

    /*
    @SprintBootTest는 기본적으로 @Transactional이 적용되어 테스트가 끝나면 데이터를 지움
    뒤에 Login 테스트를 위해 지우지 않도록 설정함
     */
    @Test
    void signup_success() {
        // Given
        SignupRequestDTO signupRequestDTO = CommonTestUtils.createSignupRequest();

        // When & Then
        webClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDTO.class)
                .value(response -> {
                    assert response.getCode() == 200;
                    assert response.getMessage().equals("회원가입 성공");
                    assert response.getResponse().equals("회원 가입 되었습니다.");
                });
    }

    @Test
    @WithMockUser
    void signup_invalidRequest_badRequest() {
        // Given
        SignupRequestDTO signupRequestDTO = CommonTestUtils.createSignupRequest();
        signupRequestDTO.setUserId(null); // 잘못된 요청 데이터

        // When & Then
        webClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequestDTO)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void login_success() {
        // Given
        SignupRequestDTO signupRequestDTO = CommonTestUtils.createSignupRequest();
        webClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupRequestDTO)
                .exchange()
                .expectStatus().isOk();

        LoginRequestDTO loginRequestDTO = CommonTestUtils.createLoginRequestDTO();

        // When & Then
        webClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDTO.class)
                .value(response -> {
                    assert response.getCode() == 200;
                    assert response.getMessage().equals("로그인 성공");

                    JwtTokenDTO jwtToken = objectMapper.convertValue(response.getResponse(), JwtTokenDTO.class);
                    assert jwtToken.getAccessToken() != null;
                    assert jwtToken.getRefreshToken() != null;
                });
    }

    @Test
    void login_unauthorized_unauthorized() {
        // Given
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUserId("testuser");
        loginRequestDTO.setPassword("wrongpassword");

        // When & Then
        webClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/CommonTestUtils.java
package com.subride.member.infra.in.web;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.dto.LoginRequestDTO;
import com.subride.member.infra.dto.SignupRequestDTO;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.out.entity.MemberEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class CommonTestUtils {
    public CommonTestUtils() {}

    // 테스트 데이터 생성 메서드
    public static SignupRequestDTO createSignupRequest() {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setUserId("testuser");
        signupRequestDTO.setPassword("password");
        signupRequestDTO.setUserName("홍길동");
        signupRequestDTO.setBankName("KB");
        signupRequestDTO.setBankAccount("123-12222");
        return signupRequestDTO;
    }

    public static LoginRequestDTO createLoginRequestDTO() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUserId("testuser");
        loginRequestDTO.setPassword("password");
        return loginRequestDTO;
    }

    public static Member createMember() {
        Member member = new Member();
        member.setUserId("testuser");
        member.setUserName("홍길동");
        member.setBankName("KB");
        member.setBankAccount("123-11111");
        member.setCharacterId(1);
        return member;
    }

    public static Account createAccount() {
        Account account = new Account();
        account.setUserId("testuser");
        account.setPassword("password");
        account.setRoles(new HashSet<>(Arrays.asList("USER", "LEADER")));
        return account;
    }

    public static String getTestJwtSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[64];
        random.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }

    public static JwtTokenDTO createTestToken(JwtTokenProvider jwtTokenProvider) {
        // 테스트 객체 생성
        Member member = CommonTestUtils.createMember();
        Account account = CommonTestUtils.createAccount();

        // 사용자의 권한 정보 생성
        Collection<? extends GrantedAuthority> authorities = account.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 새로운 액세스 토큰 생성
        MemberEntity memberEntity = MemberEntity.fromDomain(member);
        return jwtTokenProvider.createToken(memberEntity, authorities);
    }
}


// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerComponentTest.java
// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerComponentTest.java
package com.subride.member.infra.in.web;

import com.google.gson.Gson;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.inport.IAuthService;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.dto.LoginRequestDTO;
import com.subride.member.infra.dto.SignupRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
컴포넌트 테스트 예시: Controller 테스트
- 목적:
    - API End point 호출: Http 요청에 매핑된 API가 호출되는지 검증
    - 리턴값 검증: API 결과값이 잘 리턴되는지 테스트
- 방법:
    - MockMVC로 Http요청을 모방하고, API실행은 모의 Bean객체를 생성하여 Stubbing함으로써 실제 수행을 대체함
    - MockBean을 생성된 객체는 아무것도 안하고 null을 리턴(단, void 리턴일때는 아무것도 안함)
*/
@WebMvcTest(AuthController.class)
class AuthControllerComponentTest {
    /*
    모의 Http 객체이며 Bocking방식을 지원함
    @WebMvcTest는 @Controller와 @ControllerAdvice로 생성된 Bean 클래스를 자동으로 생성함
    즉, AuthController 클래스는 자동으로 생성됨
    하지만 @Component, @Service, @Repository 등의 애노테이션이 붙은 클래스는 스캔하지 않기 때문에 자동 생성되지 않음
     */
    @Autowired
    private MockMvc mockMvc;

    //-- AuthController가 의존하는 Bean객체 생성
    @MockBean
    private IAuthService authService;
    @MockBean
    private AuthControllerHelper authControllerHelper;
    //---------------

    private final Gson gson = new Gson();

    //-- API가 Spring security를 사용하므로 테스트를 위한 설정을 함
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/**").permitAll()
                            .anyRequest().authenticated()
                    );
            return http.build();
        }
    }

    /*
    /api/auth/signup 요청 시 매핑된 authController.signup이 잘 호출되고, 리턴값이 정상적인지 테스트한다.
    authController.signup 메서드는 내부적으로 authService.signup 메서드를 호출하는데,
    authService는 @MockBean으로 모의 객체로 주입되었기 때문에 실제 로직은 수행되지 않고 가정된 동작만 수행한다.
    @Mockbean으로 생성된 모의 객체는 어떠한 요청이든 수행 없이 null을 반환한다. 단, 리턴값이 void인 경우는 아무것도 하지 않는다.
    authService.signup은 void를 리턴하기 때문에 아무런 수행과 에러 없이 처리되게 된다.
    */
    @Test
    void signup_ValidInput_ReturnsSuccessResponse() throws Exception {
        // given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();

        // when, then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(signupRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.response").value("회원 가입 되었습니다."));
    }

    /*
    - 목적: 로그인 요청 시 API End point 테스트와 결과값 유효성 검증
    - 방법: 로그인 시 실제 수행인 authService.login과 authControllerHelper.createToken을 모의수행으로 스터빙
    */
    @Test
    void login_ValidCredentials_ReturnsJwtTokenDTO() throws Exception {
        // given
        LoginRequestDTO loginRequestDTO = CommonTestUtils.createLoginRequestDTO();
        Member member = CommonTestUtils.createMember();

        JwtTokenDTO jwtTokenDTO = JwtTokenDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        given(authService.login("testuser", "password")).willReturn(member);
        given(authControllerHelper.createToken(any())).willReturn(jwtTokenDTO);

        // when, then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.response.accessToken").value(jwtTokenDTO.getAccessToken()))
                .andExpect(jsonPath("$.response.refreshToken").value(jwtTokenDTO.getRefreshToken()));
    }

}

// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerUnitTest.java
package com.subride.member.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.inport.IAuthService;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.dto.JwtTokenRefreshDTO;
import com.subride.member.infra.dto.JwtTokenVarifyDTO;
import com.subride.member.infra.dto.SignupRequestDTO;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

/*
단위 테스트 코드 예제
- 목적: Service레어어로 요청 처리와 결과 반환 처리를 검증
- Service레이어 객체는 모의객체를 사용
*/

//-- 단위 테스트에는 모의 객체 생성 및 관리를 편하게 하기 위해 MockitoExtension을 사용함
@ExtendWith(MockitoExtension.class)
/*
추가로 필요한 Bean객체 로딩
- Controller클래스에 Spring security가 적용되므로 SucurtyConfig를 Import하여 필요한 Bean객체를 로딩해야함
- JWT토큰 처리를 위해 JwtTokenProvider객체도 import해야 함
*/
public class AuthControllerUnitTest {
    //-- 필요한 모의 객체를 생성
    @Mock
    private IAuthService authService;

    //-------------------------------------

    //--- AuthControllerHelper에서 사용할 CommonJwtTokenProvider 객체 생성
    private String jwtSecret = CommonTestUtils.getTestJwtSecret();
    private long jwtExpirationTime = 3600;
    private long jwtRefreshTokenExpirationTime = 36000;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private JwtTokenDTO jwtTokenDTO;
    //------------------

    //-- 모의 객체가 아닌 실제 객체를 생성하고, 실제 객체의 일부 메소드 결과를 대체하기 위해 Spy객체를 생성
    //-- Spy 대상 객체는 반드시 빈 생성자 메소드를 제공해야 함
    @Spy
    private AuthControllerHelper authControllerHelper;

    //-- 생성된 모의 객체와 Spy객체를 필요한 객체에 주입
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        /*
        Spy객체를 수동으로 생성하여 auController에 주입하는 예시임
        만약 Spy객체를 생성할 대상 객체가 빈 생성자를 제공하지 않는다면, 이 방법을 써야함
        이때는 위에서 @spy와 @InjectMocks 어노테이션을 제거해야 함
        */
        /*
        //-- 모의 객체가 아닌 실제 객체를 생성하고, 실제 객체의 일부 메소드 결과를 대체하기 위해 Mockito.spy를 이용
        authControllerHelper = Mockito.spy(new AuthControllerHelper(jwtTokenProvider, memberRepository, accountRepository));
        //-- authService는 모의 객체이고, authControllerHelper객체는 실제 객체를 감싼 Spy객체임
        authController = new AuthController(authService, authControllerHelper);
        */
        jwtTokenProvider = new JwtTokenProvider(jwtSecret, jwtExpirationTime, jwtRefreshTokenExpirationTime);
        authControllerHelper.setJwtTokenProvider(jwtTokenProvider);
        jwtTokenDTO = CommonTestUtils.createTestToken(jwtTokenProvider);
    }
    //-----------------------------

    /*
    authController.signup 호출 시 정상적으로 처리를 위한 메소드가 1번 호출되는지만 확인
    */
    @Test
    void signup_test_call() {
        //given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();

        Member member = new Member();
        Account account = new Account();

        /*
        Stub 처리: 일부 메소드를 실제 수행하지 않고 지정된 값을 리턴하도록 대체함
        Spy객체 authControllerHelper의 메소드 결과를 위에서 생성한 member와 account로 대체
        Spy객체를 Stub할 때는 doReturn().when()문을 써야 문제가 안 생김
         */
        doReturn(member).when(authControllerHelper).getMemberFromRequest(signupRequestDTO);
        doReturn(account).when(authControllerHelper).getAccountFromRequest(signupRequestDTO);

        /*
        모의 객체 authService의 signup메소드를 Stub함
        authService.signup의 결과가 void이기 때문에 willDoNothing을 사용
        모의 객체를 Stub할 때는 BDDMockito에서 권장하는 given을 사용해도 되고, Mockito에서 제공하는 when을 사용해도 되는데 given이 더 권장됨
         */
        willDoNothing().given(authService).signup(eq(member), eq(account));
        //doNothing().when(authService).signup(eq(member), eq(account));

        //when
        ResponseEntity<ResponseDTO<String>> response = authController.signup(signupRequestDTO);

        //then
        //-- 지정한 객체의 메소드가 지정한 횟수만큰 호출되었는지 검증
        verify(authControllerHelper, times(1)).getMemberFromRequest(any(SignupRequestDTO.class));
        verify(authControllerHelper, times(1)).getAccountFromRequest(any(SignupRequestDTO.class));
        verify(authService, times(1)).signup(any(Member.class), any(Account.class));
    }

    /*
    authController의 signup 메소드 테스트
    authController.signup 메소드 내에서 수행되는 아래 작업들을 테스트 함
    단, 이때 authService.signup은 Stub함

    authService.signup(authControllerHelper.getMemberFromRequest(signupRequestDTO),
                    authControllerHelper.getAccountFromRequest(signupRequestDTO));

            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원가입 성공", "회원 가입 되었습니다."));
     */
    @Test
    void signup_ValidInput_ReturnSuccessResponse() {
        //given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();

        //-- authService.signup은 Stub하여 실제로는 수행되지 않게 함
        willDoNothing().given(authService).signup(any(Member.class), any(Account.class));

        //when
        ResponseEntity<ResponseDTO<String>> response = authController.signup(signupRequestDTO);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
        assertEquals("회원 가입 되었습니다.", response.getBody().getResponse());
    }

    /*
    login 단위 테스트 여부 결정
    - 대상: authService.login과 authControllerHelper.createToken
    - 결과: Database와 연결된 검증이 필요하여 단위테스트로 부적절
     */

    /*
    validate 단위 테스트
    - authControllerHelper.checkAccessToken 만 검증
    - authControllerHelper.getMemberFromToken은 DB연결이 필요하여 Stub함
     */
    @Test
    void validate_test_checkAccessToken() {
        //Given
        JwtTokenVarifyDTO jwtTokenVarifyDTO = new JwtTokenVarifyDTO();
        jwtTokenVarifyDTO.setToken(jwtTokenDTO.getAccessToken());

        Member member = new Member();
        member.setUserId("testuser");

        //-Stubbing
        doReturn(member).when(authControllerHelper).getMemberFromToken(jwtTokenVarifyDTO.getToken());
        given(authService.validateMemberAccess(member)).willReturn(true);

        //When
        /*
        주의사항: authConrollerHelper는 실제 실행되므로, 수행에 필요한 jwtTokenProvider도 실제 객체여야 함
        그래서, 위 setup()에서 authControllerHelper객체에 실제 생성한 jwtTokenProvider 객체를 셋팅하는 것임
         */
        ResponseEntity<ResponseDTO<Integer>> response = authController.validate(jwtTokenVarifyDTO);

        //Then
        assertEquals(200, response.getBody().getCode());
        assertEquals(1, response.getBody().getResponse());
    }

    /*
    refresh 테스트
    */
    @Test
    void refresh_test_isValidRefreshToken() {
        //Given
        JwtTokenRefreshDTO jwtTokenRefreshDTO = new JwtTokenRefreshDTO();
        jwtTokenRefreshDTO.setRefreshToken(jwtTokenDTO.getRefreshToken());

        Member member = new Member();
        doReturn(member).when(authControllerHelper).getMemberFromToken(any());
        doReturn(jwtTokenDTO).when(authControllerHelper).createToken(any());

        //When
        ResponseEntity<ResponseDTO<JwtTokenDTO>> response = authController.refresh(jwtTokenRefreshDTO);

        //Then
        assertEquals(200, response.getBody().getCode());
        assertEquals(jwtTokenDTO.getAccessToken(), response.getBody().getResponse().getAccessToken());
    }
}

// File: member/member-infra/src/main/resources/application-test.yml
server:
  port: ${SERVER_PORT:18080}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:member-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///member}
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
  expiration-time: ${JWT_EXPIRATION_TIME:3600}
  refresh-token-expiration-time: ${REFRESH_TOKEN_EXPIRATION_TIME:36000}

# Logging
logging:
  level:
    root: INFO
    com.subride.member.infra.in: DEBUG
    com.subride.member.infra.out: DEBUG



// File: member/member-infra/src/main/resources/application.yml
server:
  port: ${SERVER_PORT:18080}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:member-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/member?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
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
  expiration-time: ${JWT_EXPIRATION_TIME:3600}
  refresh-token-expiration-time: ${REFRESH_TOKEN_EXPIRATION_TIME:36000}

# Logging
logging:
  level:
    root: INFO
    com.subride.member.infra.in: DEBUG
    com.subride.member.infra.out: DEBUG



// File: member/member-infra/src/main/java/com/subride/member/MemberApplication.java
package com.subride.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableAspectJAutoProxy
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/dto/MemberInfoDTO.java
package com.subride.member.infra.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInfoDTO {
    private String userId;
    private String userName;
    private String bankName;
    private String bankAccount;
    private int characterId;
}

// File: member/member-infra/src/main/java/com/subride/member/infra/dto/JwtTokenVarifyDTO.java
package com.subride.member.infra.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenVarifyDTO {
	private String token;
}


// File: member/member-infra/src/main/java/com/subride/member/infra/dto/JwtTokenDTO.java
package com.subride.member.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtTokenDTO {
    private String accessToken;
    private String refreshToken;
}

// File: member/member-infra/src/main/java/com/subride/member/infra/dto/JwtTokenRefreshDTO.java
package com.subride.member.infra.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenRefreshDTO {
	private String refreshToken;
}


// File: member/member-infra/src/main/java/com/subride/member/infra/dto/LoginRequestDTO.java
package com.subride.member.infra.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
	private String userId;
	private String password;
}


// File: member/member-infra/src/main/java/com/subride/member/infra/dto/SignupRequestDTO.java
package com.subride.member.infra.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class SignupRequestDTO {
    private String userId;
    private String password;
    private Set<String> roles;
    private String userName;
    private String bankName;
    private String bankAccount;

}


// File: member/member-infra/src/main/java/com/subride/member/infra/out/entity/MemberEntity.java
package com.subride.member.infra.out.entity;

import com.subride.member.biz.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter @Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/*
@NoArgsConstructor(access = AccessLevel.PROTECTED)
- JPA는 인자없는 기본 생성자를 대부분 요구하기 때문에 필요
- Access Level을 PROTECTED로 하는 이유는 외부에서 new 키워드로 인스턴스 생성을 못하게 하기 위함임
@NoArgsConstructor(access = AccessLevel.PROTECTED)
- 모든 인자를 갖는 생성자를 생성
- Access level을 PRIVATE으로 하여 해당 클래스에서만 사용할 수 있도록 제한함
 */
public class MemberEntity {
    @Id
    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankAccount;

    private int characterId;

    /*
    왜 static으로 메소드를 만드는가?
    - Access level에 PROTECTED라 외부에서 new 키워드로 인스턴스 생성을 못하므로 static 메소드로 만듬
    - 외부에서는 MemberEntity persistentMember = MemberEntity.fromDomain(member)와 같이 사용
    */
    public static MemberEntity fromDomain(Member member) {
        return MemberEntity.builder()
                .userId(member.getUserId())
                .userName(member.getUserName())
                .bankName(member.getBankName())
                .bankAccount(member.getBankAccount())
                .characterId(member.getCharacterId())
                .build();
    }

    public Member toDomain() {
        Member member = new Member();
        member.setUserId(this.userId);
        member.setUserName(this.userName);
        member.setBankName(this.bankName);
        member.setBankAccount(this.bankAccount);
        member.setCharacterId(this.characterId);
        return member;
    }
}

// File: member/member-infra/src/main/java/com/subride/member/infra/out/entity/AccountEntity.java
package com.subride.member.infra.out.entity;

import com.subride.member.biz.domain.Account;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/*
@NoArgsConstructor(access = AccessLevel.PROTECTED)
- JPA는 인자없는 기본 생성자를 대부분 요구하기 때문에 필요
- Access Level을 PROTECTED로 하는 이유는 외부에서 new 키워드로 인스턴스 생성을 못하게 하기 위함임
@NoArgsConstructor(access = AccessLevel.PROTECTED)
- 모든 인자를 갖는 생성자를 생성
- Access level을 PRIVATE으로 하여 해당 클래스에서만 사용할 수 있도록 제한함
 */
public class AccountEntity {
    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    /*
    왜 static으로 메소드를 만드는가?
    - Access level에 PROTECTED라 외부에서 new 키워드로 인스턴스 생성을 못하므로 static 메소드로 만듬
    - 외부에서는 AccountEntity persistentAccount = AccountEntity.fromDomain(account)와 같이 사용
     */
    public static AccountEntity fromDomain(Account account) {
        return new AccountEntity(
                account.getUserId(),
                account.getPassword(),
                account.getRoles()
        );
    }

    public Account toDomain() {
        Account account = new Account();
        account.setUserId(this.userId);
        account.setPassword(this.password);
        account.setRoles(this.roles);
        return account;
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/out/adapter/AuthProviderImpl.java
package com.subride.member.infra.out.adapter;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.outport.IAuthProvider;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.AccountEntity;
import com.subride.member.infra.out.entity.MemberEntity;
import com.subride.member.infra.out.repo.IAccountRepository;
import com.subride.member.infra.out.repo.IMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthProviderImpl implements IAuthProvider {
    private final AuthenticationManager authenticationManager;
    private final IMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final IAccountRepository accountRepository;

    @Override
    public Member validateAuth(String userId, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, password));
        } catch (BadCredentialsException e) {
            throw new InfraException("ID/PW 검증 실패", e);
        } catch (Exception e) {
            throw new InfraException("ID/PW 검증 실패", e);
        }

        Optional<MemberEntity> optionalPersistentMember = memberRepository.findByUserId(userId);
        return optionalPersistentMember.map(MemberEntity::toDomain).orElse(null);
    }

    @Override
    @Transactional
    public void signup(Member member, Account account) {
        try {
            MemberEntity memberEntity = MemberEntity.fromDomain(member);
            memberRepository.save(memberEntity);

            AccountEntity accountEntity = AccountEntity.fromDomain(account);
            //-- 암호를 단방향 암호화함
            accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
            accountRepository.save(accountEntity);
        } catch (Exception e) {
            throw new InfraException("데이터 저장 중 오류", e);
        }

    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/out/repo/IMemberRepository.java
package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository //생략해도 됨. Spring Data JPA가 자동으로 Bin객체로 생성해 줌
public interface IMemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUserId(String userId);
    List<MemberEntity> findByUserIdIn(List<String> userIdList);
}

// File: member/member-infra/src/main/java/com/subride/member/infra/out/repo/IAccountRepository.java
package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository     //생략해도 됨. Spring Data JPA가 자동으로 Bin객체로 생성해 줌
public interface IAccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUserId(String userId);
    Boolean existsByUserId(String userId);
}

// File: member/member-infra/src/main/java/com/subride/member/infra/in/web/AuthControllerHelper.java
package com.subride.member.infra.in.web;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.dto.SignupRequestDTO;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.AccountEntity;
import com.subride.member.infra.out.entity.MemberEntity;
import com.subride.member.infra.out.repo.IAccountRepository;
import com.subride.member.infra.out.repo.IMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class AuthControllerHelper {
    private JwtTokenProvider jwtTokenProvider;
    private IMemberRepository memberRepository;
    private IAccountRepository accountRepository;

    @Autowired
    public AuthControllerHelper(JwtTokenProvider jwtTokenProvider, IMemberRepository memberRepository, IAccountRepository accountRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
    }

    public AuthControllerHelper() {}

    public void setJwtTokenProvider(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public JwtTokenDTO createToken(Member member) {
        // 사용자의 계정 정보 가져오기
        AccountEntity account = accountRepository.findByUserId(member.getUserId())
                .orElseThrow(() -> new InfraException("Account not found"));

        // 사용자의 권한 정보 생성
        Collection<? extends GrantedAuthority> authorities = account.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 새로운 액세스 토큰 생성
        MemberEntity memberEntity = MemberEntity.fromDomain(member);
        return jwtTokenProvider.createToken(memberEntity, authorities);
    }

    public int checkAccessToken(String token) {
        //log.info("*** checkAccessToken: {}", token);
        return jwtTokenProvider.validateToken(token);
    }

    public boolean isValidRefreshToken(String token) {
        return jwtTokenProvider.validateRefreshToken(token);
    }

    public Member getMemberFromRequest(SignupRequestDTO signupRequestDTO) {
        Member member = new Member();
        member.setUserId(signupRequestDTO.getUserId());
        member.setUserName(signupRequestDTO.getUserName());
        member.setBankName(signupRequestDTO.getBankName());
        member.setBankAccount(signupRequestDTO.getBankAccount());
        return member;
    }

    public Account getAccountFromRequest(SignupRequestDTO signupRequestDTO) {
        //log.info("*** getAccountFromRequest");
        Account account = new Account();
        account.setUserId(signupRequestDTO.getUserId());
        account.setPassword(signupRequestDTO.getPassword());
        account.setRoles(signupRequestDTO.getRoles());
        return account;
    }

    public Member getMemberFromToken(String token) {
        //log.info("*** getMemberFromToken");
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new InfraException("User not found"));
        return member.toDomain();
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/in/web/AuthController.java
package com.subride.member.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.inport.IAuthService;
import com.subride.member.infra.dto.*;
import com.subride.member.infra.exception.InfraException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
개발표준: 콘트롤러에는 Service와 Controller Helper 클래스만 사용함
*/
@Slf4j
@Tag(name = "Auth API", description = "인증/인가 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final IAuthService authService;
    private final AuthControllerHelper authControllerHelper;

    @Operation(operationId = "auth-signup", summary = "회원가입", description = "회원가입을 처리합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO<String>> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        try {
            authService.signup(authControllerHelper.getMemberFromRequest(signupRequestDTO),
                    authControllerHelper.getAccountFromRequest(signupRequestDTO));

            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원가입 성공", "회원 가입 되었습니다."));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(operationId = "auth-login", summary = "로그인", description = "로그인 처리")
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<JwtTokenDTO>> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            Member member = authService.login(loginRequestDTO.getUserId(), loginRequestDTO.getPassword());
            if (member != null) {
                JwtTokenDTO jwtTokenDTO = authControllerHelper.createToken(member);
                return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "로그인 성공", jwtTokenDTO));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(0, "로그인 실패"));
            }
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(operationId = "validate-token", summary = "인증 토큰 검증", description = "인증 토큰을 검증합니다.")
    @PostMapping("/verify")
    public ResponseEntity<ResponseDTO<Integer>> validate(@RequestBody JwtTokenVarifyDTO jwtTokenVarifyDTO) {
        try {
            log.info("** verify: {}", jwtTokenVarifyDTO.getToken());
            int result = authControllerHelper.checkAccessToken(jwtTokenVarifyDTO.getToken());
            log.info("** RESULT: {}", result);
            Member member = authControllerHelper.getMemberFromToken(jwtTokenVarifyDTO.getToken());

            if (member == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(0, "사용자 없음"));
            }

            if (authService.validateMemberAccess(member)) {
                return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "토큰 검증 성공", result));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonUtils.createFailureResponse(0, "접근 권한이 없습니다."));
            }
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @Operation(operationId = "refresh-token", summary = "인증 토큰 갱신", description = "인증 토큰을 갱신합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<JwtTokenDTO>> refresh(@RequestBody JwtTokenRefreshDTO jwtTokenRefreshDTO) {
        try {
            authControllerHelper.isValidRefreshToken(jwtTokenRefreshDTO.getRefreshToken());
            Member member = authControllerHelper.getMemberFromToken(jwtTokenRefreshDTO.getRefreshToken());
            if (member == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonUtils.createFailureResponse(0, "사용자 없음"));
            }

            JwtTokenDTO jwtTokenDTO = authControllerHelper.createToken(member);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "토큰 갱신 성공", jwtTokenDTO));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/in/web/MemberController.java
package com.subride.member.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.member.infra.dto.MemberInfoDTO;
import com.subride.member.infra.exception.InfraException;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "MemberEntity API", description = "회원 관련 API")
public class MemberController {
    private final MemberControllerHelper memberControllerHelper;

    @GetMapping("/{userId}")
    @Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회한다.")
    public ResponseEntity<ResponseDTO<MemberInfoDTO>> getMemberInfo(@PathVariable String userId) {
        log.info("회원정보 조회***************");
        try {
            MemberInfoDTO memberInfoDTO = memberControllerHelper.getMemberInfo(userId);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원정보", memberInfoDTO));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

    @GetMapping
    @Operation(summary = "회원 정보 리스트 조회", description = "여러 회원의 정보를 조회한다.")
    @Parameters({
            @Parameter(name = "userIds", in = ParameterIn.QUERY, description = "사용자ID(콤마로 구분)", required = true)
    })
    public ResponseEntity<ResponseDTO<List<MemberInfoDTO>>> getUserInfoList(@RequestParam String userIds) {
        List<String> userIdList = Arrays.asList(userIds.replaceAll("\\s", "").split(","));

        try {
            List<MemberInfoDTO> memberInfoDTOList = memberControllerHelper.getMemberInfoList(userIdList);
            return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "회원정보 리스트", memberInfoDTOList));
        } catch (InfraException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonUtils.createFailureResponse(0, "서버 오류가 발생했습니다."));
        }
    }

}


// File: member/member-infra/src/main/java/com/subride/member/infra/in/web/MemberControllerHelper.java
package com.subride.member.infra.in.web;

import com.subride.member.infra.dto.MemberInfoDTO;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.MemberEntity;
import com.subride.member.infra.out.repo.IMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberControllerHelper {
    public final IMemberRepository memberRepository;

    public MemberInfoDTO getMemberInfo(String userId) {
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new InfraException("사용자 없음"));

        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        BeanUtils.copyProperties(member, memberInfoDTO);
        return memberInfoDTO;
    }

    public List<MemberInfoDTO> getMemberInfoList(List<String> userIdList) {
        List<MemberEntity> memberList = memberRepository.findByUserIdIn(userIdList);

        if (memberList.isEmpty()) {
            throw new InfraException("검색할 회원정보 없음");
        }

        List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();
        for (MemberEntity member : memberList) {
            MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
            BeanUtils.copyProperties(member, memberInfoDTO);
            memberInfoDTOList.add(memberInfoDTO);
        }

        return memberInfoDTOList;
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/jwt/JwtAuthenticationFilter.java
package com.subride.member.infra.common.jwt;

import com.subride.member.infra.common.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
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
        String token = jwtTokenProvider.resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token) == 1) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/jwt/JwtTokenProvider.java
package com.subride.member.infra.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.MemberEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Algorithm algorithm;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-time}") long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-expiration-time}") long refreshTokenValidityInMilliseconds) {
        this.algorithm = Algorithm.HMAC512(secretKey);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * 1000;
    }

    public JwtTokenDTO createToken(MemberEntity memberEntity, Collection<? extends GrantedAuthority> authorities) {
        try {
            Date now = new Date();
            Date accessTokenValidity = new Date(now.getTime() + accessTokenValidityInMilliseconds);
            Date refreshTokenValidity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

            String accessToken = JWT.create()
                    .withSubject(memberEntity.getUserId())
                    .withClaim("userId", memberEntity.getUserId())
                    .withClaim("userName", memberEntity.getUserName())
                    .withClaim("bankName", memberEntity.getBankName())
                    .withClaim("bankAccount", memberEntity.getBankAccount())
                    .withClaim("characterId", memberEntity.getCharacterId())
                    .withClaim("auth", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .withIssuedAt(now)
                    .withExpiresAt(accessTokenValidity)
                    .sign(algorithm);

            String refreshToken = JWT.create()
                    .withSubject(memberEntity.getUserId())
                    .withIssuedAt(now)
                    .withExpiresAt(refreshTokenValidity)
                    .sign(algorithm);

            return JwtTokenDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch(Exception e) {
            throw new InfraException(0, e.getMessage(), e);
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(refreshToken);
            return true;
        } catch (Exception e) {
            throw new InfraException(0, e.getMessage(), e);
        }
    }

    public String getUserIdFromToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(refreshToken);
            return decodedJWT.getSubject();
        } catch (Exception e) {
            throw new InfraException(0, "Invalid refresh token", e);
        }
    }

    public String resolveToken(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        } catch (Exception e) {
            throw new InfraException(0, "Invalid refresh token", e);
        }
    }

    public int validateToken(String token) {
        log.info("******** validateToken: {}", token);
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return 1; // 검사 성공 시 1 반환
        } catch (TokenExpiredException e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            throw new InfraException(10, "토큰이 만료되었습니다.");
        } catch (SignatureVerificationException e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            throw new InfraException(20, "서명 검증에 실패했습니다.");
        } catch (AlgorithmMismatchException e) {
            log.error("AlgorithmMismatchException: {}", e.getMessage(), e);
            throw new InfraException(30, "알고리즘이 일치하지 않습니다.");
        } catch (InvalidClaimException e) {
            log.error("InvalidClaimException: {}", e.getMessage(), e);
            throw new InfraException(40, "유효하지 않은 클레임입니다.");
        } catch (Exception e) {
            log.error("Undefined Error: {}", e.getMessage(), e);
            throw new InfraException(50, "토큰 검증 중 예외가 발생했습니다.");
        }
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
            throw new InfraException(0, "Invalid refresh token", e);
        }
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/jwt/CustomUserDetailsService.java
package com.subride.member.infra.common.jwt;

import com.subride.member.infra.out.entity.AccountEntity;
import com.subride.member.infra.out.repo.IAccountRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IAccountRepository accountJpaRepository;

    public CustomUserDetailsService(IAccountRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        AccountEntity account = accountJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

        Set<GrantedAuthority> authorities = account.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return User.builder()
                .username(account.getUserId())
                .password(account.getPassword())
                .authorities(authorities)
                .build();
    }
}



// File: member/member-infra/src/main/java/com/subride/member/infra/common/util/MemberCommonUtils.java
package com.subride.member.infra.common.util;

public class MemberCommonUtils {
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/config/SecurityConfig.java
package com.subride.member.infra.common.config;

import com.subride.member.infra.common.jwt.JwtAuthenticationFilter;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.common.jwt.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(customUserDetailsService)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// File: member/member-infra/src/main/java/com/subride/member/infra/common/config/LoggingAspect.java
package com.subride.member.infra.common.config;

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

// File: member/member-infra/src/main/java/com/subride/member/infra/common/config/SpringDocConfig.java
package com.subride.member.infra.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unused")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("회원 서비스 API")
                        .version("v1.0.0")
                        .description("회원 서비스 API 명세서입니다."));
    }
}

// File: member/member-infra/src/main/java/com/subride/member/infra/exception/InfraException.java
package com.subride.member.infra.exception;

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

    public int getCode() { return code; }
}


// File: member/member-biz/build.gradle
dependencies {
    implementation project(':common')
}

// File: member/member-biz/src/test/java/com/subride/member/biz/usecase/service/AuthServiceImplTest.java
package com.subride.member.biz.usecase.service;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.outport.IAuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/*
단위 테스트 예시: Service 테스트
*/
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    //-- IAuthProvider 모의 객체를 생성
    @Mock
    private IAuthProvider authProvider;

    /*
    - AuthServiceImpl 객체에 IAuthProvider 모의 객체 주입
    - AuthServiceImpl 객체 안에서 IAuthProvider를 호출하는 부분에서 실제 객체가 아닌 이 모의객체가 실행 됨
    */
    @InjectMocks
    private AuthServiceImpl authService;

    /* -- JUnit5의 새로운 기능인 @ExtendWith(MockitoExtension.class)을 사용하면 아래 강제 주입은 하면 안됨
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
     */

    @Test
    void login_ValidCredentials_ReturnsMember() {
        // given
        String userId = "hiondal";
        String password = "0srilove$";
        Member expectedMember = new Member();
        expectedMember.setUserId(userId);

        //-- when안에 지정한 메소드가 호출될 때 그 결과값을 지정함. 아래에서는 위에서 만든 expectedMember객체가 리턴됨
        given(authProvider.validateAuth(userId, password)).willReturn(expectedMember);

        /*
         when: 테스트 할 login메소드를 호출하여 결과값을 받음
         - authService.login을 하면 authProvider.validateAuth메소드가 호출됨
         - 이때 바로 위의 when().thenReturn() 설정에 따라 expectedMember가 리턴됨
         */
        Member result = authService.login(userId, password);

        /*
         then: 결과를 검증함
         - assertEquals: authService를 실행한 결과값과 기대값이 동일한지 검증. 이 경우 login메소드에 문제가 없다면 당연히 되야 함
         - verify: authService.login 수행 시 authProvider.validateAuth함수가 1번만 호출되었는지 검증
         */
        assertEquals(expectedMember, result);
        verify(authProvider, times(1)).validateAuth(userId, password);
    }

    @Test
    void signup_ValidInput_CallsAuthProviderSignup() {
        // given
        Member member = new Member();
        Account account = new Account();
        member.setCharacterId((int) (Math.random() * 4) + 1);

        // when
        authService.signup(member, account);

        // then: 위 signup수행 시에 authProvider.signup함수가 1번만 호출되었는지 검증
        verify(authProvider, times(1)).signup(member, account);
    }

    // Add more test cases for edge cases and error scenarios
}

// File: member/member-biz/src/main/java/com/subride/member/biz/usecase/inport/IAuthService.java
package com.subride.member.biz.usecase.inport;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;

public interface IAuthService {
    void signup(Member member, Account account);
    Member login(String userId, String password);

    boolean validateMemberAccess(Member member);     //-- 접근 허용 정책 결정
}


// File: member/member-biz/src/main/java/com/subride/member/biz/usecase/outport/IAuthProvider.java
package com.subride.member.biz.usecase.outport;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;

public interface IAuthProvider {
    Member validateAuth(String userId, String password);

    void signup(Member member, Account account);

}


// File: member/member-biz/src/main/java/com/subride/member/biz/usecase/service/AuthServiceImpl.java
package com.subride.member.biz.usecase.service;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.inport.IAuthService;
import com.subride.member.biz.usecase.outport.IAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final IAuthProvider authProvider;

    @Override
    public Member login(String userId, String password) {
        return authProvider.validateAuth(userId, password);
    }

    @Override
    public void signup(Member member, Account account) {
        //-- profile image 번호를 생성
        member.setCharacterId((int) (Math.random() * 4) + 1);

        authProvider.signup(member, account);
    }

    @Override
    public boolean validateMemberAccess(Member member) {
        return member.canbeAccessed();
    }
}


// File: member/member-biz/src/main/java/com/subride/member/biz/domain/Account.java
package com.subride.member.biz.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Account {
    private String userId;
    private String password;
    private Set<String> roles;
}


// File: member/member-biz/src/main/java/com/subride/member/biz/domain/Member.java
package com.subride.member.biz.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
    private String userId;
    private String userName;
    private String bankName;
    private String bankAccount;
    private int characterId;

    public boolean canbeAccessed() {
        return !userId.equalsIgnoreCase("user99");
    }
}


// File: member/member-biz/src/main/java/com/subride/member/biz/exception/BizException.java
package com.subride.member.biz.exception;

public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}


// File: subrecommend/subrecommend-infra/build.gradle
dependencies {
    implementation project(':common')
    implementation project(':subrecommend:subrecommend-biz')
}

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
    private String description;
    private Long fee;
    private int maxShareNum;
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
    private String name;
    private String spendingCategory;

    public Category toDomain() {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
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
    public Map<String, Long> getSpendingByCategory(String userId) {
        return spendingRepository.getSpendingByCategory(userId);
    }

    @Override
    public Category getCategoryBySpendingCategory(String spendingCategory) {
        CategoryEntity categoryEntity = categoryRepository.findBySpendingCategory(spendingCategory)
                .orElseThrow(() -> new InfraException("Category not found"));
        return categoryEntity.toDomain();
    }

    @Override
    public List<Sub> getSubListByCategoryId(Long categoryId) {
        List<SubEntity> subEntities = subRepository.findByCategoryIdOrderByName(categoryId);
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
    Optional<CategoryEntity> findBySpendingCategory(String spendingCategory);
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/repo/ISpendingRepository.java
package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.out.entity.SpendingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

public interface ISpendingRepository extends JpaRepository<SpendingEntity, Long> {
    @Query("SELECT s.category, SUM(s.amount) FROM SpendingEntity s WHERE s.userId = :userId GROUP BY s.category")
    Map<String, Long> getSpendingByCategory(String userId);
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/out/repo/ISubRepository.java
package com.subride.subrecommend.infra.out.repo;

import com.subride.subrecommend.infra.out.entity.SubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISubRepository extends JpaRepository<SubEntity, Long> {
    List<SubEntity> findByCategoryIdOrderByName(Long categoryId);
}



// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/in/web/SubRecommendController.java
package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.biz.usecase.inport.ISubRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subrecommend")
@RequiredArgsConstructor
public class SubRecommendController {
    private final ISubRecommendService subRecommendService;
    private final SubRecommendControllerHelper subRecommendControllerHelper;

    @GetMapping("/category")
    public ResponseEntity<CategoryInfoDTO> getRecommendCategory(@RequestParam String userId) {
        CategoryInfoDTO categoryInfoDTO = subRecommendService.getRecommendCategoryBySpending(userId);
        return ResponseEntity.ok(categoryInfoDTO);
    }

    @GetMapping("/list")
    public ResponseEntity<List<SubInfoDTO>> getRecommendSubList(@RequestParam Long categoryId) {
        List<SubInfoDTO> subInfoDTOList = subRecommendService.getRecommendSubListByCategory(categoryId);
        return ResponseEntity.ok(subInfoDTOList);
    }

    @GetMapping("/detail/{subId}")
    public ResponseEntity<SubInfoDTO> getSubDetail(@PathVariable Long subId) {
        SubInfoDTO subInfoDTO = subRecommendControllerHelper.getSubDetail(subId);
        return ResponseEntity.ok(subInfoDTO);
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/in/web/SubRecommendControllerHelper.java
package com.subride.subrecommend.infra.in.web;

import com.subride.subrecommend.biz.domain.Sub;
import com.subride.subrecommend.biz.dto.SubInfoDTO;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SubRecommendControllerHelper {
    private final ISubRepository subRepository;

    public SubInfoDTO getSubDetail(Long subId) {
        SubEntity sub = subRepository.findById(subId)
                .orElseThrow(() -> new IllegalArgumentException("Sub not found"));
        return toSubInfoDTO(sub);
    }

    public SubInfoDTO toSubInfoDTO(SubEntity sub) {
        SubInfoDTO subInfoDTO = new SubInfoDTO();
        subInfoDTO.setId(sub.getId());
        subInfoDTO.setName(sub.getName());
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
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/jwt/JwtAuthenticationFilter.java
// CommonJwtAuthenticationFilter.java
package com.subride.subrecommend.infra.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
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
import jakarta.servlet.http.HttpServletRequest;
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
            DecodedJWT decodedJWT = verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}

// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/config/SecurityConfig.java
package com.subride.subrecommend.infra.common.config;

import com.subride.subrecommend.infra.common.jwt.JwtAuthenticationFilter;
import com.subride.subrecommend.infra.common.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers(HttpMethod.POST, "/api/subrecommend/**").permitAll()
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


// File: subrecommend/subrecommend-infra/src/main/java/com/subride/subrecommend/infra/common/config/DataInitializer.java
package com.subride.subrecommend.infra.common.config;

import com.subride.subrecommend.infra.out.entity.CategoryEntity;
import com.subride.subrecommend.infra.out.entity.SubEntity;
import com.subride.subrecommend.infra.out.repo.ICategoryRepository;
import com.subride.subrecommend.infra.out.repo.ISubRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final ICategoryRepository categoryRepository;
    private final ISubRepository subRepository;

    public DataInitializer(ICategoryRepository categoryRepository, ISubRepository subRepository) {
        this.categoryRepository = categoryRepository;
        this.subRepository = subRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 카테고리 데이터 입력
        CategoryEntity category1 = new CategoryEntity(1L, "생필품", "Life");
        CategoryEntity category2 = new CategoryEntity(2L, "반려동물", "Pet");
        CategoryEntity category3 = new CategoryEntity(3L, "OTT", "OTT");
        CategoryEntity category4 = new CategoryEntity(4L, "FOOD", "Food");
        CategoryEntity category5 = new CategoryEntity(5L, "건강", "Health");
        CategoryEntity category6 = new CategoryEntity(6L, "문화", "Culture");

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);
        categoryRepository.save(category4);
        categoryRepository.save(category5);
        categoryRepository.save(category6);

        // 구독 서비스 데이터 입력
        SubEntity sub1 = new SubEntity(1L, "런드리고", "빨래구독 서비스", category1, 35000L, 2, "laundrygo.png");
        SubEntity sub2 = new SubEntity(2L, "술담화", "인생 전통술 구독 서비스", category4, 39000L, 3, "suldamhwa.jpeg");
        SubEntity sub3 = new SubEntity(4L, "필리", "맞춤형 영양제 정기배송", category5, 15000L, 3, "pilly.png");
        SubEntity sub4 = new SubEntity(5L, "넷플릭스", "넷플릭스", category3, 15000L, 5, "netflix.png");
        SubEntity sub5 = new SubEntity(6L, "티빙", "티빙", category3, 15000L, 5, "tving.png");
        SubEntity sub6 = new SubEntity(7L, "쿠팡플레이", "쿠팡플레이", category3, 15000L, 5, "coupang.png");
        SubEntity sub7 = new SubEntity(8L, "디즈니플러스", "디즈니플러스", category3, 15000L, 5, "disney.png");
        SubEntity sub8 = new SubEntity(9L, "해피문데이", "생리대 배송 서비스", category1, 6000L, 2, "happymoonday.jpeg");
        SubEntity sub9 = new SubEntity(10L, "하비인더박스", "취미용 소품 송 서비스", category1, 29000L, 3, "hobbyinthebox.jpeg");
        SubEntity sub10 = new SubEntity(11L, "월간가슴", "맞춤형 브라 배송", category1, 16000L, 2, "monthlychest.png");
        SubEntity sub11 = new SubEntity(12L, "위클리셔츠", "깔끔하고 다양한 셔츠 3~5장 매주 배송", category1, 40000L, 2, "weeklyshirts.jpeg");
        SubEntity sub12 = new SubEntity(13L, "월간과자", "매월 다른 구성의 과자상자 배송", category4, 9900L, 3, "monthlysnack.jpeg");
        SubEntity sub13 = new SubEntity(14L, "밀리의서재", "전자책 무제한 구독", category6, 9900L, 5, "milibook.jpeg");
        SubEntity sub14 = new SubEntity(15L, "더 반찬", "맛있고 다양한 집밥반찬 5세트", category4, 70000L, 3, "sidedishes.jpeg");
        SubEntity sub15 = new SubEntity(16L, "와이즐리", "면도날 구독 서비스", category1, 8900L, 4, "wisely.jpeg");
        SubEntity sub16 = new SubEntity(17L, "미하이 삭스", "매달 패션 양말 3종 배송", category1, 990L, 3, "mehi.jpeg");
        SubEntity sub17 = new SubEntity(18L, "핀즐", "자취방 꾸미고 싶은 사람들을 위한 그림 구독 서비스", category1, 26000L, 3, "pinzle.png");
        SubEntity sub18 = new SubEntity(19L, "꾸까", "2주마다 꽃 배달 서비스", category1, 30000L, 3, "kukka.png");
        SubEntity sub19 = new SubEntity(20L, "커피 리브레", "매주 다른 종류의 커피 배달", category4, 48000L, 5, "coffeelibre.jpeg");

        subRepository.save(sub1);
        subRepository.save(sub2);
        subRepository.save(sub3);
        subRepository.save(sub4);
        subRepository.save(sub5);
        subRepository.save(sub6);
        subRepository.save(sub7);
        subRepository.save(sub8);
        subRepository.save(sub9);
        subRepository.save(sub10);
        subRepository.save(sub11);
        subRepository.save(sub12);
        subRepository.save(sub13);
        subRepository.save(sub14);
        subRepository.save(sub15);
        subRepository.save(sub16);
        subRepository.save(sub17);
        subRepository.save(sub18);
        subRepository.save(sub19);
    }
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

    public int getCode() { return code; }
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
    private Long categoryId;
    private String categoryName;
    private Long totalSpending;
}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/usecase/inport/ISubRecommendService.java
package com.subride.subrecommend.biz.usecase.inport;
import com.subride.subrecommend.biz.dto.CategoryInfoDTO;
import com.subride.subrecommend.biz.dto.SubInfoDTO;

import java.util.List;

public interface ISubRecommendService {
    CategoryInfoDTO getRecommendCategoryBySpending(String userId);
    List<SubInfoDTO> getRecommendSubListByCategory(Long categoryId);
}

// File: subrecommend/subrecommend-biz/src/main/java/com/subride/subrecommend/biz/usecase/outport/ISubRecommendProvider.java
package com.subride.subrecommend.biz.usecase.outport;

import com.subride.subrecommend.biz.domain.Category;
import com.subride.subrecommend.biz.domain.Sub;

import java.util.List;
import java.util.Map;

public interface ISubRecommendProvider {
    Map<String, Long> getSpendingByCategory(String userId);
    Category getCategoryBySpendingCategory(String spendingCategory);
    List<Sub> getSubListByCategoryId(Long categoryId);
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
    public CategoryInfoDTO getRecommendCategoryBySpending(String userId) {
        Map<String, Long> spendingByCategory = subRecommendProvider.getSpendingByCategory(userId);
        String maxSpendingCategory = spendingByCategory.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        Category category = subRecommendProvider.getCategoryBySpendingCategory(maxSpendingCategory);

        CategoryInfoDTO categoryInfoDTO = new CategoryInfoDTO();
        categoryInfoDTO.setCategoryId(category.getId());
        categoryInfoDTO.setCategoryName(category.getName());
        categoryInfoDTO.setTotalSpending(spendingByCategory.get(maxSpendingCategory));

        return categoryInfoDTO;
    }

    @Override
    public List<SubInfoDTO> getRecommendSubListByCategory(Long categoryId) {
        List<Sub> subList = subRecommendProvider.getSubListByCategoryId(categoryId);

        return subList.stream()
                .map(this::toSubInfoDTO)
                .collect(Collectors.toList());
    }

    private SubInfoDTO toSubInfoDTO(Sub sub) {
        SubInfoDTO subInfoDTO = new SubInfoDTO();
        subInfoDTO.setId(sub.getId());
        subInfoDTO.setName(sub.getName());
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
    private Long id;
    private String name;
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
    private String description;
    private Category category;
    private Long fee;
    private int maxShareNum;
    private String logo;
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


// File: /Users/ondal/workspace/subride/build.gradle
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.1.11'
	id 'io.spring.dependency-management' version '1.1.4'
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
		implementation 'commons-beanutils:commons-beanutils:1.9.4'
		implementation 'org.springframework.boot:spring-boot-starter-aop'
		implementation 'com.google.code.gson:gson:2.11.0'
		compileOnly 'org.projectlombok:lombok:1.18.12'
		annotationProcessor 'org.projectlombok:lombok:1.18.24'

		//==============  For TEST
		testImplementation 'org.springframework.boot:spring-boot-starter-test'

		//--JUnit, Mokito Test
		testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.0'
		testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.0'
		testImplementation 'org.mockito:mockito-core:3.12.4'
		testImplementation 'org.mockito:mockito-junit-jupiter:3.12.4'

		//-- spring security test
		testImplementation 'org.springframework.security:spring-security-test'

		//-- For mysql System test
		testImplementation 'org.testcontainers:mysql:1.17.3'

		//-- For WebMvc System test
		implementation 'org.springframework.boot:spring-boot-starter-webflux'

		//-- lombok
		// -- @SpringBootTest를 사용하여 전체 애플리케이션 컨텍스트를 로딩되는 테스트 코드에만 사용
		// -- 그 외 단위나 컴포넌트 테스트에 사용하면 제대로 동작안함
		testCompileOnly 'org.projectlombok:lombok:1.18.12'
		testAnnotationProcessor 'org.projectlombok:lombok:1.18.24'
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
		include '**/*Test.class'
	}
	//==========================
}

subprojects {
	apply plugin: 'org.springframework.boot'
}

project(':common') {
	bootJar.enabled = false
	jar.enabled = true

	dependencies {
		implementation 'org.springframework.boot:spring-boot-starter-web'
		implementation 'org.springframework.boot:spring-boot-starter-security'
		implementation 'com.auth0:java-jwt:4.4.0'
	}
}

configure(subprojects.findAll { it.name.endsWith('-infra') }) {
	dependencies {
		implementation 'org.springframework.boot:spring-boot-starter-web'
		implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
		implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'
		runtimeOnly 'com.mysql:mysql-connector-j'
		implementation 'org.springframework.boot:spring-boot-starter-security'
		implementation 'com.auth0:java-jwt:4.4.0'
	}
}

configure(subprojects.findAll { it.name.endsWith('-biz') }) {
	bootJar.enabled = false
	jar.enabled = true
}

