package com.tian.tianjava.controller;

import com.tian.tianjava.entity.QueryLog;
import com.tian.tianjava.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/log")
@Validated
public class LogController {
    @Autowired
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/find_log")
    public Object findLog() {
        return logService.findLog();
    }

    @GetMapping("/find_name")
    public Object findLog_name(String username) {
        List<QueryLog> log = logService.findLog();

        System.out.println(log);
        return logService.findLog_name(username);
    }

    @GetMapping("/find_keyword")
    public Object findLogsByKeyword(String keyword){
        return logService.findLogsByKeyword(keyword);
    }

    @GetMapping("/find_time")
    public Object findLog_time(int hours){
        return logService.findLog_time(hours);
    }


}
