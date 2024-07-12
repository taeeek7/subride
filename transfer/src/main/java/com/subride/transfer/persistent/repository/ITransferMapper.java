package com.subride.transfer.persistent.repository;

import com.subride.transfer.persistent.entity.Transfer;
import org.springframework.data.repository.query.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Mapper
@Repository
@SuppressWarnings("unused")
public interface ITransferMapper {
    List<Transfer> findByGroupIdAndTransferDateBetween(@Param("groupId") Long groupId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    void save(Transfer transfer);
    void deleteAll();
    void insertList(List<Transfer> transfers);
}