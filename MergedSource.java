// File: transfer\build.gradle
dependencies {
    implementation project(':common')

    //-- OpenFeign Client: Blocking방식의 Http Client
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'

    //--mybatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3'
}

/*
OpenFeign Client는 SpringCloud의 컴포넌트이기 때문에 Spring Cloud 종속성 관리 지정 필요
Spring Boot 버전에 맞는 Spring Cloud 버전을 지정해야 함
https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions#supported-releases
*/
dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2023.0.1"
    }
}

// File: transfer\build\resources\main\application.yml
server:
  port: ${SERVER_PORT:18084}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:transfer-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/transfer?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
    schema: classpath:schema.sql  #Table 생성 SLQ
    initialization-mode: always        #시작시 테이블 존재 체크하고 없으면 생성

mybatis:
  type-aliases-package: com.subride.transfer.persistent.entity
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-handlers-package: com.subride.transfer.persistent.typehandler

springdoc:
  swagger-ui:
    path: /swagger-ui.html
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    com.subride.transfer: DEBUG
feign:
  mygroup:
    url: ${MYGRP_URI:http://localhost:18083}


// File: transfer\build\resources\test\application-test.yml
server:
  port: ${SERVER_PORT:18084}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:transfer-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///transfer}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
    schema: classpath:schema.sql  #Table 생성 SLQ
    initialization-mode: always        #시작시 테이블 존재 체크하고 없으면 생성

mybatis:
  type-aliases-package: com.subride.transfer.persistent.entity
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-handlers-package: com.subride.transfer.persistent.typehandler

springdoc:
  swagger-ui:
    path: /swagger-ui.html
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO


// File: transfer\src\main\java\com\subride\transfer\TransferApplication.java
package com.subride.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TransferApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
    }
}


// File: transfer\src\main\java\com\subride\transfer\common\config\LoggingAspect.java
package com.subride.transfer.common.config;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Aspect       //Disable하려면 리마크 함
@Component
@Slf4j
@SuppressWarnings("unused")
public class LoggingAspect {
    private final Gson gson = new Gson();

    @Pointcut("execution(* com.subride..*.*(..))")
    private void loggingPointcut() {}

    @Before("loggingPointcut()")
    public void logMethodStart(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String argString = getArgumentString(args);

        log.info("[START] {}.{} - Args: [{}]", className, methodName, argString);
    }

    @AfterReturning(pointcut = "loggingPointcut()", returning = "result")
    public void logMethodEnd(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String resultString = getResultString(result);

        log.info("[END] {}.{} - Result: {}", className, methodName, resultString);
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.error("[EXCEPTION] {}.{} - Exception: {}", className, methodName, exception.getMessage());
    }

    private String getArgumentString(Object[] args) {
        StringBuilder argString = new StringBuilder();

        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    argString.append(arg).append(", ");
                } else if (arg instanceof Collection) {
                    argString.append(((Collection<?>) arg).size()).append(" elements, ");
                } else if (arg instanceof Map) {
                    argString.append(((Map<?, ?>) arg).size()).append(" entries, ");
                } else {
                    argString.append(arg);
                    /*
                    try {
                        String jsonString = gson.toJson(arg);
                        argString.append(jsonString).append(", ");
                    } catch (Exception e) {
                        log.warn("JSON serialization failed for argument: {}", arg);
                        argString.append("JSON serialization failed, ");
                    }
                    */

                }
            } else {
                argString.append("null, ");
            }
        }

        if (!argString.isEmpty()) {
            argString.setLength(argString.length() - 2);
        }

        return argString.toString();
    }

    private String getResultString(Object result) {
        if (result != null) {
            if (result instanceof String || result instanceof Number || result instanceof Boolean) {
                return result.toString();
            } else if (result instanceof Collection) {
                return ((Collection<?>) result).size() + " elements";
            } else if (result instanceof Map) {
                return ((Map<?, ?>) result).size() + " entries";
            } else {
                return result.toString();
                /*
                try {
                    return gson.toJson(result);
                } catch (Exception e) {
                    log.warn("JSON serialization failed for result: {}", result);
                    return "JSON serialization failed";
                }

                 */
            }
        } else {
            return "null";
        }
    }
}

// File: transfer\src\main\java\com\subride\transfer\common\config\SecurityConfig.java
package com.subride.transfer.common.config;

import com.subride.transfer.common.jwt.JwtAuthenticationFilter;
import com.subride.transfer.common.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@SuppressWarnings("unused")
public class SecurityConfig {
    protected final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs.yaml", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


// File: transfer\src\main\java\com\subride\transfer\common\config\SpringDocConfig.java
package com.subride.transfer.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SuppressWarnings("unused")
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("구독추천 서비스 API")
                        .version("v1.0.0")
                        .description("구독추천 서비스 API 명세서입니다. "));
    }
}


// File: transfer\src\main\java\com\subride\transfer\common\dto\TransferRequest.java
package com.subride.transfer.common.dto;


import com.subride.transfer.common.enums.Period;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {
    private Long groupId;
    private Period period;
}

// File: transfer\src\main\java\com\subride\transfer\common\dto\TransferResponse.java
package com.subride.transfer.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TransferResponse {
    private Long id;
    private String memberId;
    private BigDecimal amount;
    private LocalDate transferDate;
}

// File: transfer\src\main\java\com\subride\transfer\common\enums\Period.java
package com.subride.transfer.common.enums;

public enum Period {
    THREE_MONTHS,
    ONE_YEAR
}

// File: transfer\src\main\java\com\subride\transfer\common\exception\TransferException.java
package com.subride.transfer.common.exception;

public class TransferException extends RuntimeException {
    private int code;

    public TransferException(String message) {
        super(message);
    }

    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferException(int code, String message) {
        super(message);
        this.code = code;
    }

    public TransferException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

// File: transfer\src\main\java\com\subride\transfer\common\feign\MyGroupFeignClient.java
package com.subride.transfer.common.feign;

import com.subride.common.dto.GroupMemberDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "myGroupFeignClient", url = "${feign.mygroup.url}")
public interface MyGroupFeignClient {
    @GetMapping("/api/my-groups/all-members")
    ResponseDTO<List<GroupMemberDTO>> getAllGroupMembers();
}


// File: transfer\src\main\java\com\subride\transfer\common\jwt\JwtAuthenticationFilter.java
// CommonJwtAuthenticationFilter.java
package com.subride.transfer.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

// File: transfer\src\main\java\com\subride\transfer\common\jwt\JwtAuthenticationInterceptor.java
package com.subride.transfer.common.jwt;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
Feign 클라이언트에서 요청 시 Authorization 헤더에 인증토큰 추가하기
 */
@Component
public class JwtAuthenticationInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String token = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
            if (token != null) {
                template.header(AUTHORIZATION_HEADER, token);
            }
        }
    }
}

// File: transfer\src\main\java\com\subride\transfer\common\jwt\JwtTokenProvider.java
// CommonJwtTokenProvider.java
package com.subride.transfer.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.subride.transfer.common.exception.TransferException;
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
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private final Algorithm algorithm;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.algorithm = Algorithm.HMAC512(secretKey);
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
            throw new TransferException(0, "Invalid refresh token");
        }
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}

// File: transfer\src\main\java\com\subride\transfer\controller\TransferController.java
package com.subride.transfer.controller;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.util.CommonUtils;
import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "이체조회 서비스 API")
@RestController
@RequestMapping("/api/transfer")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @Operation(summary = "이체내역 조회", description = "특정 그룹의 이체내역을 조회합니다.")
    @Parameters({
            @Parameter(name = "groupId", in = ParameterIn.QUERY, description = "그룹 ID", required = true),
            @Parameter(name = "period", in = ParameterIn.QUERY, description = "조회 기간 (THREE_MONTHS, ONE_YEAR)", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<TransferResponse>>> getTransferHistory(
            @RequestParam Long groupId,
            @RequestParam Period period) {
        List<TransferResponse> transferHistory = transferService.getTransferHistory(groupId, period);
        return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "이체내역 조회 성공", transferHistory));

    }

    @Operation(summary = "테스트 데이터 생성", description = "테스트를 위한 데이터를 생성합니다.")
    @PostMapping("/test-data")
    public ResponseEntity<ResponseDTO<Void>> createTestData() {
        transferService.createTestData();
        return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "테스트 데이터 생성 성공", null));

    }

    @Operation(summary = "전체 데이터 삭제", description = "모든 이체 데이터를 삭제합니다.")
    @DeleteMapping("/all")
    public ResponseEntity<ResponseDTO<Void>> deleteAllData() {
        transferService.deleteAllData();
        return ResponseEntity.ok(CommonUtils.createSuccessResponse(200, "전체 데이터 삭제 성공", null));

    }
}

// File: transfer\src\main\java\com\subride\transfer\persistent\dao\TransferProvider.java
package com.subride.transfer.persistent.dao;

import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.common.exception.TransferException;
import com.subride.transfer.persistent.dao.ITransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransferProvider {
    private final ITransferMapper transferMapper;

    public List<TransferResponse> getTransferHistory(Long groupId, Period period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        if (period == Period.THREE_MONTHS) {
            startDate = endDate.minusMonths(3);
        } else if (period == Period.ONE_YEAR) {
            startDate = endDate.minusYears(1);
        } else {
            throw new TransferException("잘못된 조회 기간입니다.");
        }

        List<Transfer> transferList = transferMapper.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

        return transferList.stream()
                .map(this::toTransferResponse)
                .collect(Collectors.toList());
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .memberId(transfer.getMemberId())
                .amount(transfer.getAmount())
                .transferDate(transfer.getTransferDate())
                .build();
    }
}

// File: transfer\src\main\java\com\subride\transfer\persistent\entity\Transfer.java
package com.subride.transfer.persistent.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    private Long id;
    private Long groupId;
    private String memberId;
    private BigDecimal amount;
    private LocalDate transferDate;
}


// File: transfer\src\main\java\com\subride\transfer\persistent\repository\ITransferMapper.java
package com.subride.transfer.persistent.repository;

import com.subride.transfer.persistent.entity.Transfer;
import org.springframework.data.repository.query.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Mapper
@Repository
@SuppressWarnings("unused")
public interface ITransferMapper {
    List<Transfer> findByGroupIdAndTransferDateBetween(@Param("groupId") Long groupId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    void save(Transfer transfer);
    void deleteAll();
    void insertList(List<Transfer> transfers);
}

// File: transfer\src\main\java\com\subride\transfer\persistent\typehandler\LocalDateTypeHandler.java
package com.subride.transfer.persistent.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;

@MappedTypes(LocalDate.class)
public class LocalDateTypeHandler extends BaseTypeHandler<LocalDate> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
        ps.setDate(i, java.sql.Date.valueOf(parameter));
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        java.sql.Date date = rs.getDate(columnName);
        return date != null ? date.toLocalDate() : null;
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        java.sql.Date date = rs.getDate(columnIndex);
        return date != null ? date.toLocalDate() : null;
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        java.sql.Date date = cs.getDate(columnIndex);
        return date != null ? date.toLocalDate() : null;
    }

    private static LocalDate getLocalDate(Date date) {
        if (date != null) {
            return date.toLocalDate();
        }
        return null;
    }
}

// File: transfer\src\main\java\com\subride\transfer\service\TransferService.java
package com.subride.transfer.service;

import com.google.gson.*;
import com.subride.common.dto.GroupMemberDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.common.feign.MyGroupFeignClient;
import com.subride.transfer.persistent.dao.TransferProvider;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.persistent.dao.ITransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferProvider transferProvider;
    private final ITransferMapper transferMapper;
    private final MyGroupFeignClient myGroupFeignClient;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public List<TransferResponse> getTransferHistory(Long groupId, Period period) {
        return transferProvider.getTransferHistory(groupId, period);
    }

    public void createTestData() {
        // 등록된 그룹의 참여자들 userId 가져오기
        ResponseDTO<List<GroupMemberDTO>> response = myGroupFeignClient.getAllGroupMembers();
        List<GroupMemberDTO> groupMembers = response.getResponse();
        log.info("Group members: {}", gson.toJson(groupMembers));

        List<Transfer> transfers = new ArrayList<>();
        Random random = new Random();

        for (GroupMemberDTO groupMember : groupMembers) {
            Long groupId = groupMember.getGroupId();
            Set<String> memberIds = groupMember.getMemberIds();
            int paymentDay = groupMember.getPaymentDay();

            for (String memberId : memberIds) {
                LocalDate transferDate;
                if (LocalDate.now().getDayOfMonth() >= paymentDay) {
                    transferDate = LocalDate.now().withDayOfMonth(paymentDay);
                } else {
                    transferDate = LocalDate.now().minusMonths(1).withDayOfMonth(paymentDay);
                }

                for (int i = 0; i < 12; i++) {
                    BigDecimal amount = BigDecimal.valueOf(random.nextInt(40001) + 10000);

                    Transfer transfer = new Transfer();
                    transfer.setGroupId(groupId);
                    transfer.setMemberId(memberId);
                    transfer.setAmount(amount);
                    transfer.setTransferDate(transferDate);

                    transfers.add(transfer);

                    transferDate = transferDate.minusMonths(1);
                }
            }
        }

        log.info("Generated transfer data: {}", gson.toJson(transfers));

        transferMapper.insertList(transfers);
    }

    public void deleteAllData() {
        transferMapper.deleteAll();
    }

    private static class LocalDateAdapter implements JsonSerializer<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public JsonElement serialize(LocalDate localDate, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(localDate));
        }
    }
}

// File: transfer\src\main\resources\application.yml
server:
  port: ${SERVER_PORT:18084}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:transfer-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/transfer?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
    schema: classpath:schema.sql  #Table 생성 SLQ
    initialization-mode: always        #시작시 테이블 존재 체크하고 없으면 생성

mybatis:
  type-aliases-package: com.subride.transfer.persistent.entity
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-handlers-package: com.subride.transfer.persistent.typehandler

springdoc:
  swagger-ui:
    path: /swagger-ui.html
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO
    org.springframework.security: DEBUG
    com.subride.transfer: DEBUG
feign:
  mygroup:
    url: ${MYGRP_URI:http://localhost:18083}


// File: transfer\src\test\java\com\subride\transfer\controller\TransferControllerComponentTest.java
// File: transfer/src/test/java/com/subride/transfer/controller/TransferControllerComponentTest.java
package com.subride.transfer.controller;

import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
public class TransferControllerComponentTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    @WithMockUser
    public void getTransferHistory_ValidInput_ReturnsTransferHistory() throws Exception {
        Long groupId = 1L;
        Period period = Period.THREE_MONTHS;
        List<TransferResponse> transferResponses = List.of(TransferResponse.builder().build());

        when(transferService.getTransferHistory(eq(groupId), eq(period))).thenReturn(transferResponses);

        mockMvc.perform(get("/api/transfer")
                        .param("groupId", String.valueOf(groupId))
                        .param("period", period.toString()))
                .andExpect(status().isOk());
    }

}

// File: transfer\src\test\java\com\subride\transfer\controller\TransferControllerSystemTest.java
package com.subride.transfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.persistent.repository.ITransferMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TransferControllerSystemTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITransferMapper transferMapper;

    @Autowired
    private DataSource dataSource;

    private WebTestClient webClient;

    @BeforeEach
    void setup() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String strSql = "CREATE TABLE IF NOT EXISTS transfer (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    group_id BIGINT NOT NULL,\n" +
                    "    member_id VARCHAR(255) NOT NULL,\n" +
                    "    amount DECIMAL(19, 2) NOT NULL,\n" +
                    "    transfer_date DATE NOT NULL\n" +
                    ");";

            statement.execute(strSql);
        }

        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();

        cleanup();  // 테스트 데이터 모두 지움

        Transfer transfer1 = new Transfer();
        transfer1.setGroupId(1L);
        transfer1.setMemberId("user01");
        transfer1.setAmount(BigDecimal.valueOf(10000));
        transfer1.setTransferDate(LocalDate.of(2024, 5, 5));

        Transfer transfer2 = new Transfer();
        transfer2.setGroupId(2L);
        transfer2.setMemberId("user02");
        transfer2.setAmount(BigDecimal.valueOf(20000));
        transfer2.setTransferDate(LocalDate.of(2024, 5, 5));

        transferMapper.insertList(List.of(transfer1, transfer2));
    }

    @AfterEach
    void cleanup() {
        transferMapper.deleteAll();
    }

    @Test
    @WithMockUser
    void getTransferHistory_success() {
        // Given
        Long groupId = 1L;
        Period period = Period.ONE_YEAR;

        // When & Then
        webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/transfer")
                        .queryParam("groupId", groupId)
                        .queryParam("period", period)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDTO.class)
                .value(response -> {
                    assert response.getCode() == 200;
                    assert response.getMessage().equals("이체내역 조회 성공");

                    List<Transfer> transferList = objectMapper.convertValue(response.getResponse(), List.class);
                    assert transferList.size() == 1;
                });
    }

}

// File: transfer\src\test\java\com\subride\transfer\persistent\dao\TransferRepositoryComponentTest.java
package com.subride.transfer.persistent.dao;

import com.subride.transfer.common.config.SecurityConfig;
import com.subride.transfer.common.jwt.JwtTokenProvider;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.persistent.repository.ITransferMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class TransferRepositoryComponentTest {
    @Autowired
    private ITransferMapper transferMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setup() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String strSql = "CREATE TABLE IF NOT EXISTS transfer (\n" +
                    "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    group_id BIGINT NOT NULL,\n" +
                    "    member_id VARCHAR(255) NOT NULL,\n" +
                    "    amount DECIMAL(19, 2) NOT NULL,\n" +
                    "    transfer_date DATE NOT NULL\n" +
                    ");";

            statement.execute(strSql);
        }

        cleanup();  // 테스트 데이터 모두 지움

        // 테스트 데이터 생성
        Transfer transfer1 = new Transfer();
        transfer1.setGroupId(1L);
        transfer1.setMemberId("user01");
        transfer1.setAmount(BigDecimal.valueOf(10000));
        transfer1.setTransferDate(LocalDate.of(2024, 6, 1));

        Transfer transfer2 = new Transfer();
        transfer2.setGroupId(1L);
        transfer2.setMemberId("user02");
        transfer2.setAmount(BigDecimal.valueOf(20000));
        transfer2.setTransferDate(LocalDate.of(2024, 7, 1));

        transferMapper.insertList(List.of(transfer1, transfer2));
    }

    @AfterEach
    void cleanup() {
        transferMapper.deleteAll();
    }

    @Test
    void findByGroupIdAndTransferDateBetween_ValidInput_ReturnsTransfers() {
        // Given
        Long groupId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        // When
        List<Transfer> transfers = transferMapper.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

        // Then
        assertThat(transfers).hasSize(2);
        assertThat(transfers.get(0).getGroupId()).isEqualTo(groupId);
        assertThat(transfers.get(0).getMemberId()).isEqualTo("user01");
        assertThat(transfers.get(0).getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(transfers.get(0).getTransferDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(transfers.get(1).getGroupId()).isEqualTo(groupId);
        assertThat(transfers.get(1).getMemberId()).isEqualTo("user02");
        assertThat(transfers.get(1).getAmount()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(transfers.get(1).getTransferDate()).isEqualTo(LocalDate.of(2023, 7, 1));
    }

    @Test
    void findByGroupIdAndTransferDateBetween_InvalidGroupId_ReturnsEmptyList() {
        // Given
        Long groupId = 999L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        // When
        List<Transfer> transfers = transferMapper.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

        // Then
        assertThat(transfers).isEmpty();
    }

    @Test
    void findByGroupIdAndTransferDateBetween_InvalidDateRange_ReturnsEmptyList() {
        // Given
        Long groupId = 1L;
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2022, 12, 31);

        // When
        List<Transfer> transfers = transferMapper.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

        // Then
        assertThat(transfers).isEmpty();
    }
}

// File: transfer\src\test\resources\application-test.yml
server:
  port: ${SERVER_PORT:18084}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:transfer-service}
  datasource:
    driver-class-name: ${DB_DRIVER:org.testcontainers.jdbc.ContainerDatabaseDriver}
    url: ${DB_URL:jdbc:tc:mysql:8.0.29:///transfer}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
    schema: classpath:schema.sql  #Table 생성 SLQ
    initialization-mode: always        #시작시 테이블 존재 체크하고 없으면 생성

mybatis:
  type-aliases-package: com.subride.transfer.persistent.entity
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-handlers-package: com.subride.transfer.persistent.typehandler

springdoc:
  swagger-ui:
    path: /swagger-ui.html
jwt:
  secret: ${JWT_SECRET:8O2HQ13etL2BWZvYOiWsJ5uWFoLi6NBUG8divYVoCgtHVvlk3dqRksMl16toztDUeBTSIuOOPvHIrYq11G2BwQ==}

# Logging
logging:
  level:
    root: INFO


// File: C:\home\clone\subride\settings.gradle
rootProject.name = 'subride'
include 'common'
include 'member:member-biz'
include 'member:member-infra'
include 'subrecommend:subrecommend-infra'
include 'subrecommend:subrecommend-biz'
include 'mysub:mysub-infra'
include 'mysub:mysub-biz'
include 'mygrp:mygrp-infra'
include 'mygrp:mygrp-biz'
include 'transfer'



// File: C:\home\clone\subride\build.gradle
/*
Spring Boot 버전에 따라 사용하는 라이브러리의 버전을 맞춰줘야 함
라이브러리 버전을 맞추지 않으면 실행 시 이상한 에러가 많이 발생함.
이를 편하게 하기 위해 Spring Boot는 기본적으로 지원하는 라이브러리 목록을 갖고 있음
이 목록에 있는 라이브러리는 버전을 명시하지 않으면 Spring Boot 버전에 맞는 버전을 로딩함

Spring Boot 지원 라이브러리 목록 확인
1) Spring Boot 공식 사이트 접속: spring.io
2) Projects > Spring Boot 선택 > LEARN 탭 클릭
3) 사용할 Spring Boot 버전의 'Reference Doc' 클릭
4) 좌측 메뉴에서 보통 맨 마지막에 있는 'Dependency Versions' 클릭
5) 사용할 라이브러리를 찾음. 만약 있으면 버전 명시 안해도 됨
*/

plugins {
	id 'java'
	id 'org.springframework.boot' version '3.2.6'
}

allprojects {
	group = 'com.subride'
	version = '0.0.1-SNAPSHOT'

	apply plugin: 'java'
	apply plugin: 'io.spring.dependency-management'

	/*
    Gradle 8.7 부터 자바 버전 지정 방식 변경
    이전 코드는 아래와 같이 Java 항목으로 감싸지 않았고 버전을 직접 지정했음
    sourceCompatibility = '17'
    targetCompatibility = '17'
    */
	java {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	repositories {
		mavenCentral()
	}

	dependencies {
		implementation 'org.springframework.boot:spring-boot-starter-validation'
		implementation 'org.springframework.boot:spring-boot-starter-aop'
		implementation 'com.google.code.gson:gson'               		//Json처리
		compileOnly 'org.projectlombok:lombok'
		annotationProcessor 'org.projectlombok:lombok'

		/*
        TEST를 위한 설정
        */
		//=====================================================
		testImplementation 'org.springframework.boot:spring-boot-starter-test'

		//--JUnit, Mokito Test
		testImplementation 'org.junit.jupiter:junit-jupiter-api'
		testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'

		testImplementation 'org.mockito:mockito-core'
		testImplementation 'org.mockito:mockito-junit-jupiter'

		//-- spring security test
		testImplementation 'org.springframework.security:spring-security-test'

		//-- For mysql System test
		testImplementation 'org.testcontainers:mysql'

		//-- For WebMvc System test
		implementation 'org.springframework.boot:spring-boot-starter-webflux'

		//-- lombok
		// -- @SpringBootTest를 사용하여 전체 애플리케이션 컨텍스트를 로딩되는 테스트 코드에만 사용
		// -- 그 외 단위나 컴포넌트 테스트에 사용하면 제대로 동작안함
		testCompileOnly 'org.projectlombok:lombok'
		testAnnotationProcessor 'org.projectlombok:lombok'
		//=============================================
	}

	//==== Test를 위한 설정 ===
	sourceSets {
		test {
			java {
				srcDirs = ['src/test/java']
			}
		}
	}
	test {
		useJUnitPlatform()
		include '**/*Test.class'		//--클래스 이름이 Test로 끝나는 것만 포함함
	}
	//==========================
}

subprojects {
	apply plugin: 'org.springframework.boot'
}

project(':common') {
	bootJar.enabled = false
	jar.enabled = true
}

configure(subprojects.findAll { it.name.endsWith('-infra') || it.name == 'transfer'}) {
	dependencies {
		implementation 'org.springframework.boot:spring-boot-starter-web'
		implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
		implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'	//For swagger

		runtimeOnly 'com.mysql:mysql-connector-j'
		implementation 'org.springframework.boot:spring-boot-starter-security'
		implementation 'com.auth0:java-jwt:4.4.0'			//JWT unitlity
	}
}

configure(subprojects.findAll { it.name.endsWith('-biz') }) {
	bootJar.enabled = false
	jar.enabled = true
}

// Define a custom task to build all infra projects
tasks.register('buildAll') {
	dependsOn ':member:member-infra:build',
			':subrecommend:subrecommend-infra:build',
			':mysub:mysub-infra:build',
			':mygrp:mygrp-infra:build',
			':transfer:build'
}
tasks.register('ms1') {
	dependsOn ':member:member-infra:build'
}
tasks.register('ms2') {
	dependsOn ':subrecommend:subrecommend-infra:build'
}
tasks.register('ms3') {
	dependsOn ':mysub:mysub-infra:build'
}
tasks.register('ms4') {
	dependsOn ':mygrp:mygrp-infra:build'
}
tasks.register('ms5') {
	dependsOn ':transfer:build'
}



