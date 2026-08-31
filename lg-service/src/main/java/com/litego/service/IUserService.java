package com.litego.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.litego.domain.dto.LoginFormDTO;
import com.litego.domain.po.User;
import com.litego.domain.vo.UserLoginVO;
import jakarta.servlet.http.HttpSession;

//import javax.servlet.http.HttpSession;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author 阿怪
 * @since 2023-05-05
 */
public interface IUserService extends IService<User> {

    void register(User user);

    UserLoginVO loginByEmail(LoginFormDTO loginFormDTO, HttpSession session);

    UserLoginVO loginByPwd(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);

    boolean checkEmailRegistered(String email);
}
