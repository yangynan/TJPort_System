package com.tian.tianjava.configuration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration//确保提前执行下面的函数
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }




    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("其他请求需要身份认证");
        http.cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ 允许跨域
                .csrf(csrf -> csrf.disable()) // ✅ 关闭 CSRF 保护
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // ✅ 确保 Spring Security 使用 session ALWAYS  IF_REQUIRED
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/log/**").hasRole("admin-part")
                        .requestMatchers("/user/delete").hasRole("admin-part")
                        .requestMatchers("/user/login", "/user/register", "/user/sendCode", "/public/**", "/static/**").permitAll()
                        .anyRequest().authenticated()) // 其他请求需要身份认证


                .formLogin(login -> login
                        .successHandler((request, response, authentication) -> {
                            System.out.println("✅ 登录成功：" + authentication.getName());
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .failureHandler((request, response, exception) -> {
                            System.out.println("❌ 登录失败：" + exception.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                ) .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"未登录，请先登录！\"}");
                        })
                );

        System.out.println("zz"+SecurityContextHolder.getContext().getAuthentication());

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8081")); // ✅ 允许前端访问
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // ✅ 允许所有请求头
        configuration.setAllowCredentials(true); // ✅ 允许浏览器携带 Cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
//    http://localhost:8080/request?day=24-05-24&time=00:00:00&tableid=1&table=0
    public SecurityConfig() {
        // ✅ 允许 SecurityContext 继承到子线程，避免 AOP 获取 anonymousUser
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public SecurityContextPersistenceFilter securityContextPersistenceFilter() {
        return new SecurityContextPersistenceFilter();
    }


}


//
//import org.springframework.context.annotation.Bean;
//        import org.springframework.context.annotation.Configuration;
//        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//        import org.springframework.security.web.SecurityFilterChain;
//        import org.springframework.web.cors.CorsConfiguration;
//        import org.springframework.web.cors.CorsConfigurationSource;
//        import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//        import java.util.List;







