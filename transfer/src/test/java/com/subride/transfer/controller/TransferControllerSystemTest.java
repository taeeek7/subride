package com.subride.transfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subride.common.dto.ResponseDTO;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.persistent.repository.ITransferRepository;
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

import java.math.BigDecimal;
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
    private ITransferRepository transferRepository;

    private WebTestClient webClient;

    @BeforeEach
    void setup() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();

        cleanup();  // 테스트 데이터 모두 지움

        Transfer transfer1 = Transfer.builder()
                .groupId(1L)
                .memberId("user01")
                .amount(BigDecimal.valueOf(10000))
                .transferDate(LocalDate.of(2024, 5, 5))
                .build();

        Transfer transfer2 = Transfer.builder()
                .groupId(1L)
                .memberId("user02")
                .amount(BigDecimal.valueOf(20000))
                .transferDate(LocalDate.of(2024, 5, 5))
                .build();

        transferRepository.saveAll(List.of(transfer1, transfer2));
    }

    @AfterEach
    void cleanup() {
        transferRepository.deleteAll();
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
                    assert transferList.size() == 2;
                });
    }

}