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
        return !member.getUserId().equalsIgnoreCase("user99");
    }
}
