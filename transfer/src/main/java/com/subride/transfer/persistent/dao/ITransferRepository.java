package com.subride.transfer.persistent.dao;

import com.subride.transfer.persistent.entity.Transfer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Mapper
@Repository
@SuppressWarnings("unused")
public interface ITransferRepository {
    List<Transfer> findByGroupIdAndTransferDateBetween(@Param("groupId") Long groupId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    void save(Transfer transfer);
    void deleteAll();
    void insertList(List<Transfer> transfers);
}