package com.subride.member.infra.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.subride.member.infra.dto.JwtTokenDTO;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.MemberEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Algorithm algorithm;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-time}") long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-expiration-time}") long refreshTokenValidityInMilliseconds) {
        this.algorithm = Algorithm.HMAC512(secretKey);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * 1000;
    }

    public JwtTokenDTO createToken(MemberEntity memberEntity, Collection<? extends GrantedAuthority> authorities) {
        try {
            Date now = new Date();
            Date accessTokenValidity = new Date(now.getTime() + accessTokenValidityInMilliseconds);
            Date refreshTokenValidity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

            String accessToken = JWT.create()
                    .withSubject(memberEntity.getUserId())
                    .withClaim("userId", memberEntity.getUserId())
                    .withClaim("userName", memberEntity.getUserName())
                    .withClaim("bankName", memberEntity.getBankName())
                    .withClaim("bankAccount", memberEntity.getBankAccount())
                    .withClaim("characterId", memberEntity.getCharacterId())
                    .withClaim("auth", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .withIssuedAt(now)
                    .withExpiresAt(accessTokenValidity)
                    .sign(algorithm);

            String refreshToken = JWT.create()
                    .withSubject(memberEntity.getUserId())
                    .withIssuedAt(now)
                    .withExpiresAt(refreshTokenValidity)
                    .sign(algorithm);

            return JwtTokenDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch(Exception e) {
            throw new InfraException(0, e.getMessage(), e);
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(refreshToken);
            return true;
        } catch (Exception e) {
            throw new InfraException(0, e.getMessage(), e);
        }
    }

    public String getUserIdFromToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(refreshToken);
            return decodedJWT.getSubject();
        } catch (Exception e) {
            throw new InfraException(0, "Invalid refresh token", e);
        }
    }

    public String resolveToken(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        } catch (Exception e) {
            throw new InfraException(0, "Invalid refresh token", e);
        }
    }

    public int validateToken(String token) {
        log.info("******** validateToken: {}", token);
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return 1; // 검사 성공 시 1 반환
        } catch (TokenExpiredException e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            throw new InfraException(10, "토큰이 만료되었습니다.");
        } catch (SignatureVerificationException e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            throw new InfraException(20, "서명 검증에 실패했습니다.");
        } catch (AlgorithmMismatchException e) {
            log.error("AlgorithmMismatchException: {}", e.getMessage(), e);
            throw new InfraException(30, "알고리즘이 일치하지 않습니다.");
        } catch (InvalidClaimException e) {
            log.error("InvalidClaimException: {}", e.getMessage(), e);
            throw new InfraException(40, "유효하지 않은 클레임입니다.");
        } catch (Exception e) {
            log.error("Undefined Error: {}", e.getMessage(), e);
            throw new InfraException(50, "토큰 검증 중 예외가 발생했습니다.");
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String username = decodedJWT.getSubject();
            String[] authStrings = decodedJWT.getClaim("auth").asArray(String.class);
            Collection<? extends GrantedAuthority> authorities = Arrays.stream(authStrings)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UserDetails userDetails = new User(username, "", authorities);

            return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
        } catch (Exception e) {
            throw new InfraException(0, "Invalid refresh token", e);
        }
    }
}
