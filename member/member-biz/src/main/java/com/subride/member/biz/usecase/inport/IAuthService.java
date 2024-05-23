package com.subride.member.biz.usecase.inport;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;

public interface IAuthService {
    void signup(Member member, Account account);
    Member login(String userId, String password);

    boolean validateMemberAccess(Member member);     //-- 접근 허용 정책 결정
}
