package com.example.emos.workflow.common.util;

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
    /**
     * 验证码过期提示
     */
    public static final String CAPTCHA_EXPIRE_MSG = "验证码已失效";
    /**
     * 验证码错误提示
     */
    public static final String CAPTCHA_ERROR = "验证码错误";


    /**
     * 目标业务方法
     */
    public static final String RABBIT_TARGET = "rabbit:properties:target";
    /**
     * 生产者重发次数
     */
    public static final String RABBIT_PRODUCER_RETRY_NUM = "rabbit:properties:producer_retry_num";
    /**
     * 消费者重发次数
     */
    public static final String RABBIT_CONSUMER_RETRY_NUM = "rabbit:properties:consumer_retry_num";

    /**
     * 消费端幂等，拼接 key
     */
    public static final String RABBIT_CONSUMER_MESSAGE_ID = "rabbit:consumer:message_id:";
    /**
     * 生产者可靠性投递，拼接 key
     */
    public static final String RABBIT_PRODUCER_MESSAGE_ID = "rabbit:producer:message_id:";


    // 开启会议流程实例
    public static final String TARGET_START_MEETING_PROCESS = "startMeetingProcess";

    // 删除会议流程实例
    public static final String TARGET_DELETE_PROCESS_BY_ID = "deleteProcessById";

    // 开启报销流程实例
    public static final String TARGET_START_REIM_PROCESS = "startReimProcess";

    // 开启请假流程
    public static final String TARGET_START_LEAVE_PROCESS = "startLeaveProcess";
}
