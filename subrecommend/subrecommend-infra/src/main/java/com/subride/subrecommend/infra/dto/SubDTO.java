package com.subride.subrecommend.infra.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubDTO {
    private Long id;
    private String name;
    private String description;
    private Long fee;
    private int maxShareNum;
}