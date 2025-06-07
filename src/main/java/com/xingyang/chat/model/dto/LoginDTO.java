package com.xingyang.chat.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Login DTO
 *
 * @author XingYang
 */
@Data
@Schema(description = "Login Request")
public class LoginDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Schema(description = "Username", required = true)
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Schema(description = "Password", required = true)
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Schema(description = "Remember me")
    private Boolean rememberMe = false;
} 