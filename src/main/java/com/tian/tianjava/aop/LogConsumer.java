//package com.tian.tianjava.aop;
//
//import com.tian.tianjava.mapper.QueryLogMapper;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class LogConsumer {
//
//    @Autowired
//    private QueryLogMapper queryLogMapper;
//
//    @RabbitListener(queues = "log.queue")
//    public void handleLog(QueryLogMessage message) {
//        try {
//            String username = message.getUsername();
//            String sqlQuery = message.getLog().getSqlQuery();
//            System.out.println("username1：" + username);
//
//            queryLogMapper.insertLog(username, sqlQuery);
//
//            System.out.println("消费者已成功写入日志: " + username + " -> " + sqlQuery);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
