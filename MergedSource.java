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


// File: member/member-infra/src/test/java/com/subride/member/infra/out/adapter/AuthProviderImplComponentTest.java
package com.subride.member.infra.out.adapter;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.common.config.SecurityConfig;
import com.subride.member.infra.common.jwt.CustomUserDetailsService;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.exception.InfraException;
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
        Member member = new Member();
        member.setUserId("testuser");
        member.setUserName("John Doe");
        member.setBankName("Test Bank");
        member.setBankAccount("1234567890");

        Account account = new Account();
        account.setUserId("testuser");
        account.setPassword("password");

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
import com.subride.member.infra.common.dto.MemberInfoDTO;
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

// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerSystemTest.java
package com.subride.member.infra.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.member.infra.common.dto.JwtTokenDTO;
import com.subride.member.infra.common.dto.LoginRequestDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    // 테스트 데이터 생성 메서드
    private SignupRequestDTO createSignupRequest() {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setUserId("testuser");
        signupRequestDTO.setPassword("password");
        signupRequestDTO.setUserName("홍길동");
        signupRequestDTO.setBankName("KB");
        signupRequestDTO.setBankAccount("123-12222");
        return signupRequestDTO;
    }

    @Test
    void signup_success() {
        // Given
        SignupRequestDTO signupRequestDTO = createSignupRequest();

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
    void signup_invalidRequest_badRequest() {
        // Given
        SignupRequestDTO signupRequestDTO = createSignupRequest();
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
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUserId("testuser");
        loginRequestDTO.setPassword("password");

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

// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerComponentTest.java
// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerComponentTest.java
package com.subride.member.infra.in.web;

import com.google.gson.Gson;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.inport.IAuthService;
import com.subride.member.infra.common.dto.JwtTokenDTO;
import com.subride.member.infra.common.dto.LoginRequestDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
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
    //-- 모의 Http 객체이며 Bocking방식을 지원함
    @Autowired
    private MockMvc mockMvc;

    //-- API실행 시 필요한 Bean객체에 대한 모의 객체 생성
    @MockBean
    private IAuthService authService;
    @MockBean
    private AuthControllerHelper authControllerHelper;

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
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUserId("testuser");
        loginRequestDTO.setPassword("password");

        Member member = new Member();
        member.setUserId("testuser");

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
import com.subride.member.infra.common.dto.JwtTokenDTO;
import com.subride.member.infra.common.dto.JwtTokenRefreshDTO;
import com.subride.member.infra.common.dto.JwtTokenVarifyDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.out.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

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
public class AuthControllerUnitTest {
    //-- 필요한 모의 객체를 생성
    @Mock
    private IAuthService authService;

    //-------------------------------------

    //--- AuthControllerHelper에서 사용할 JwtTokenProvider 객체 생성
    private String jwtSecret = getTestJwtSecret();
    private long jwtExpirationTime = 3600;
    private long jwtRefreshTokenExpirationTime = 36000;

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
        jwtTokenDTO = createTestToken();
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

    //=============== Private functions ====================
    private String getTestJwtSecret() {
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[64];
        random.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }

    private JwtTokenDTO createTestToken() {
        // 테스트 객체 생성
        Member member = new Member();
        member.setUserId("testuser");
        member.setUserName("홍길동");
        member.setBankName("KB");
        member.setBankAccount("123-12222");
        member.setCharacterId(1);

        Account account = new Account();
        account.setUserId(member.getUserId());
        account.setPassword("password");
        account.setRoles(new HashSet<>(Arrays.asList("USER", "ADMIN")));

        // 사용자의 권한 정보 생성
        Collection<? extends GrantedAuthority> authorities = account.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 새로운 액세스 토큰 생성
        MemberEntity memberEntity = MemberEntity.fromDomain(member);
        return jwtTokenProvider.createToken(memberEntity, authorities);

    }
}

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
        MemberEntity memberEntity = MemberEntity.fromDomain(member);
        memberRepository.save(memberEntity);

        AccountEntity accountEntity = AccountEntity.fromDomain(account);
        //-- 암호를 단방향 암호화함
        accountEntity.setPassword(passwordEncoder.encode(account.getPassword()));
        accountRepository.save(accountEntity);
    }
}


// File: member/member-infra/src/main/java/com/subride/member/infra/out/repo/IMemberRepository.java
package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IMemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUserId(String userId);
    List<MemberEntity> findByUserIdIn(List<String> userIdList);
}

// File: member/member-infra/src/main/java/com/subride/member/infra/out/repo/IAccountRepository.java
package com.subride.member.infra.out.repo;

import com.subride.member.infra.out.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUserId(String userId);
    Boolean existsByUserId(String userId);
}

// File: member/member-infra/src/main/java/com/subride/member/infra/in/web/AuthControllerHelper.java
package com.subride.member.infra.in.web;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.common.dto.JwtTokenDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
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
import com.subride.member.infra.common.dto.*;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonUtils.createFailureResponse(e.getCode(), e.getMessage()));
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
import com.subride.member.infra.common.dto.MemberInfoDTO;
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

import com.subride.member.infra.common.dto.MemberInfoDTO;
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


// File: member/member-infra/src/main/java/com/subride/member/infra/common/dto/MemberInfoDTO.java
package com.subride.member.infra.common.dto;

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

// File: member/member-infra/src/main/java/com/subride/member/infra/common/dto/JwtTokenVarifyDTO.java
package com.subride.member.infra.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenVarifyDTO {
	private String token;
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/dto/JwtTokenDTO.java
package com.subride.member.infra.common.dto;

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

// File: member/member-infra/src/main/java/com/subride/member/infra/common/dto/JwtTokenRefreshDTO.java
package com.subride.member.infra.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenRefreshDTO {
	private String refreshToken;
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/dto/LoginRequestDTO.java
package com.subride.member.infra.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
	private String userId;
	private String password;
}


// File: member/member-infra/src/main/java/com/subride/member/infra/common/dto/SignupRequestDTO.java
package com.subride.member.infra.common.dto;

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
import com.subride.member.infra.common.dto.JwtTokenDTO;
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
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
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

// File: member/member-infra/src/main/java/com/subride/member/infra/common/config/WebConfig.java
package com.subride.member.infra.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@SuppressWarnings("unused")
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
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
                        .title("Subride MemberEntity API")
                        .version("v1.0.0")
                        .description("Subride 회원 서비스 API 명세서입니다."));
    }
}

// File: member/member-infra/src/main/java/com/subride/member/infra/common/config/JwtAuthenticationFilter.java
package com.subride.member.infra.common.config;

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


