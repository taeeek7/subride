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