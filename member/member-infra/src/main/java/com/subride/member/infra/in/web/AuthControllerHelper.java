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
