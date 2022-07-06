package com.example.emos.api.common.util;

/**
 * Created By zf
 * 描述:  常量表
 */
public class Constants {

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    public static final String CAPTCHA_EXPIRE_MSG = "验证码已失效";
    public static final String CAPTCHA_ERROR = "验证码错误";


}
