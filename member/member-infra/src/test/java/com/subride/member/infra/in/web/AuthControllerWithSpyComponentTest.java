// File: member/member-infra/src/test/java/com/subride/member/infra/in/web/AuthControllerWithSpyComponentTest.java
package com.subride.member.infra.in.web;

import com.google.gson.Gson;
import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.biz.usecase.service.AuthServiceImpl;
import com.subride.member.infra.common.config.SecurityConfig;
import com.subride.member.infra.common.dto.LoginRequestDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
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