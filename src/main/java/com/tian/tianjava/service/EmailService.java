package com.tian.tianjava.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


    @Service
    public class EmailService {

        @Autowired
        private JavaMailSender mailSender;

        // 从配置文件读取你的发件人邮箱
        @Autowired
        private org.springframework.core.env.Environment env;

        /**
         * 发送简单文本邮件
         */
        public boolean sendSimpleMail(String to, String subject, String content) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(env.getProperty("spring.mail.username")); // 发件人
                message.setTo(to);                                        // 收件人
                message.setSubject(subject);                              // 主题
                message.setText(content);                                 // 正文
                mailSender.send(message);
                return true;
            } catch (Exception e) {
                e.printStackTrace(); // 日志记录
                return false;
            }
        }
    }


