package com.subride.mysub.infra.out.entity;

import com.subride.mysub.biz.domain.MySub;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "my_sub")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MySubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "sub_id")
    private Long subId;

    public MySub toDomain() {
        MySub mySub = new MySub();
        mySub.setUserId(userId);
        mySub.setSubId(subId);
        return mySub;
    }
}