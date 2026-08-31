package com.litego.utils;

import java.util.Random;

/**
 * 验证码生成工具类
 * 用于生成指定位数的纯数字验证码
 */
public class ValidateCodeutil {

    // 私有化构造方法，避免工具类被实例化
    private ValidateCodeutil() {
    }

    /**
     * 生成指定长度的纯数字验证码
     * @param length 验证码位数
     * @return 纯数字验证码
     */
    public static Integer generateValidateCode(int length) {
        // 校验长度合法性
        if (length <= 0 || length > 10) { // 验证码最多10位
            throw new IllegalArgumentException("验证码位数必须是1-10之间的正整数");
        }

        // 计算位数对应的范围：比如5位是 10000 ~ 99999，避免生成以0开头的验证码
        int min = (int) Math.pow(10, length - 1); // 5位的话min=10^4=10000
        int max = (int) Math.pow(10, length) - 1; // 5位的话max=10^5-1=99999

        // 生成指定范围内的随机整数
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    // 一个默认生成5位验证码的重载方法
    public static Integer generateValidateCode() {
        return generateValidateCode(5);
    }
}