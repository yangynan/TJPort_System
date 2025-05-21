package com.tian.tianjava.service;

import com.tian.tianjava.entity.PageBean;
import com.tian.tianjava.entity.Schema;
import com.tian.tianjava.mapper.Findmapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service

public class Findservice {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public Findmapper findmapper;

    @Autowired
    private RedissonClient redissonClient;


//    public List<Schema> find(Date day, LocalTime time, Integer tableid, Integer table) {
//        String dayStr = (day != null) ? day.toString() : "null";
//        String timeStr = (time != null) ? time.toString() : "null";
//        String idStr = (tableid != null) ? tableid.toString() : "null";
//        String tableStr = (table != null) ? table.toString() : "null";
//
//        String key = "find:" + dayStr + ":" + timeStr + ":" + idStr + ":" + tableStr;
//
//
//        // 1. 尝试从 Redis 获取缓存
//        Object cachedData = redisTemplate.opsForValue().get(key);
//        if (cachedData != null) {
//            return (List<Schema>) cachedData;
//        }
//
//        // 2. 查询数据库
//        System.out.println("第一次查数据库");
//
//        List<Schema> result;
//        if (tableid == null || tableid == 0) {
//            List<Schema> table_1 = findmapper.find(day, time, 1, table);
//            List<Schema> table_2 = findmapper.find(day, time, 2, table);
//            List<Schema> table_3 = findmapper.find(day, time, 3, table);
//            table_1.addAll(table_2);
//            table_1.addAll(table_3);
//            result = table_1;
//        } else {
//            result = findmapper.find(day, time, tableid, table);
//        }
//
//        // 3. 判断是否是“今日”数据，设置不同缓存时间
//        long ttl; // 缓存时间（秒）
//        LocalDate inputDate = day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        LocalDate today = LocalDate.now();
//
//        if (inputDate.equals(today)) {
//            // 是今天的数据，热点缓存 12 小时
//            ttl = 12 * 60 * 60;
//        } else {
//            // 普通数据缓存 10 分钟
//            ttl = 10 * 60;
//        }
//// 4. 设置缓存
//        if (result != null && !result.isEmpty()) {
//            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
//        } else {
//            // 缓存空值 1 分钟，防止缓存穿透（可选）
//            redisTemplate.opsForValue().set(key, Collections.emptyList(), 60, TimeUnit.SECONDS);
//        }
//
////
//
//        return result;
//    }


    public List<Schema> find(Date day, LocalTime time, Integer tableid, Integer table) {
        // 1. 构造缓存 Key，避免 null 报错
        String dayStr = (day != null) ? day.toString() : "null";
        String timeStr = (time != null) ? time.toString() : "null";
        String idStr = (tableid != null) ? tableid.toString() : "null";
        String tableStr = (table != null) ? table.toString() : "null";
        String key = "find:" + dayStr + ":" + timeStr + ":" + idStr + ":" + tableStr;

        // 2. 先从 Redis 缓存中读取数据（命中缓存直接返回）
        Object cachedData = redisTemplate.opsForValue().get(key);
        System.out.println("Cached data: " + cachedData);
        if (cachedData != null) {
            return (List<Schema>) cachedData;
        }

        // 3. 构造锁的 key（每个查询唯一的锁）
        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);
        System.out.println("Lock Key: " + lockKey);

        try {
            // 4. 尝试获取分布式锁，最多等待 500ms，锁过期 10 秒自动释放
            if (lock.tryLock(500, 10000, TimeUnit.MILLISECONDS)) {
                try {
                    // 5. 双重检查缓存，防止重复查库
                    cachedData = redisTemplate.opsForValue().get(key);
                    if (cachedData != null) {
                        return (List<Schema>) cachedData;
                    }

                    System.out.println("【查数据库】key=" + key);

                    // 6. 查询数据库
                    List<Schema> result;
                    if (tableid == null || tableid == 0) {
                        List<Schema> table_1 = findmapper.find(day, time, 1, table);
                        List<Schema> table_2 = findmapper.find(day, time, 2, table);
                        List<Schema> table_3 = findmapper.find(day, time, 3, table);
                        table_1.addAll(table_2);
                        table_1.addAll(table_3);
                        result = table_1;
                    } else {
                        result = findmapper.find(day, time, tableid, table);
                    }

                    // 7. 判断是否是“今天”的数据，用不同缓存时间
                    long ttl;
                    LocalDate inputDate = day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate today = LocalDate.now();
                    if (inputDate.equals(today)) {
                        ttl = 12 * 60 * 60+new Random().nextInt(600); // 热点缓存 12 小时

                    } else {
                        ttl = 10 * 60+new Random().nextInt(60); // 普通缓存 10 分钟
                    }

                    // 8. 缓存设置，防止缓存穿透（空数据缓存 60 秒）
                    if (result != null && !result.isEmpty()) {
                        redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(key, Collections.emptyList(), 60, TimeUnit.SECONDS);
                    }

                    return result;
                } finally {
                    // 9. 无论如何释放锁，防止死锁
                    lock.unlock();
                }
            } else {
                // 10. 未抢到锁（其他线程正在查），等待一会后重试缓存
                Thread.sleep(100);
                cachedData = redisTemplate.opsForValue().get(key);
                if (cachedData != null) {
                    return (List<Schema>) cachedData;
                } else {
                    // 最终失败也返回空数据，防止系统挂掉
                    return Collections.emptyList();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取Redis分布式锁失败", e);
        }
    }


    public PageBean pageFind(Integer pageNum, Integer pageSize, Date day, LocalTime time, Integer tableid, Integer table) {
        // 1. 构建缓存的 key
        String dayStr = (day != null) ? day.toString() : "null";
        String timeStr = (time != null) ? time.toString() : "null";
        String idStr = (tableid != null) ? tableid.toString() : "null";
        String tableStr = (table != null) ? table.toString() : "null";
        String key = "pageFind:" + pageNum + ":" + pageSize + ":" + dayStr + ":" + timeStr + ":" + idStr + ":" + tableStr;

        // 2. 查询缓存，命中直接返回
        Object cached = redisTemplate.opsForValue().get(key);
        System.out.println("Cached data: " + cached);
        if (cached != null) {
            return (PageBean) cached;
        }

        // 3. 构建锁 key，并尝试获取锁（防止缓存击穿）
        String lockKey = "lock:" + key;
        System.out.println("Lock Key: " + lockKey);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(500, 10000, TimeUnit.MILLISECONDS)) {
                try {
                    // 4. 再次检查缓存（双重检查）
                    cached = redisTemplate.opsForValue().get(key);
                    if (cached != null) {
                        return (PageBean) cached;
                    }

                    // 5. 开始查数据库
                    int offset = (pageNum - 1) * pageSize;
                    Integer total;
                    PageBean result;

                    if (tableid == null || tableid == 0) {
                        List<Schema> table_1 = findmapper.pageFindall(offset, pageSize, day, time, table);
                        Integer count_1 = findmapper.count(day, time, 1, table);
                        Integer count_2 = findmapper.count(day, time, 2, table);
                        Integer count_3 = findmapper.count(day, time, 3, table);
                        total = count_1 + count_2 + count_3;
                        result = new PageBean(total, table_1);
                    } else {
                        total = findmapper.count(day, time, tableid, table);
                        List<Schema> dataList = findmapper.pageFind(offset, pageSize, day, time, tableid, table);
                        result = new PageBean(total, dataList);
                    }

                    // 6. 判断是否热点数据
                    long ttl;
                    LocalDate inputDate = day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate today = LocalDate.now();

                    if (inputDate.equals(today)) {
                        ttl = 12 * 60 * 60; // 热点缓存 12小时
                    } else {
                        ttl = 10 * 60; // 普通数据 10分钟
                    }

                    // 7. 写入缓存（防止穿透）
                    if (result != null&&result.getTotal()!=0) {
                        redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(key, new PageBean(0, Collections.emptyList()), 60, TimeUnit.SECONDS);
                    }

                    return result;
                } finally {
                    // 8. 释放锁
                    lock.unlock();
                }
            } else {
                // 9. 没抢到锁，等待一会再尝试从缓存中取
                Thread.sleep(100);
                cached = redisTemplate.opsForValue().get(key);
                if (cached != null) {
                    return (PageBean) cached;
                } else {
                    // 如果缓存还是没数据，返回空
                    return new PageBean(0, Collections.emptyList());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("分页Redis锁异常", e);
        }
    }
//
//
//    public PageBean pageFind(Integer pageNum, Integer pageSize, Date day, LocalTime time, Integer tableid, Integer table) {
//        String dayStr = (day != null) ? day.toString() : "null";
//        String timeStr = (time != null) ? time.toString() : "null";
//        String idStr = (tableid != null) ? tableid.toString() : "null";
//        String tableStr = (table != null) ? table.toString() : "null";
//
//        String key = "find:" + dayStr + ":" + timeStr + ":" + idStr + ":" + tableStr;
//        //String key = "pageFind:" + pageNum + ":" + pageSize + ":" + day + ":" + time + ":" + tableid + ":" + table;
//        Object cached = redisTemplate.opsForValue().get(key);
//        if (cached != null) {
//            return (PageBean) cached;
//        }
//
//        pageNum = (pageNum - 1) * pageSize;
//        Integer total;
//        PageBean result;
//
//        if (tableid == null || tableid == 0) {
//            List<Schema> table_1 = findmapper.pageFindall(pageNum, pageSize, day, time, table);
//            Integer count_1 = findmapper.count(day, time, 1, table);
//            Integer count_2 = findmapper.count(day, time, 2, table);
//            Integer count_3 = findmapper.count(day, time, 3, table);
//            total = count_1 + count_2 + count_3;
//            result = new PageBean(total, table_1);
//        } else {
//            total = findmapper.count(day, time, tableid, table);
//            List<Schema> dataList = findmapper.pageFind(pageNum, pageSize, day, time, tableid, table);
//            result = new PageBean(total, dataList);
//        }
//
//        // redisTemplate.opsForValue().set(key, result, 10, TimeUnit.MINUTES);
//        // 3. 判断是否是“今日”数据，设置不同缓存时间
//        long ttl; // 缓存时间（秒）
//        LocalDate inputDate = day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        LocalDate today = LocalDate.now();
//
//        if (inputDate.equals(today)) {
//            // 是今天的数据，热点缓存 12 小时
//            ttl = 12 * 60 * 60;
//        } else {
//            // 普通数据缓存 10 分钟
//            ttl = 10 * 60;
//        }
//
//        // 4. 设置缓存
//        if (result != null) {
//            redisTemplate.opsForValue().set(key, result, ttl, TimeUnit.SECONDS);
//        } else {
//            // 缓存空值 1 分钟，防止缓存穿透（可选）
//            redisTemplate.opsForValue().set(key, Collections.emptyList(), 60, TimeUnit.SECONDS);
//        }
//
//
//        return result;
//    }

    public List<Schema> findwhile(LocalDate day1, LocalTime time1, LocalDate day2, LocalTime time2, Integer tableid, Integer table) {
        String key = "findwhile:" + day1 + ":" + time1 + ":" + day2 + ":" + time2 + ":" + tableid + ":" + table;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<Schema>) cached;
        }

        System.out.println("第二");
        if (time1 == null) {
            time1 = LocalTime.of(0, 0, 0);
        }
        if (time2 == null) {
            time2 = LocalTime.of(0, 0, 0);
        }

        List<Schema> result;
        if (tableid == null || tableid == 0) {
            List<Schema> table_1 = findmapper.findwhile(day1, time1, day2, time2, 1, table);
            List<Schema> table_2 = findmapper.findwhile(day1, time1, day2, time2, 2, table);
            List<Schema> table_3 = findmapper.findwhile(day1, time1, day2, time2, 3, table);
            table_1.addAll(table_2);
            table_1.addAll(table_3);
            result = table_1;
        } else {
            result = findmapper.findwhile(day1, time1, day2, time2, tableid, table);
        }


        if (result != null && !result.isEmpty()) {
            redisTemplate.opsForValue().set(key, result, 10, TimeUnit.SECONDS);
        } else {
            // 缓存空值 1 分钟，防止缓存穿透（可选）
            redisTemplate.opsForValue().set(key, Collections.emptyList(), 60, TimeUnit.SECONDS);
        }

        return result;
    }

    public PageBean pageFind2(Integer pageNum, Integer pageSize, Integer tableid, Integer table, LocalDate date1, LocalTime time1, LocalDate date2, LocalTime time2) {
        String key = "pageFind2:" + pageNum + ":" + pageSize + ":" + tableid + ":" + table + ":" + date1 + ":" + time1 + ":" + date2 + ":" + time2;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (PageBean) cached;
        }

        pageNum = (pageNum - 1) * pageSize;
        Integer total;
        PageBean result;

        if (time1 == null) {
            time1 = LocalTime.of(0, 0, 0);
        }
        if (time2 == null) {
            time2 = LocalTime.of(0, 0, 0);
        }

        if (tableid == null || tableid == 0) {
            List<Schema> table_1 = findmapper.pageFindtimeALL(pageNum, pageSize, date1, time1, date2, time2, table);
            Integer count_1 = findmapper.count2(date1, time1, date2, time2, 1, table);
            Integer count_2 = findmapper.count2(date1, time1, date2, time2, 2, table);
            Integer count_3 = findmapper.count2(date1, time1, date2, time2, 3, table);
            total = count_1 + count_2 + count_3;
            result = new PageBean(total, table_1);
        } else {
            total = findmapper.count2(date1, time1, date2, time2, tableid, table);
            List<Schema> dataList = findmapper.pageFindtime(pageNum, pageSize, date1, time1, date2, time2, tableid, table);
            result = new PageBean(total, dataList);
        }


        if (result != null) {
            redisTemplate.opsForValue().set(key, result, 10, TimeUnit.SECONDS);
        } else {
            // 缓存空值 1 分钟，防止缓存穿透（可选）
            redisTemplate.opsForValue().set(key, Collections.emptyList(), 60, TimeUnit.SECONDS);
        }
        return result;
    }
}




//
//
//    //    基本查询功能
//    public List<Schema> find(Date day, LocalTime time, Integer tableid, Integer table){
//        System.out.println("第一");
//        if(tableid == null ||tableid == 0){
//            List<Schema> table_1=findmapper.find(day, time, 1, table);
//            List<Schema> table_2=findmapper.find(day, time, 2, table);
//            List<Schema> table_3=findmapper.find(day, time, 3, table);
//            table_1.addAll(table_2);
//            table_1.addAll(table_3);
//            return table_1;
//        }
//        else {
//            return findmapper.find(day, time, tableid, table);
//        }
//    }
//       // 分页查询
//
//    public PageBean pageFind(Integer pageNum, Integer pageSize, Date day, LocalTime time, Integer tableid, Integer table){
//        pageNum = (pageNum-1)*pageSize;
//        Integer total;
//        if(tableid == null ||tableid == 0){
//            List<Schema> table_1=findmapper.pageFindall(pageNum, pageSize,day,time,table);
//            Integer count_1=findmapper.count(day,time,1,table);
//            Integer count_2=findmapper.count(day,time,2,table);
//            Integer count_3=findmapper.count(day,time,3,table);
//            total=count_1+count_2+count_3;
//            return new PageBean(total, table_1);
//        }
//        else {
//            total = findmapper.count(day,time,tableid,table);
//            List<Schema> dataList = findmapper.pageFind(pageNum, pageSize,day,time,tableid,table);
////          封装Page对象
//            return new PageBean(total, dataList);
//        }
//    }
//
//    public List<Schema> findwhile(LocalDate day1, LocalTime time1, LocalDate day2, LocalTime time2, Integer tableid, Integer table){
//
//        System.out.println("第二");
//        if(time1 == null){
//            time1= LocalTime.of(0,0,0);
//        }
//        if(time2 == null){
//            time2= LocalTime.of(0,0,0);
//        }
//        if(tableid == null ||tableid == 0){
//            List<Schema> table_1=findmapper.findwhile(day1, time1, day2, time2, 1, table);
//            List<Schema> table_2=findmapper.findwhile(day1, time1, day2, time2, 2, table);
//            List<Schema> table_3=findmapper.findwhile(day1, time1, day2, time2, 3, table);
//            table_1.addAll(table_2);
//            table_1.addAll(table_3);
//            return table_1;
//        }
//        else {
//            return findmapper.findwhile(day1, time1, day2, time2, tableid, table);
//        }
//    }
//
//    public PageBean pageFind2(Integer pageNum, Integer pageSize,Integer tableid, Integer table, LocalDate date1, LocalTime time1, LocalDate date2, LocalTime time2){
//        pageNum = (pageNum-1)*pageSize;
//        Integer total;
//        if(time1 == null){
//            time1= LocalTime.of(0,0,0);
//        }
//        if(time2 == null){
//            time2= LocalTime.of(0,0,0);
//        }
//
//
//        if(tableid == null ||tableid == 0){
//            List<Schema> table_1=findmapper.pageFindtimeALL(pageNum, pageSize,date1,time1,date2,time2,table);
//            Integer count_1=findmapper.count2(date1, time1, date2, time2, 1, table);
//            Integer count_2=findmapper.count2(date1, time1, date2, time2, 2, table);
//            Integer count_3=findmapper.count2(date1, time1, date2, time2, 3, table);
//            total=count_1+count_2+count_3;
//            return new PageBean(total, table_1);
//        }
//        else {
//            total = findmapper.count2(date1, time1, date2, time2, tableid, table);
//
//            List<Schema> dataList = findmapper.pageFindtime(pageNum, pageSize,date1,time1,date2,time2,tableid,table);
////          封装Page对象
//            return new PageBean(total, dataList);
//        }
//    }
//
//
//
//}