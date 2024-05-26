package com.subride.member.infra.in.web;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.common.dto.JwtTokenDTO;
import com.subride.member.infra.common.dto.LoginRequestDTO;
import com.subride.member.infra.common.dto.SignupRequestDTO;
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
