
package com.tian.tianjava.controller;
import com.tian.tianjava.entity.RegisterUser;
import com.tian.tianjava.entity.ResetPasswordDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tian.tianjava.service.Userservice;
import org.springframework.web.bind.annotation.*;
import java.util.Map;



@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private Userservice userservice;

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        return userservice.resetPassword(dto.getEmail(), dto.getNewPassword(), dto.getEmailCode());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate(); // ✅ 清除 session
        SecurityContextHolder.clearContext(); // ✅ 清空认证上下文
        return ResponseEntity.ok("注销成功");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null) {
            return ResponseEntity.badRequest().body("缺少用户名");
        }
        int rows = userservice.deleteUserByUsername(username);
        return rows > 0 ? ResponseEntity.ok("账号已删除") : ResponseEntity.status(404).body("用户不存在");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> userMap) {
        String username = userMap.get("username");
        String password = userMap.get("password");
        return userservice.login(username, password);
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUser dto) {
        System.out.println("收到注册请求: " + dto.getEmail());

        // 如果验证码存在，则视为注册提交
        return userservice.register(dto.getUsername(), dto.getPassword(), dto.getEmail(), dto.getEmailCode());
    }

    @PostMapping("/sendCode")
    public ResponseEntity<?> sendEmailCode(@RequestBody RegisterUser dto) {
        System.out.println("收到邮箱验证码请求：" + dto.getEmail());
        return userservice.sendEmailCode(dto.getEmail());
    }

}
