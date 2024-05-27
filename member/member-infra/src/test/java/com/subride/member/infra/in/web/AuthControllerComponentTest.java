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