package com.subride.member.biz.usecase.outport;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;

public interface IAuthProvider {
    Member validateAuth(String userId, String password);

    void signup(Member member, Account account);

}
