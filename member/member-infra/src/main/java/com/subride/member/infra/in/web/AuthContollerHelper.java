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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
class AuthContollerHelper {
    private final JwtTokenProvider jwtTokenProvider;
    private final IMemberRepository memberRepository;
    private final IAccountRepository accountRepository;

    JwtTokenDTO createToken(Member member) {
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

    int checkAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    boolean isValidRefreshToken(String token) {
        return jwtTokenProvider.validateRefreshToken(token);
    }

    Member getMemberFromRequest(SignupRequestDTO signupRequestDTO) {
        Member member = new Member();
        member.setUserId(signupRequestDTO.getUserId());
        member.setUserName(signupRequestDTO.getUserName());
        member.setBankName(signupRequestDTO.getBankName());
        member.setBankAccount(signupRequestDTO.getBankAccount());
        return member;
    }

    Account getAccountFromRequest(SignupRequestDTO signupRequestDTO) {
        Account account = new Account();
        account.setUserId(signupRequestDTO.getUserId());
        account.setPassword(signupRequestDTO.getPassword());
        account.setRoles(signupRequestDTO.getRoles());
        return account;
    }

    Member getMemberFromToken(String token) {
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new InfraException("User not found"));
        return member.toDomain();
    }
}
