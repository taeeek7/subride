package com.subride.transfer.persistent.repository;

import com.subride.transfer.persistent.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ITransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByGroupIdAndTransferDateBetween(Long groupId, LocalDate startDate, LocalDate endDate);
}