package com.tian.tianjava.service;

import com.tian.tianjava.entity.QueryLog;
import com.tian.tianjava.mapper.QueryLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class LogService {
    @Autowired
    private final QueryLogMapper queryLogMapper;
    public LogService(QueryLogMapper queryLogMapper) {
        this.queryLogMapper = queryLogMapper;
    }

    public List<QueryLog> findLog() {
        return queryLogMapper.findLog();
    }
    public List<QueryLog> findLog_name(String username) {
        return queryLogMapper.findLog_name(username);
    }

    public List<QueryLog> findLogsByKeyword(String keyword) {
        return queryLogMapper.findLogsByKeyword(keyword);
    }
    public List<QueryLog> findLog_time(int hours) {
        return queryLogMapper.findLog_time(hours);
    }

}
