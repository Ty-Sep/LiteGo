package com.litego.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

import org.springframework.scheduling.annotation.Async;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtils {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailTaskExecutor")   // 异步执行
    public void sendEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "轻途健康助手");
            helper.setTo(toEmail);
            helper.setSubject("LiteGo验证码");
            String content = "你的验证码是：" + code + "，用于轻途健康助手登录验证，5分钟内有效，请及时验证！";
            helper.setText(content, false);
            mailSender.send(message);
            log.info("验证码邮件已发送至：{}", toEmail);
        } catch (Exception e) {
            log.error("邮件发送失败 to: {}, code: {}", toEmail, code, e);
        }
    }
}