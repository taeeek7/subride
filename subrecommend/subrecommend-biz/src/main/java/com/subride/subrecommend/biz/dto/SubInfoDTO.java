package com.subride.subrecommend.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubInfoDTO {
    private Long id;
    private String name;
    private String categoryName;
    private String logo;
    private String description;
    private Long fee;
    private int maxShareNum;
}