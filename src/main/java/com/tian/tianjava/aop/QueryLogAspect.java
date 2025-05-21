package com.tian.tianjava.aop;



import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import com.tian.tianjava.entity.PageBean;
import com.tian.tianjava.mapper.QueryLogMapper;
import com.tian.tianjava.mapper.Usermapper;
//import com.tian.tianjava.configuration.RabbitMQConfig;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class QueryLogAspect {

//    @Autowired
//    private QueryLogMapper queryLogMapper;  // 假设你有 LogMapper 来执行插入日志的操作

    @Autowired
    private Usermapper usermapper;  // 假设你有 LogMapper 来执行插入日志的操作


//    @Autowired
//    private RabbitTemplate rabbitTemplate;



    @AfterReturning(
            pointcut = "execution(* com.tian.tianjava.service.*.*(..)) && " +
                    "!execution(* com.tian.tianjava.service.Userservice.*(..)) && "+
                    "!execution(* com.tian.tianjava.service.EmailService.*(..)) && "+
                    "!execution(* com.tian.tianjava.service.LogService.*(..)) ",
//            +"!execution(* com.tian.tian_demo.service.Findservice.pageFind(..))&&"
//            +"!execution(* com.tian.tian_demo.service.Findservice.pageFind2(..))",
            returning = "result"
    )
    public void logQuery(JoinPoint joinPoint, Object result) {
        // 如果 result 不是成功的业务响应，则不记录日志
        if (!isSuccessResult(result)) {
            System.out.println("查询未成功，跳过日志记录。");
            return;
        }

        // 获取方法参数
        Object[] args = joinPoint.getArgs();


        // ✅ 手动获取最新的 SecurityContext
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("AOP 重新获取的 SecurityContext 认证信息：" + authentication);

        String username = "匿名用户";
        Integer userId = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
            userId = usermapper.findUserIdByUsername(username); // 你需要实现此方法
            System.out.println("username：" + username);
            System.out.println("userId：" + userId);

        }

        String methodName = joinPoint.getSignature().getName();
        String sqlQuery = "执行方法:" + methodName + " 参数: " + Arrays.toString(args);

        QueryLog log = new QueryLog();
//        log.setName(username);
        log.setUserId(userId);
        log.setSqlQuery(sqlQuery);
        log.setCreatedAt(LocalDateTime.now());
        System.out.println("log:" + log);

//        QueryLogMessage message = new QueryLogMessage(username, log);
//        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);



        System.out.println("已异步发送日志消息到MQ: userId=" + userId + ", 内容=" + sqlQuery);
    }

    // 判断是否是成功的 Result 对象
    private boolean isSuccessResult(Object result) {
        if (result == null) {
            System.out.println("查询结果为空，跳过日志记录。");
            return false;
        }

        System.out.println("result 对象：" + result);
        System.out.println("result 类型：" + result.getClass().getName());

        // 1️⃣ 如果 result 是 List 类型，检查是否为空
        if (result instanceof List) {
            if (((List<?>) result).isEmpty()) {
                System.out.println("查询结果为空列表，跳过日志记录。");
                return false;
            }
        }

        // 2️⃣ 如果 result 是 PageBean 类型，检查 total 是否为 0
        if (result instanceof PageBean) {
            try {
                // 通过反射获取 PageBean 的 total 字段
                Field totalField = result.getClass().getDeclaredField("total");
                totalField.setAccessible(true); // 允许访问私有字段
                int total = (int) totalField.get(result);

                System.out.println("PageBean.total = " + total);

                if (total == 0) {
                    System.out.println("PageBean.total = 0，无数据，跳过日志记录。");
                    return false;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // 其余情况记录日志
        return true;
    }}
