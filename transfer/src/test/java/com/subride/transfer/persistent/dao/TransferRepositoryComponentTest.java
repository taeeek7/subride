package com.subride.transfer.persistent.dao;

import com.subride.transfer.common.config.SecurityConfig;
import com.subride.transfer.common.jwt.JwtTokenProvider;
import com.subride.transfer.persistent.entity.Transfer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class TransferRepositoryComponentTest {
    @Autowired
    private ITransferRepository transferMapper;

    @BeforeEach
    void setup() {
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
        assertThat(transfers.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(transfers.get(0).getTransferDate()).isEqualTo(LocalDate.of(2024, 6, 1));

        assertThat(transfers.get(1).getGroupId()).isEqualTo(groupId);
        assertThat(transfers.get(1).getMemberId()).isEqualTo("user02");
        assertThat(transfers.get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(transfers.get(1).getTransferDate()).isEqualTo(LocalDate.of(2024, 7, 1));
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