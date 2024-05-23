package com.subride.member.infra.out.entity;

import com.subride.member.biz.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter @Setter
@Builder
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
public class MemberEntity {
    @Id
    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankAccount;

    private int characterId;

    /*
    왜 static으로 메소드를 만드는가?
    - Access level에 PROTECTED라 외부에서 new 키워드로 인스턴스 생성을 못하므로 static 메소드로 만듬
    - 외부에서는 MemberEntity persistentMember = MemberEntity.fromDomain(member)와 같이 사용
    */
    public static MemberEntity fromDomain(Member member) {
        return MemberEntity.builder()
                .userId(member.getUserId())
                .userName(member.getUserName())
                .bankName(member.getBankName())
                .bankAccount(member.getBankAccount())
                .characterId(member.getCharacterId())
                .build();
    }

    public Member toDomain() {
        Member member = new Member();
        member.setUserId(this.userId);
        member.setUserName(this.userName);
        member.setBankName(this.bankName);
        member.setBankAccount(this.bankAccount);
        member.setCharacterId(this.characterId);
        return member;
    }
}