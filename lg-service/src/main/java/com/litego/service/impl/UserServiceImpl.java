package com.litego.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litego.common.exception.BadRequestException;
import com.litego.common.exception.BizIllegalException;
import com.litego.common.exception.ForbiddenException;
import com.litego.common.utils.UserContext;
import com.litego.config.JwtProperties;
import com.litego.domain.po.User;
import com.litego.mapper.UserMapper;
import com.litego.service.IUserService;
import com.litego.utils.JwtTool;
import com.litego.domain.dto.LoginFormDTO;
import com.litego.domain.vo.UserLoginVO;
import com.litego.enums.UserStatus;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

//import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    @Override
    public UserLoginVO loginByEmail(LoginFormDTO loginDTO, HttpSession session) {
        String email = loginDTO.getEmail();
        String inputCode = loginDTO.getCode();
        String sessionKey = "EMAIL_CODE_" + email;

        // 1. 从Session获取保存的验证码和过期时间
        String savedCode = (String) session.getAttribute(sessionKey);
        Long expireTime = (Long) session.getAttribute(sessionKey + "_EXPIRE");

        // 2. 校验验证码
        if (!StringUtils.hasText(savedCode)) {
            throw new BadRequestException("请先发送验证码");
        }
        if (System.currentTimeMillis() > expireTime) {
            throw new BadRequestException("验证码已过期，请重新发送");
        }
        if (!savedCode.equals(inputCode)) {
            throw new BadRequestException("验证码错误");
        }

        // 3. 判断邮箱是否已注册
        User user = getOne(new QueryWrapper<User>().eq("email", email));
        if (user == null) {
            // 4. 未注册：自动创建用户（前端已确认注册）
            user = createUserByEmail(email);
            log.info("邮箱{}未注册，已自动完成注册，用户ID：{}", email, user.getId());
        } else {
            // 5. 已注册：校验用户状态
            if (user.getStatus() == UserStatus.FROZEN) {
                throw new ForbiddenException("用户被冻结，无法登录");
            }
        }

        // 6. 生成TOKEN（登录/注册后统一返回）
        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        // 7. 清除Session中的验证码（避免重复使用）
        session.removeAttribute(sessionKey);
        session.removeAttribute(sessionKey + "_EXPIRE");

        // 8. 封装VO返回
        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername()); // 新用户用户名默认用邮箱前缀
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        return vo;
    }

    // 私有方法：根据邮箱创建新用户
    private User createUserByEmail(String email) {
        User user = new User();
        // 用户名：取邮箱@前的部分，避免重复加随机数
        String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 6);
        user.setUsername(username);
        // 密码：默认加密（可让用户后续修改，这里用随机密码）
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString().substring(0, 8)));
        // 邮箱
        user.setEmail(email);
        // 基础配置
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(UserStatus.NORMAL); // 正常状态
        user.setBalance(0); // 初始余额0
        // 保存用户
        save(user);
        return user;
    }

    // 新增：判断邮箱是否已注册
    @Override
    public boolean checkEmailRegistered(String email) {
        return count(new QueryWrapper<User>().eq("email", email)) > 0;
    }

    @Override
    public UserLoginVO loginByPwd(LoginFormDTO loginDTO) {
        // 1.数据校验
        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        // 2.根据用户名或手机号查询
        User user = getOne(new QueryWrapper<User>().eq("username", email));
        Assert.notNull(user, "用户名错误");
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4.校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 5.生成TOKEN
        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        // 6.封装VO返回
        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        return vo;
    }

    @Override
    public void register(User user) {
        // 1.数据校验
        String username = user.getUsername();
        String email = user.getEmail();
        if (getOne(new QueryWrapper<User>().eq("email", email)) != null) {
            throw new BadRequestException("邮箱已存在");
        }
    }

    @Override
    public void deductMoney(String pw, Integer totalFee) {
        log.info("开始扣款");
        // 1.校验密码
        User user = getById(UserContext.getUser());
        if(user == null || !passwordEncoder.matches(pw, user.getPassword())){
            // 密码错误
            throw new BizIllegalException("用户密码错误");
        }

        // 2.尝试扣款
        try {
            baseMapper.updateMoney(UserContext.getUser(), totalFee);
        } catch (Exception e) {
            throw new RuntimeException("扣款失败，可能是余额不足！", e);
        }
        log.info("扣款成功");
    }
}
