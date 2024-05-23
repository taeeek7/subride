package com.subride.member.infra.out.entity;

import com.subride.member.biz.domain.Account;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/*
@NoArgsConstructor(access = AccessLevel.PROTECTED)
- JPA는 인자없는 기본 생성자를 대부분 요구하기 때문에 필요
- Access Level을 PROTECTED로 하는 이유는 외부에서 new 키워드로 인스턴스 생성을 못하게 하기 위함임
@NoArgsConstructor(access = AccessLevel.PROTECTED)
- 모든 인자를 갖는 생성자를 생성
- Access level을 PRIVATE으로 하여 해당 클래스에서만 사용할 수 있도록 제한함
 */
public class AccountEntity {
    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    /*
    왜 static으로 메소드를 만드는가?
    - Access level에 PROTECTED라 외부에서 new 키워드로 인스턴스 생성을 못하므로 static 메소드로 만듬
    - 외부에서는 AccountEntity persistentAccount = AccountEntity.fromDomain(account)와 같이 사용
     */
    public static AccountEntity fromDomain(Account account) {
        return new AccountEntity(
                account.getUserId(),
                account.getPassword(),
                account.getRoles()
        );
    }

    public Account toDomain() {
        Account account = new Account();
        account.setUserId(this.userId);
        account.setPassword(this.password);
        account.setRoles(this.roles);
        return account;
    }
}
