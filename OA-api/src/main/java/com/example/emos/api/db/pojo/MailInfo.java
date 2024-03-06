package com.example.emos.api.db.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created By zf
 * 描述:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailInfo {
    private final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    // 发件人账户
    private String sendEmailAccount;

    // 发件人密码
    private String sendEmailPassword;

    // 收件人账户
    private String receiveMailAccount;

    // 发送人姓名
    private String sendPersonName;

    // 收件人姓名
    private String receivePersonName;

    // 邮件标题
    private String mailTitle;

    // 邮件正文
    private String mailContent;
}
