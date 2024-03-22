package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReportMapper {
    Double getTurnover(LocalDateTime begin, LocalDateTime end);

    Integer newUserNum(LocalDateTime begin, LocalDateTime end);

    Integer totalUserNum(LocalDateTime time);

    Integer totalOrderNum(LocalDateTime begin, LocalDateTime end);

    Integer validOrderNum(LocalDateTime begin, LocalDateTime end);

    List<GoodsSalesDTO> salesTop10(LocalDateTime begin, LocalDateTime end);
}
