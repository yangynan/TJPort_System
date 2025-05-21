package com.tian.tianjava.aop;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class QueryLog implements Serializable {
    private String name;
    private Integer userId;    // 必须与user表id对应
    private String sqlQuery;   // 避免使用sql字段名
    private LocalDateTime createdAt;
}

