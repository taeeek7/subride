package com.subride.transfer.persistent.dao;

import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.common.exception.TransferException;
import com.subride.transfer.persistent.repository.ITransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.debug("Transfer list: {}", transferList);

        return transferList.stream()
                .map(this::toTransferResponse)
                .collect(Collectors.toList());
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        log.info("date -> {}", transfer.getTransferDate());
        return TransferResponse.builder()
                .id(transfer.getId())
                .memberId(transfer.getMemberId())
                .amount(transfer.getAmount())
                .transferDate(transfer.getTransferDate())
                .build();
    }
}