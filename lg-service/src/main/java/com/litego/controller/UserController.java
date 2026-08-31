package com.litego.controller;

import cn.hutool.core.util.StrUtil;
import com.litego.common.domain.R;
import com.litego.common.exception.BadRequestException;
import com.litego.domain.dto.LoginFormDTO;
import com.litego.domain.po.User;
import com.litego.domain.vo.UserLoginVO;
import com.litego.service.IUserService;
import com.litego.utils.EmailUtils;
import com.litego.utils.ValidateCodeutil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;  // 注意这里

import java.io.UnsupportedEncodingException;

@Tag(name = "用户相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;
    private final EmailUtils emailUtils;

    //todo 待实现
    @Operation(summary = "用户登录接口（密码）")
    @PostMapping("/login/pwd")
    public UserLoginVO loginByPwd(@RequestBody @Validated(LoginFormDTO.PasswordLoginGroup.class) LoginFormDTO loginFormDTO) {
        return userService.loginByPwd(loginFormDTO);
    }

    @Operation(summary = "用户登录接口（邮箱）")
    @PostMapping("/login/email")
    public R<UserLoginVO> loginByEmail(@RequestBody @Validated(LoginFormDTO.CodeLoginGroup.class) LoginFormDTO loginFormDTO, HttpSession session) {
        try {
            UserLoginVO vo = userService.loginByEmail(loginFormDTO, session);
            return R.ok(vo);
        } catch (BadRequestException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error("登录失败：" + e.getMessage());
        }
    }


    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/sendEmailMsg")
    public R<String> sendEmailMsg(@RequestBody User user, HttpSession session) {
        log.info("===进入发送邮箱接口===");
        String email = user.getEmail();

        if (StrUtil.isBlank(email)) {
            return R.error("邮箱不能为空");
        }

        boolean isRegistered = userService.checkEmailRegistered(email);
        String code = ValidateCodeutil.generateValidateCode(4).toString();
        log.info("邮箱{}的验证码：{}（是否已注册：{}）", email, code, isRegistered);

        // 1. 先存储验证码到 Session（确保即使邮件发送失败也能重试）
        String sessionKey = "EMAIL_CODE_" + email;
        session.setAttribute(sessionKey, code);
        session.setAttribute(sessionKey + "_EXPIRE", System.currentTimeMillis() + 5 * 60 * 1000);

        // 2. 异步发送邮件（不等待结果）
        emailUtils.sendEmail(email, code);

        // 3. 返回成功
        if (isRegistered) {
            return R.ok("验证码已发送（邮箱已注册）");
        } else {
            return R.ok("new_email");
        }
    }
}