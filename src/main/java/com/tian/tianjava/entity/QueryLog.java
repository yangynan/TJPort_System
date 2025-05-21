package com.tian.tianjava.entity;



import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

//分页查询结果封装类
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryLog {

//    @Id
//    @GeneratedValue
//    private Long id;
//
//    private String userId;  // 记录用户 ID
//    private String sql_query;     // 记录执行的 SQL 查询
////    private String methodName; // 记录方法名
//    private String timestamp;   // 记录查询时间
//
//    // Getter and Setter
    private Integer id;
    private String username;               // ✅ 映射 username
    private String sqlQuery;              // ✅ 映射 sql_query
    private LocalDateTime timestamp;      // ✅ 映射 timestamp
}
