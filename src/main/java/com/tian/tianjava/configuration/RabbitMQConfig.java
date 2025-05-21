//package com.tian.tianjava.configuration;
//
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    public static final String EXCHANGE_NAME = "log.exchange";
//    public static final String QUEUE_NAME = "log.queue";
//    public static final String ROUTING_KEY = "log.insert";
//
//    // 1. 创建交换机
//    @Bean
//    public DirectExchange logExchange() {
//        return new DirectExchange(EXCHANGE_NAME);
//    }
//
//    // 2. 创建队列
//    @Bean
//    public Queue logQueue() {
//        return new Queue(QUEUE_NAME, true);
//    }
//
//    // 3. 队列绑定到交换机
//    @Bean
//    public Binding bindingLogQueue(Queue logQueue, DirectExchange logExchange) {
//        return BindingBuilder.bind(logQueue).to(logExchange).with(ROUTING_KEY);
//    }
//
//    // 4. 配置生产端序列化为 JSON
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(new Jackson2JsonMessageConverter());
//        return template;
//    }
//
//    // 5. 配置消费者端序列化为 JSON
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(new Jackson2JsonMessageConverter());
//        return factory;
//    }
//}
