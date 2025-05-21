package com.tian.tianjava.mapper;

import com.tian.tianjava.entity.QueryLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QueryLogMapper {

    @Insert("INSERT INTO query_log (username, sql_query) VALUES (#{username}, #{sqlQuery})")
    void insertLog(String username, String sqlQuery);



    @Select("SELECT * FROM query_log ORDER BY timestamp DESC")
    List<QueryLog> findLog(); // 所有日志

    @Select("SELECT * FROM query_log WHERE username = #{username} ORDER BY timestamp DESC")
    List<QueryLog> findLog_name(@Param("username") String username);

    @Select("SELECT * FROM query_log WHERE sql_query LIKE CONCAT('%', #{keyword}, '%') ORDER BY timestamp DESC")
    List<QueryLog> findLogsByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM query_log " +
            "WHERE timestamp >= NOW() - INTERVAL #{hours} HOUR " +
            "ORDER BY timestamp DESC")
    List<QueryLog> findLog_time(@Param("hours") int hours);





}
