package com.tian.tianjava.service;
import com.tian.tianjava.mapper.Usermapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.tian.tianjava.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


import java.util.concurrent.ConcurrentHashMap;

@Service
public class Userservice {

    @Autowired
    private Usermapper usermapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // 你之前已有
    private final Map<String, EmailCodeEntry> emailCodeCache = new ConcurrentHashMap<>();
    private final Map<String, Long> emailSendTimeCache = new ConcurrentHashMap<>();



    public ResponseEntity<?> resetPassword(String email, String newPassword, String emailCode) {

        // 查询用户是否存在
        User user = usermapper.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("该邮箱未注册");
        }
// 校验验证码
        EmailCodeEntry entry = emailCodeCache.get(email);
        if (entry == null || entry.isExpired() || !entry.getCode().equals(emailCode)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱验证码错误或已过期");
        }
        // 校验邮箱、新密码、验证码格式

        String validationMsg = validateInput("123456", newPassword);
        if (validationMsg != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationMsg);
        }
        // 修改密码
        String hashedPassword = passwordEncoder.encode(newPassword);
        int updated = usermapper.updatePasswordByEmail(email, hashedPassword);

        if (updated > 0) {
            emailCodeCache.remove(email); // 清除验证码
            return ResponseEntity.ok("密码重置成功，请重新登录");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("密码重置失败，系统异常");
        }
    }


    // 内存中验证码缓存（email -> code + 过期时间）

    /**
     * 发送邮箱验证码（内存缓存实现）
     */

    public ResponseEntity<?> sendEmailCode(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@whut\\.edu\\.cn$")) {//后缀判断公司邮箱
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱格式不正确");
        }

        long now = System.currentTimeMillis();
        Long lastSendTime = emailSendTimeCache.get(email);
        if (lastSendTime != null && now - lastSendTime < 60_000) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("发送太频繁，请稍后再试");
        }

        String code = String.valueOf(new Random().nextInt(899999) + 100000); // 六位数字
        long expireAt = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟后过期
        emailCodeCache.put(email, new EmailCodeEntry(code, expireAt));


        emailSendTimeCache.put(email, now);

        boolean success = emailService.sendSimpleMail(email, "注册验证码", "您的验证码为：" + code + "，有效期5分钟");
        if (success) {
            return ResponseEntity.ok("验证码已发送");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("验证码发送失败");
        }
    }

    /**
     * 注册用户（带邮箱验证码验证）
     */
    public ResponseEntity<?> register(String username, String password, String email, String emailCode) {
        // 格式校验
        String validationMsg = validateInput(username, password);
        if (validationMsg != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationMsg);
        }

        // 校验验证码
        EmailCodeEntry entry = emailCodeCache.get(email);
        if (entry == null || entry.isExpired() || !entry.getCode().equals(emailCode)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱验证码错误或已过期");
        }

        // 用户查重
        if (usermapper.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("注册失败，用户名已存在");
        }

        if (usermapper.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("注册失败，邮箱已注册");
        }
        if (emailCode == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请输入邮箱验证码");
        }


        // 注册逻辑
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setRole("USER");

        if (usermapper.register(user) > 0) {
            emailCodeCache.remove(email); // 清除验证码
            return ResponseEntity.ok("注册成功");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败，系统异常");
        }
    }

    /**
     * 登录逻辑（含格式校验）
     */
    public ResponseEntity<?> login(String username, String password) {
        String validationMsg = validateInput(username, password);
        if (validationMsg != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationMsg);
        }

        User user = usermapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {

            String role = user.getRole(); // 例如 "admin-part"、"auditor"、"user"
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authToken =new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authToken);


//            UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(user.getUsername(), null,
//                            Collections.singletonList(new SimpleGrantedAuthority("USER")));//将用户信息写入session
//            SecurityContextHolder.getContext().setAuthentication(authToken);
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
    }

    public int deleteUserByUsername(String username) {
        return usermapper.deleteByUsername(username);
    }


    /**
     * 输入参数校验
     */
    private String validateInput(String username, String password) {
        if (username == null || username.length() == 0 || username.length() > 10) {
            return "用户名长度需为 1~10 位";
        }

        if (password == null || password.length() == 0 || password.length() > 20) {
            return "密码长度需为 1~20 位";
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$")) {
            return "密码需包含字母和数字组合";
        }



        return null;
    }

    /**
     * 邮箱验证码对象
     */
    static class EmailCodeEntry {
        private final String code;
        private final long expireAt;

        public EmailCodeEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}

