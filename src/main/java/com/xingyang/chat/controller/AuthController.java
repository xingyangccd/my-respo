package com.xingyang.chat.controller;

import com.xingyang.chat.model.dto.LoginDTO;
import com.xingyang.chat.model.dto.RegisterDTO;
import com.xingyang.chat.model.vo.Result;
import com.xingyang.chat.model.vo.UserVO;
import com.xingyang.chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Authentication Controller
 *
 * @author XingYang
 */
@Slf4j
@Tag(name = "Authentication API", description = "Authentication endpoints")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "User Login", description = "Login with username and password")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        log.info("User login: {}", loginDTO.getUsername());
        Map<String, Object> result = userService.login(loginDTO);
        return Result.success("Login successful", result);
    }

    @Operation(summary = "User Register", description = "Register a new account")
    @PostMapping("/register")
    public Result<UserVO> register(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("User registration: {}", registerDTO.getUsername());
        UserVO user = userService.register(registerDTO);
        return Result.success("Registration successful", user);
    }

    @Operation(summary = "Current User", description = "Get current user information")
    @GetMapping("/user")
    public Result<UserVO> getCurrentUser() {
        log.info("Get current user information");
        UserVO user = userService.getCurrentUser();
        return Result.success(user);
    }
} 