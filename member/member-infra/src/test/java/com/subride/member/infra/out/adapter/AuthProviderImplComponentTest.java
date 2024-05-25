package com.subride.member.infra.out.adapter;

import com.subride.member.biz.domain.Account;
import com.subride.member.biz.domain.Member;
import com.subride.member.infra.common.config.SecurityConfig;
import com.subride.member.infra.common.jwt.CustomUserDetailsService;
import com.subride.member.infra.common.jwt.JwtTokenProvider;
import com.subride.member.infra.exception.InfraException;
import com.subride.member.infra.out.entity.AccountEntity;
import com.subride.member.infra.out.entity.MemberEntity;
import com.subride.member.infra.out.repo.IAccountRepository;
import com.subride.member.infra.out.repo.IMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///member",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})

/*
@DataJpaTest는 데이터 관련된 Bean만 로딩하므로 추가로 필요한 클래스는 Import 해 줘야 함
추가 필요 클래스가 뭔지 찾는 가장 쉬운 방법은 실행하고 에러 메시지에서 찾는 방법임
 */
@Import({SecurityConfig.class, JwtTokenProvider.class, CustomUserDetailsService.class})
class AuthProviderImplComponentTest {

    private AuthProviderImpl authProvider;

    @Autowired
    private IMemberRepository memberRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setup() {
        authProvider = new AuthProviderImpl(authenticationManager, memberRepository, passwordEncoder, accountRepository);
    }

    @Test
    void signup_ValidInput_SavesMemberAndAccount() {
        // given
        Member member = new Member();
        member.setUserId("testuser");
        member.setUserName("John Doe");
        member.setBankName("Test Bank");
        member.setBankAccount("1234567890");

        Account account = new Account();
        account.setUserId("testuser");
        account.setPassword("password");

        // when
        authProvider.signup(member, account);

        // then
        MemberEntity savedMember = memberRepository.findByUserId("testuser")
                .orElseThrow(() -> new InfraException("Member not found"));

        assertThat(savedMember.getUserId()).isEqualTo(member.getUserId());
        assertThat(savedMember.getUserName()).isEqualTo(member.getUserName());
        assertThat(savedMember.getBankName()).isEqualTo(member.getBankName());
        assertThat(savedMember.getBankAccount()).isEqualTo(member.getBankAccount());

        AccountEntity savedAccount = accountRepository.findByUserId("testuser")
                .orElseThrow(() -> new InfraException("Account not found"));

        assertThat(savedAccount.getUserId()).isEqualTo(account.getUserId());
        assertThat(passwordEncoder.matches(account.getPassword(), savedAccount.getPassword())).isTrue();
    }

}
