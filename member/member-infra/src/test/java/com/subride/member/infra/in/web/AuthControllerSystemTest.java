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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import static com.subride.member.infra.in.web.CommonTestUtils.createSignupRequest;

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