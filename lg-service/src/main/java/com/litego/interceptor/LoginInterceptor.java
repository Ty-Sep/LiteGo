package com.litego.interceptor;

import cn.hutool.core.util.StrUtil;
import com.litego.common.exception.UnauthorizedException;
import com.litego.common.utils.UserContext;
import com.litego.utils.JwtTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtTool jwtTool;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的 token（注意头名称大小写不敏感，但通常用 "Authorization"）
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            // 没有 token 或格式错误 → 未登录
            throw new UnauthorizedException("用户未登录");
        }

        // 2. 提取 token
        String token = authHeader.substring(7);

        // 3. 校验 token
        try {
            Long userId = jwtTool.parseToken(token);
            // 如果 parseToken 返回 null 表示无效（取决于你的实现），也需处理
            if (userId == null) {
                throw new UnauthorizedException("token不合规");
            }
            // 存入上下文
            UserContext.setUser(userId);
        } catch (Exception e) {
            // 解析过程抛出异常（如过期、签名错误）
            throw new UnauthorizedException("token不合规");
        }

        // 4. 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户
        UserContext.removeUser();
    }
}