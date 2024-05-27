package com.subride.member.infra.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenRefreshDTO {
	private String refreshToken;
}
