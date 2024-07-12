package com.subride.transfer.service;

import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.persistent.dao.TransferProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferProvider transferProvider;

    public List<TransferResponse> getTransferHistory(Long groupId, Period period) {
        return transferProvider.getTransferHistory(groupId, period);
    }

    public void createTestData() {
        transferProvider.createTestData();
    }
}