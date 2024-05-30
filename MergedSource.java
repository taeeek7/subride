// File: mygrp/mygrp-infra/build.gradle
dependencies {
    implementation project(':common')
    implementation project(':mygrp:mygrp-biz')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'

    runtimeOnly 'com.mysql:mysql-connector-j'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2023.0.1"
    }
}

// File: mygrp/mygrp-infra/build/resources/main/application.yml
server:
  port: ${SERVER_PORT:18083}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:mygrp-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/mygrp?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: ${JPA_SHOW_SQL:false}
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: ${JPA_HIBERNATE_FORMAT_SQL:true}
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
    com.subride.mygrp.infra.in: DEBUG
    com.subride.mygrp.infra.out: DEBUG
    feign:
      codec:
        logger:
          level: DEBUG
feign:
  member:
    url: ${MEMBER_URI:http://localhost:18080}
  subrecommend:
    url: ${SUBRECOMMEND_URI:http://localhost:18081}
  mysub:
    url: ${MYSUB_URI:http://localhost:18082}


// File: mygrp/mygrp-infra/src/main/resources/application.yml
server:
  port: ${SERVER_PORT:18083}
spring:
  application:
    name: ${SPRING_APPLICATION_NAME:mygrp-service}
  datasource:
    driver-class-name: ${DB_DRIVER:com.mysql.cj.jdbc.Driver}
    url: ${DB_URL:jdbc:mysql://localhost:3306/mygrp?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:P@ssw0rd$}
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQLDialect
    show-sql: ${JPA_SHOW_SQL:false}
    hibernate:
      ddl-auto: ${JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: ${JPA_HIBERNATE_FORMAT_SQL:true}
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
    com.subride.mygrp.infra.in: DEBUG
    com.subride.mygrp.infra.out: DEBUG
    feign:
      codec:
        logger:
          level: DEBUG
feign:
  member:
    url: ${MEMBER_URI:http://localhost:18080}
  subrecommend:
    url: ${SUBRECOMMEND_URI:http://localhost:18081}
  mysub:
    url: ${MYSUB_URI:http://localhost:18082}


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/MygrpApplication.java
package com.subride.mygrp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MygrpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MygrpApplication.class, args);
    }
}

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/out/feign/SubRecommendFeignClient.java
package com.subride.mygrp.infra.out.feign;

import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "subRecommendFeignClient", url = "${feign.subrecommend.url}")
public interface SubRecommendFeignClient {
    @GetMapping("/api/subrecommend/detail/{subId}")
    ResponseDTO<SubInfoDTO> getSubDetail(@PathVariable("subId") Long subId);

    @GetMapping("/api/subrecommend/list-by-ids")
    ResponseDTO<List<SubInfoDTO>> getSubInfoListByIds(@RequestParam List<Long> subIds);
}


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/out/feign/MySubFeignClient.java
package com.subride.mygrp.infra.out.feign;

import com.subride.common.dto.MySubInfoDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mySubFeignClient", url = "${feign.mysub.url}")
public interface MySubFeignClient {
    @GetMapping("/api/my-subs")
    ResponseDTO<List<MySubInfoDTO>> getMySubList(@RequestParam String userId);

    @GetMapping("/api/my-subs/checking-subscribe")
    ResponseDTO<Boolean> checkSubscription(@RequestParam String userId, @RequestParam Long subId);

    @PostMapping("/api/my-subs/{subId}")
    ResponseDTO<Void> subscribeSub(@PathVariable Long subId, @RequestParam String userId);
}


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/out/feign/MemberFeignClient.java
package com.subride.mygrp.infra.out.feign;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "memberFeignClient", url = "${feign.member.url}")
public interface MemberFeignClient {
    @GetMapping("/api/members/{userId}")
    ResponseDTO<MemberInfoDTO> getMemberInfo(@PathVariable String userId);

    @GetMapping("/api/members")
    ResponseDTO<List<MemberInfoDTO>> getMemberInfoList(@RequestParam String userIds);
}


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/out/entity/GroupEntity.java
package com.subride.mygrp.infra.out.entity;

import com.subride.mygrp.biz.domain.Group;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "my_group")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;
    private String groupName;
    private Long subId;
    private String leaderId;

    @ElementCollection
    @CollectionTable(name = "my_group_member", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "member_id")
    private Set<String> memberIds;

    private String bankName;
    private String bankAccount;
    private int paymentDay;
    private String inviteCode;

    public Group toDomain() {
        Group group = new Group();
        group.setMyGroupId(groupId);
        group.setMyGroupName(groupName);
        group.setSubId(subId);
        group.setLeaderId(leaderId);
        group.setMemberIds(memberIds);
        group.setBankName(bankName);
        group.setBankAccount(bankAccount);
        group.setPaymentDay(paymentDay);
        group.setInviteCode(inviteCode);
        return group;
    }

    public static GroupEntity fromDomain(Group group) {
        return GroupEntity.builder()
                .groupId(group.getMyGroupId())
                .groupName(group.getMyGroupName())
                .subId(group.getSubId())
                .leaderId(group.getLeaderId())
                .memberIds(group.getMemberIds())
                .bankName(group.getBankName())
                .bankAccount(group.getBankAccount())
                .paymentDay(group.getPaymentDay())
                .inviteCode(group.getInviteCode())
                .build();
    }
}


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/out/adapter/MyGroupProviderImpl.java
package com.subride.mygrp.infra.out.adapter;

import com.subride.common.dto.MemberInfoDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.common.dto.SubInfoDTO;
import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.usecase.outport.IMyGroupProvider;
import com.subride.mygrp.infra.exception.InfraException;
import com.subride.mygrp.infra.out.entity.GroupEntity;
import com.subride.mygrp.infra.out.feign.MemberFeignClient;
import com.subride.mygrp.infra.out.feign.MySubFeignClient;
import com.subride.mygrp.infra.out.feign.SubRecommendFeignClient;
import com.subride.mygrp.infra.out.repo.IMyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupProviderImpl implements IMyGroupProvider {
    private final IMyGroupRepository myGroupRepository;
    private final SubRecommendFeignClient subRecommendFeignClient;
    private final MySubFeignClient mySubFeignClient;
    private final MemberFeignClient memberFeignClient;

    @Override
    public Group getMyGroupByGroupId(Long myGroupId, String userId) {
        GroupEntity groupEntity = myGroupRepository.findById(myGroupId)
                .orElseThrow(() -> new InfraException("My group not found"));

        // SubRecommendFeignClient를 사용하여 구독 정보 조회
        ResponseDTO<SubInfoDTO> responseSub = subRecommendFeignClient.getSubDetail(groupEntity.getSubId());
        SubInfoDTO subInfoDTO = responseSub.getResponse();

        // 그룹 엔티티와 구독 정보를 매핑하여 Group 도메인 객체 생성
        Group group = toGroup(groupEntity, subInfoDTO, userId);

        // MemberFeignClient를 사용하여 사용자 정보 조회
        String userIds = String.join(",", groupEntity.getMemberIds());
        ResponseDTO<List<MemberInfoDTO>> responseMember = memberFeignClient.getMemberInfoList(userIds);
        List<MemberInfoDTO> memberInfoDTOList = responseMember.getResponse();
        group.setMembers(memberInfoDTOList);

        return group;
    }

    @Override
    public List<Group> getMyGroupListByUserId(String userId) {
        List<GroupEntity> groupEntityList = myGroupRepository.findByMemberIdsContaining(userId);

        // 그룹 엔티티에서 구독 ID 리스트 추출
        List<Long> subIds = groupEntityList.stream()
                .map(GroupEntity::getSubId)
                .collect(Collectors.toList());

        // SubRecommendFeignClient를 사용하여 구독 정보 리스트 조회
        ResponseDTO<List<SubInfoDTO>> responseDTO = subRecommendFeignClient.getSubInfoListByIds(subIds);
        List<SubInfoDTO> subInfoDTOList = responseDTO.getResponse();

        // 그룹 엔티티와 구독 정보를 매핑하여 Group 도메인 객체 생성
        return groupEntityList.stream()
                .map(groupEntity -> {
                    SubInfoDTO subInfoDTO = subInfoDTOList.stream()
                            .filter(dto -> dto.getSubId().equals(groupEntity.getSubId()))
                            .findFirst()
                            .orElse(null);

                    Group group = toGroup(groupEntity, subInfoDTO, userId);

                    return group;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByMyGroupIdAndUserId(Long myGroupId, String userId) {
        return myGroupRepository.existsByMyGroupIdAndMemberIdsContaining(myGroupId, userId);
    }

    @Override
    public void saveMyGroup(Group myGroup) {
        GroupEntity groupEntity = GroupEntity.fromDomain(myGroup);
        myGroupRepository.save(groupEntity);
    }

    @Override
    public void deleteMyGroupUser(Long myGroupId, String userId) {
        GroupEntity groupEntity = myGroupRepository.findById(myGroupId)
                .orElseThrow(() -> new InfraException("My group not found"));

        groupEntity.getMemberIds().remove(userId);
        myGroupRepository.save(groupEntity);
    }

    @Override
    public Group getMyGroupByInviteCode(String inviteCode) {
        GroupEntity groupEntity = myGroupRepository.findByInviteCode(inviteCode);
        if (groupEntity == null) {
            throw new InfraException("Invalid invite code");
        }
        return groupEntity.toDomain();
    }

    @Override
    public boolean isSubscribed(String userId, Long subId) {
        ResponseDTO<Boolean> response = mySubFeignClient.checkSubscription(userId, subId);
        return response.getResponse();
    }

    @Override
    public boolean subscribeSub(Long subId, String userId) {
        ResponseDTO<Void> response = mySubFeignClient.subscribeSub(subId, userId);
        return response.getCode() == 200;
    }

    private Group toGroup(GroupEntity groupEntity, SubInfoDTO subInfoDTO, String userId) {
        Group group = groupEntity.toDomain();
        group.setSubName(subInfoDTO.getSubName());
        group.setLogo(subInfoDTO.getLogo());
        group.setFee(subInfoDTO.getFee());
        group.setMaxShareNum(subInfoDTO.getMaxShareNum());

        if(userId != null) {    //userId값이 있으면 실제 구독료와 최대 절감액을 계산함
            group.calulatePayedFee(userId);
            group.calulateDiscountedFee(userId);
        }
        return group;
    }
}

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/out/repo/IMyGroupRepository.java
package com.subride.mygrp.infra.out.repo;

import com.subride.mygrp.infra.out.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMyGroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByMemberIdsContaining(String userId);
    boolean existsByMyGroupIdAndMemberIdsContaining(Long myGroupId, String userId);
    void deleteById(Long id);
    GroupEntity findByInviteCode(String inviteCode);
}

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/in/web/MyGroupControllerHelper.java
package com.subride.mygrp.infra.in.web;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupDetailDTO;
import com.subride.mygrp.biz.dto.GroupSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyGroupControllerHelper {

    public List<GroupSummaryDTO> getGroupSummaryList(List<Group> myGroupList) {
        return myGroupList.stream()
                .map(this::toGroupSummaryDTO)
                .collect(Collectors.toList());
    }

    public GroupDetailDTO getGroupDetail(Group group) {
        GroupDetailDTO groupDetailDTO = new GroupDetailDTO();
        BeanUtils.copyProperties(group, groupDetailDTO);
        return groupDetailDTO;
    }

    private GroupSummaryDTO toGroupSummaryDTO(Group myGroup) {
        GroupSummaryDTO groupSummaryDTO = new GroupSummaryDTO();
        groupSummaryDTO.setGroupId(myGroup.getMyGroupId());
        groupSummaryDTO.setGroupName(myGroup.getMyGroupName());
        groupSummaryDTO.setSubName(myGroup.getSubName());
        groupSummaryDTO.setLogo(myGroup.getLogo());
        groupSummaryDTO.setPaymentDay(myGroup.getPaymentDay());
        groupSummaryDTO.setFee(myGroup.getFee());
        groupSummaryDTO.setPayedFee(myGroup.getPayedFee());
        groupSummaryDTO.setDiscountedFee(myGroup.getDiscountedFee());
        return groupSummaryDTO;
    }
}

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/in/web/MyGroupController.java
package com.subride.mygrp.infra.in.web;

import com.subride.common.dto.ResponseDTO;
import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupDetailDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.biz.dto.GroupSummaryDTO;
import com.subride.mygrp.biz.usecase.inport.IMyGroupService;
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

@Tag(name = "마이그룹 서비스 API")
@RestController
@SuppressWarnings("unused")
@RequestMapping("/api/my-groups")
@SecurityRequirement(name = "bearerAuth")    //이 어노테이션이 없으면 요청 헤더에 Authorization헤더가 안 생김
@RequiredArgsConstructor
public class MyGroupController {
    private final IMyGroupService myGroupService;
    private final MyGroupControllerHelper myGroupControllerHelper;

    @Operation(summary = "사용자의 썹그룹 목록 리턴")
    @Parameters({
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<GroupSummaryDTO>>> getMyGroupList(@RequestParam String userId) {
        List<Group> groupGroupList = myGroupService.getMyGroupSummaryList(userId);

        List<GroupSummaryDTO> groupSummaryDTOList = myGroupControllerHelper.getGroupSummaryList(groupGroupList);

        return ResponseEntity.ok(ResponseDTO.<List<GroupSummaryDTO>>builder()
                .code(200)
                .message("마이그룹 목록 조회 성공")
                .response(groupSummaryDTOList)
                .build());
    }

    @Operation(summary = "썹그룹 상세 정보 리턴")
    @Parameters({
            @Parameter(name = "groupId", in = ParameterIn.PATH, description = "썹그룹ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = false)
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<ResponseDTO<GroupDetailDTO>> getMyGroupDetail(
            @PathVariable Long myGroupId, @RequestParam(required = false) String userId) {
        Group group = myGroupService.getMyGroupDetail(myGroupId, userId);
        GroupDetailDTO groupDetailDTO = myGroupControllerHelper.getGroupDetail(group);

        return ResponseEntity.ok(ResponseDTO.<GroupDetailDTO>builder()
                .code(200)
                .message("마이그룹 상세 조회 성공")
                .response(groupDetailDTO)
                .build());
    }

    @Operation(summary = "썹그룹 생성", description = "새로운 썹그룹을 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseDTO<Void>> createMyGroup(@RequestBody GroupCreateDTO groupCreateDTO) {
        myGroupService.createMyGroup(groupCreateDTO);
        return ResponseEntity.ok(ResponseDTO.<Void>builder()
                .code(200)
                .message("마이그룹 생성 성공")
                .build());
    }

    @Operation(summary = "썹그룹 참여", description = "사용자ID와 썹그룹ID를 이용하여 썹그룹 참여정보 생성")
    @PostMapping("/join")
    public ResponseEntity<ResponseDTO<Void>> joinMyGroup(@RequestBody GroupJoinDTO groupJoinDTO) {
        myGroupService.joinMyGroup(groupJoinDTO);
        return ResponseEntity.ok(ResponseDTO.<Void>builder()
                .code(200)
                .message("마이그룹 참여 성공")
                .build());
    }

    @Operation(summary = "썹그룹 탈퇴", description = "썹그룹에서 탈퇴합니다.")
    @Parameters({
            @Parameter(name = "myGroupId", in = ParameterIn.PATH, description = "썹그룹ID", required = true),
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "사용자ID", required = true)
    })
    @DeleteMapping("/{myGroupId}")
    public ResponseEntity<ResponseDTO<Void>> leaveMyGroup(@PathVariable Long myGroupId, @RequestParam String userId) {
        myGroupService.leaveMyGroup(myGroupId, userId);
        return ResponseEntity.ok(ResponseDTO.<Void>builder()
                .code(200)
                .message("마이그룹 탈퇴 성공")
                .build());
    }
}

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/jwt/JwtAuthenticationInterceptor.java
package com.subride.mygrp.infra.common.jwt;

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

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/jwt/JwtAuthenticationFilter.java
// CommonJwtAuthenticationFilter.java
package com.subride.mygrp.infra.common.jwt;

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

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/jwt/JwtTokenProvider.java
// CommonJwtTokenProvider.java
package com.subride.mygrp.infra.common.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.subride.mygrp.infra.exception.InfraException;
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
            throw new InfraException(0, "Invalid refresh token");
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

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/config/SecurityConfig.java
package com.subride.mygrp.infra.common.config;

import com.subride.mygrp.infra.common.jwt.JwtAuthenticationFilter;
import com.subride.mygrp.infra.common.jwt.JwtTokenProvider;
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


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/config/LoggingAspect.java
package com.subride.mygrp.infra.common.config;

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

// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/common/config/SpringDocConfig.java
package com.subride.mygrp.infra.common.config;

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
                        .title("마이그룹 서비스 API")
                        .version("v1.0.0")
                        .description("마이그룹 서비스 API 명세서입니다. "));
    }
}


// File: mygrp/mygrp-infra/src/main/java/com/subride/mygrp/infra/exception/InfraException.java
package com.subride.mygrp.infra.exception;

import lombok.Getter;

@Getter
public class InfraException extends RuntimeException {
    private int code;

    public InfraException(String message) {
        super(message);
    }

    public InfraException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfraException(int code, String message) {
        super(message);
        this.code = code;
    }

    public InfraException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}

// File: mygrp/mygrp-biz/build.gradle
dependencies {
    implementation project(':common')
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/dto/GroupJoinDTO.java
package com.subride.mygrp.biz.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupJoinDTO {
    private String inviteCode;
    private String userId;
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/dto/GroupSummaryDTO.java
package com.subride.mygrp.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupSummaryDTO {
    private Long groupId;
    private String groupName;
    private String subName;
    private String logo;
    private int paymentDay;
    private Long fee;
    private Long payedFee;
    private Long discountedFee;
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/dto/GroupDetailDTO.java
package com.subride.mygrp.biz.dto;

import com.subride.common.dto.MemberInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class GroupDetailDTO {
    private Long groupId;
    private String groupName;
    private Long subId;
    private String leaderId;
    private Set<String> memberIds;
    private String bankName;
    private String bankAccount;
    private int paymentDay;
    private String inviteCode;

    private String subName;
    private String logo;
    private Long fee;
    private int maxShareNum;

    private Long payedFee;
    private Long discountedFee;

    private List<MemberInfoDTO> members;
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/dto/GroupCreateDTO.java
package com.subride.mygrp.biz.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupCreateDTO {
    private String groupName;
    private Long subId;
    private String leaderId;
    private String bankName;
    private String bankAccount;
    private int paymentDay;
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/usecase/inport/IMyGroupService.java
package com.subride.mygrp.biz.usecase.inport;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;

import java.util.List;

public interface IMyGroupService {
    List<Group> getMyGroupSummaryList(String userId);
    Group getMyGroupDetail(Long myGroupId, String userId);
    void createMyGroup(GroupCreateDTO groupCreateDTO);
    void joinMyGroup(GroupJoinDTO groupJoinDTO);
    void leaveMyGroup(Long myGroupId, String userId);
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/usecase/outport/IMyGroupProvider.java
package com.subride.mygrp.biz.usecase.outport;

import com.subride.mygrp.biz.domain.Group;

import java.util.List;

public interface IMyGroupProvider {
    Group getMyGroupByGroupId(Long myGroupId, String userId);
    Group getMyGroupByInviteCode(String invideCode);
    List<Group> getMyGroupListByUserId(String userId);
    boolean existsByMyGroupIdAndUserId(Long myGroupId, String userId);
    void saveMyGroup(Group group);
    void deleteMyGroupUser(Long myGroupId, String userId);
    boolean isSubscribed(String userId, Long subId);
    boolean subscribeSub(Long subId, String userId);
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/usecase/service/MyGroupServiceImpl.java
package com.subride.mygrp.biz.usecase.service;

import com.subride.mygrp.biz.domain.Group;
import com.subride.mygrp.biz.dto.GroupCreateDTO;
import com.subride.mygrp.biz.dto.GroupJoinDTO;
import com.subride.mygrp.biz.exception.BizException;
import com.subride.mygrp.biz.usecase.inport.IMyGroupService;
import com.subride.mygrp.biz.usecase.outport.IMyGroupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyGroupServiceImpl implements IMyGroupService {
    private final IMyGroupProvider myGroupProvider;

    @Override
    public List<Group> getMyGroupSummaryList(String userId) {
        List<Group> myGroupList = myGroupProvider.getMyGroupListByUserId(userId);
        return myGroupList;
    }

    @Override
    public Group getMyGroupDetail(Long myGroupId, String userId) {
        return myGroupProvider.getMyGroupByGroupId(myGroupId, userId);
    }

    @Override
    public void createMyGroup(GroupCreateDTO groupCreateDTO) {
        Group myGroup = new Group();
        myGroup.setMyGroupName(groupCreateDTO.getGroupName());
        myGroup.setSubId(groupCreateDTO.getSubId());
        myGroup.setLeaderId(groupCreateDTO.getLeaderId());
        myGroup.setBankName(groupCreateDTO.getBankName());
        myGroup.setBankAccount(groupCreateDTO.getBankAccount());
        myGroup.setPaymentDay(groupCreateDTO.getPaymentDay());

        myGroupProvider.saveMyGroup(myGroup);
    }

    @Override
    public void joinMyGroup(GroupJoinDTO groupJoinDTO) {
        Group myGroup = myGroupProvider.getMyGroupByInviteCode(groupJoinDTO.getInviteCode());

        if (myGroup.getMemberIds().size() >= myGroup.getMaxShareNum()) {
            throw new BizException("The group is already full.");
        }

        myGroup.getMemberIds().add(groupJoinDTO.getUserId());
        myGroupProvider.saveMyGroup(myGroup);

        // 사용자가 그룹의 구독서비스에 가입되어 있지 않으면 구독서비스 가입 처리
        if (!myGroupProvider.isSubscribed(groupJoinDTO.getUserId(), myGroup.getSubId())) {
            myGroupProvider.subscribeSub(myGroup.getSubId(), groupJoinDTO.getUserId());
        }
    }

    @Override
    public void leaveMyGroup(Long myGroupId, String userId) {
        if (!myGroupProvider.existsByMyGroupIdAndUserId(myGroupId, userId)) {
            throw new BizException("You are not a member of this group.");
        }
        myGroupProvider.deleteMyGroupUser(myGroupId, userId);
    }
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/domain/Group.java
package com.subride.mygrp.biz.domain;

import com.subride.common.dto.MemberInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Group {
    private Long myGroupId;
    private String myGroupName;
    private Long subId;
    private String leaderId;
    private Set<String> memberIds;
    private String bankName;
    private String bankAccount;
    private int paymentDay;
    private String inviteCode;

    private String subName;
    private String logo;
    private Long fee;
    private int maxShareNum;

    private Long payedFee;
    private Long discountedFee;

    private List<MemberInfoDTO> members;

    //-- 실제로 내고 있는 구독료 계산
    public void calulatePayedFee(String userId) {
        if (this.memberIds.contains(userId)) {
            this.discountedFee = this.fee / this.memberIds.size();
        } else {
            this.discountedFee = this.fee;
        }
    }

    //-- 절감할 수 있는 금액 계산
    public void calulateDiscountedFee(String userId) {
        if (this.memberIds.contains(userId)) {
            this.discountedFee = this.fee - this.fee / this.memberIds.size();
        } else {
            this.discountedFee = this.fee - this.fee / this.maxShareNum;
        }
    }
}

// File: mygrp/mygrp-biz/src/main/java/com/subride/mygrp/biz/exception/BizException.java
package com.subride.mygrp.biz.exception;

public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}

// File: /Users/ondal/workspace/subride/settings.gradle
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



// File: /Users/ondal/workspace/subride/build.gradle
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

configure(subprojects.findAll { it.name.endsWith('-infra') }) {
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

