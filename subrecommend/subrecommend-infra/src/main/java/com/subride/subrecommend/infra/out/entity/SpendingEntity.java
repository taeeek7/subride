package com.subride.subrecommend.infra.out.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spending")
@Getter
@Setter
public class SpendingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "category")
    private String category;

    @Column(name = "amount")
    private Long amount;

    // 기타 필요한 필드 및 메서드 추가
}