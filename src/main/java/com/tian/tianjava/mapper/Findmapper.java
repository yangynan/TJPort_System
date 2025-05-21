package com.tian.tianjava.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tian.tianjava.entity.Schema;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Mapper

public interface Findmapper extends BaseMapper {

    //    List<Schema> getWindSpeed(Date day, LocalTime time, Integer tableid);
    List<Schema> find(Date day, LocalTime time, Integer tableid,Integer table);
    List<Schema> findwhile(LocalDate day1, LocalTime time1, LocalDate day2, LocalTime time2, Integer tableid, Integer table);
    List<Schema> pageFind(Integer pageNum, Integer pageSize,Date day, LocalTime time, Integer tableid,Integer table);
    List<Schema> pageFindall(Integer pageNum, Integer pageSize,Date day, LocalTime time,Integer table);
    Integer count(Date day, LocalTime time, Integer tableid,Integer table);
    Integer count2(LocalDate day1, LocalTime time1, LocalDate day2, LocalTime time2, Integer tableid, Integer table);
    List<Schema> pageFindtime(Integer pageNum, Integer pageSize,LocalDate day1, LocalTime time1,LocalDate day2, LocalTime time2, Integer tableid,Integer table);
    List<Schema> pageFindtimeALL(Integer pageNum, Integer pageSize,LocalDate day1, LocalTime time1,LocalDate day2, LocalTime time2, Integer table);
}