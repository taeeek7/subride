package com.subride.member.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtTokenDTO {
    private String accessToken;
    private String refreshToken;
}