package com.subride.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/*
구독서비스 정보를 담을 객체
구독추천 서비스에 요청하여 정보를 채움
 */
@Getter
@Setter
public class SubInfoDTO {
    /*
    구독추천서비스의 SubInfoDTO와 필드명이 달라 @JsonProperty로 필드 매핑을 함
    */
    @JsonProperty("id")
    private Long subId;
    @JsonProperty("name")
    private String subName;

    //-- 필드명이 동일하면 같은 이름으로 자동 매핑되므로 매핑 불필요
    private String categoryName;
    private String logo;
    private String description;
    private Long fee;
    private int maxShareNum;
}
