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
