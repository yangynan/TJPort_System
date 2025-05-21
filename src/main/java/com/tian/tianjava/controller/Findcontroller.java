package com.tian.tianjava.controller;

import com.tian.tianjava.entity.PageBean;
import com.tian.tianjava.entity.Result;
import com.tian.tianjava.mapper.Findmapper;
import com.tian.tianjava.service.Findservice;

import com.tian.tianjava.entity.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/")
@Validated

//@CrossOrigin(origins = "http://localhost:8081/")
//新写的是我按照你的改了一下，注释掉的是我之前的，是可以用的

public class Findcontroller {
    @Autowired
    public Findmapper findmapper;
    @Autowired
    public Findservice findservice;

    //  全部查询
    @GetMapping("/request")
    public Result findAllList(@DateTimeFormat(pattern = "yyyy-MM-dd")Date date, @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time, Integer id, Integer table){

//        table代表查什么,tableid代表哪个传感器
        List<Schema> outcome =  findservice.find(date, time, id, table);
        System.out.println(outcome);
        System.out.println("requestDate: " + date + ", Time: " + time + ", ID: " + id + ", Table: " + table);
        return Result.success(outcome);
    }
    //    查询一段时间
    @GetMapping("/time")
    public Result findtime(@DateTimeFormat(pattern = "yy-MM-dd")LocalDate date1, @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time1, @DateTimeFormat(pattern = "yy-MM-dd") LocalDate date2, @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time2, Integer id, Integer table){
//        table代表查什么,tableid代表哪个传感器
        List<Schema> outcome = findservice.findwhile(date1, time1,date2,time2, id, table);
        System.out.println(outcome);
        return Result.success(outcome);
    }

    //    分页查询
    @GetMapping("/page")
    public Result pageFind(@RequestParam Integer page, @RequestParam Integer pageSize, @DateTimeFormat(pattern = "yy-MM-dd")Date date, @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time, Integer id, Integer table){
//    @RequestParam接收
//    limit第一个参数= （pageNum-1)+pageSize
//        return findservice.pageFind(pageNum,pageSize,day,time,tableid,table);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔍 当前身份：" + authentication);
        log.info("分页查询, 参数: {},{},{},{},{}",page,pageSize,id,date,time);
        //调用service分页查询
        PageBean pageBean = findservice.pageFind(page,pageSize,date,time,id,table);
        return Result.success(pageBean);

    }
    @GetMapping("/page2")
    public Result pageFind2(@RequestParam Integer page, @RequestParam Integer pageSize, @DateTimeFormat(pattern = "yy-MM-dd")LocalDate date1, @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time1,@DateTimeFormat(pattern = "yy-MM-dd") LocalDate date2, @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time2, Integer id, Integer table){
//    @RequestParam接收
//    limit第一个参数= （pageNum-1)+pageSize
//        return findservice.pageFind(pageNum,pageSize,day,time,tableid,table);
        log.info("分页查询, 参数: {},{},{},{},{}",page,pageSize,id,date1,time1,date2);
        //调用service分页查询
        PageBean pageBean = findservice.pageFind2(page,pageSize,id,table,date1,time1,date2,time2);
        return Result.success(pageBean);


    }


}



