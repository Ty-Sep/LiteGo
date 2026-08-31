package com.litego.domain.dto;

//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//import javax.validation.constraints.Email;
//import javax.validation.constraints.NotNull;

@Data
@Schema(description = "登录表单实体")
public class LoginFormDTO {
    // 定义分组标记接口
    public interface PasswordLoginGroup {}
    public interface CodeLoginGroup {}

    @NotNull(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Schema(description = "邮箱", required = true)
    private String email;

    // 仅验证码登录必填
    @NotNull(message = "验证码不能为空", groups = CodeLoginGroup.class)
    @Schema(description = "验证码（仅验证码登录时必填）", required = true)
    private String code;

    // 仅密码登录必填
    @NotNull(message = "密码不能为空", groups = PasswordLoginGroup.class)
    @Schema(description = "验证码（仅密码登录时必填）", required = true)
    private String password;
}