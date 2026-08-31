package com.litego.config;

import com.litego.interceptor.LoginInterceptor;
import com.litego.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final JwtTool jwtTool;
    private final AuthProperties authProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(jwtTool))
                .addPathPatterns("/**")          // 需要拦截的路径
                .excludePathPatterns(authProperties.getExcludePaths());    // 放行的公开路径
    }
}
