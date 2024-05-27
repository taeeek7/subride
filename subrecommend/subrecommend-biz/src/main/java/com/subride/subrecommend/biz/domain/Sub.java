package com.subride.subrecommend.biz.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sub {
    private Long id;
    private String name;
    private String description;
    private Category category;
    private Long fee;
    private int maxShareNum;
    private String logo;
}