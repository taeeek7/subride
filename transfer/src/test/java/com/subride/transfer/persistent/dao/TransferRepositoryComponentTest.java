package com.subride.transfer.persistent.dao;

import com.subride.transfer.common.config.SecurityConfig;
import com.subride.transfer.common.jwt.JwtTokenProvider;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.persistent.repository.ITransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.url=jdbc:tc:mysql:8.0.29:///transfer",
        "spring.datasource.username=root",
        "spring.datasource.password=P@ssw0rd$",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class TransferRepositoryComponentTest {
    @Autowired
    private ITransferRepository transferRepository;

    @BeforeEach
    void setup() {
        cleanup();  // 테스트 데이터 모두 지움

        // 테스트 데이터 생성
        Transfer transfer1 = Transfer.builder()
                .groupId(1L)
                .memberId("user01")
                .amount(BigDecimal.valueOf(10000))
                .transferDate(LocalDate.of(2023, 6, 1))
                .build();

        Transfer transfer2 = Transfer.builder()
                .groupId(1L)
                .memberId("user02")
                .amount(BigDecimal.valueOf(20000))
                .transferDate(LocalDate.of(2023, 7, 1))
                .build();

        transferRepository.saveAll(List.of(transfer1, transfer2));
    }

    @AfterEach
    void cleanup() {
        transferRepository.deleteAll();
    }

    @Test
    void findByGroupIdAndTransferDateBetween_ValidInput_ReturnsTransfers() {
        // Given
        Long groupId = 1L;
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        // When
        List<Transfer> transfers = transferRepository.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

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
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        // When
        List<Transfer> transfers = transferRepository.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

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
        List<Transfer> transfers = transferRepository.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);

        // Then
        assertThat(transfers).isEmpty();
    }
}