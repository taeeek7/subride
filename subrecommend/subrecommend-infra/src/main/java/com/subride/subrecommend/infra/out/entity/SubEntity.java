package com.subride.subrecommend.infra.out.entity;

import com.subride.subrecommend.biz.domain.Sub;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    private Long fee;
    private int maxShareNum;
    private String logo;

    // toDomain 메서드 수정
    public Sub toDomain() {
        Sub sub = new Sub();
        sub.setId(id);
        sub.setName(name);
        sub.setDescription(description);
        sub.setCategory(category.toDomain());
        sub.setFee(fee);
        sub.setMaxShareNum(maxShareNum);
        sub.setLogo(logo);
        return sub;
    }
}