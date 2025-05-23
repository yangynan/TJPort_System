package com.tian.tianjava.aop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryLogMessage implements Serializable {
    private String username;
    private QueryLog log;
}
