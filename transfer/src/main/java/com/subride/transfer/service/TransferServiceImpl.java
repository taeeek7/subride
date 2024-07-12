package com.subride.transfer.service;

import com.google.gson.*;
import com.subride.common.dto.GroupMemberDTO;
import com.subride.common.dto.ResponseDTO;
import com.subride.transfer.common.dto.TransferResponse;
import com.subride.transfer.common.enums.Period;
import com.subride.transfer.common.exception.TransferException;
import com.subride.transfer.common.feign.MyGroupFeignClient;
import com.subride.transfer.persistent.entity.Transfer;
import com.subride.transfer.persistent.dao.ITransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferServiceImpl implements ITransferService {
    private final ITransferRepository transferRepository;
    private final MyGroupFeignClient myGroupFeignClient;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new TransferServiceImpl.LocalDateAdapter())
            .create();

    @Override
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

        List<Transfer> transferList = transferRepository.findByGroupIdAndTransferDateBetween(groupId, startDate, endDate);
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

    @Override
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

        transferRepository.insertList(transfers);
    }

    private static class LocalDateAdapter implements JsonSerializer<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public JsonElement serialize(LocalDate localDate, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(localDate));
        }
    }

    @Override
    public void deleteAllData() {
        transferRepository.deleteAll();
    }
}