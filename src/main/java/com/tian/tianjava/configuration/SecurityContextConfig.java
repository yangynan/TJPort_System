package com.tian.tianjava.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;




@Configuration
public class SecurityContextConfig {


    @PostConstruct
    public void setup() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        System.out.println("✅ SecurityContext 配置已生效: MODE_INHERITABLETHREADLOCAL");
    }
}
