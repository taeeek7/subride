package com.subride.transfer.service;

import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;

import java.util.List;

public interface ITransferService {
    List<TransferResponse> getTransferHistory(Long groupId, Period period);
    void createTestData();
    void deleteAllData();
}
