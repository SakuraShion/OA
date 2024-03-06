package com.example.emos.api.common.util;

import cn.hutool.core.map.MapUtil;
import com.ctc.wstx.shaded.msv_core.verifier.jarv.Const;
import com.example.emos.api.db.dao.TbUserDao;
import com.example.emos.api.db.pojo.MailInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created By zf
 * 描述:
 */
@Slf4j
@Component
public class MailSendUtils {

    @Autowired
    private TbUserDao userDao;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.name}")
    private String name;

    public void sendEmail(String type, int id) {
        String mailTitle = null;
        String mailContent = null;

        HashMap map = userDao.searchEmailAndName(id);

        if (type.equals(MailConst.MailType.USER.getType())) {
            mailTitle = MailConst.MailTitle.INSERT_USER;
            mailContent = MailConst.MailContent.INSERT_USER;
        }


        try {
            String sex = MapUtil.getStr(map, "sex").equals("男") ? "先生" : "女生";
            MailInfo mailInfo = new MailInfo(
                    username,
                    password,
                    MapUtil.getStr(map, "email"),
                    name,
                    MapUtil.getStr(map, "name"),
                    mailTitle,
                    String.format(mailContent, MapUtil.getStr(map, "name"),
                            sex,
                            MapUtil.getStr(map, "dept"),
                            MapUtil.getStr(map, "username"),
                            MapUtil.getStr(map, "password")
                    )) ;
            MailSendUtils.sendEmail(mailInfo);

        } catch (Exception e) {
            log.error("邮件发送异常", e);
        }
    }

    private static void sendEmail(MailInfo mailInfo) throws Exception {

        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", "smtp.aliyun.com");   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.port", "465");

        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getInstance(props);
        session.setDebug(true);                                 // 设置为debug模式, 可以查看详细的发送 log

        // 3. 创建一封邮件
        MimeMessage message = MailSendUtils.createMimeMessage(session, mailInfo);

        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        transport.connect(mailInfo.getSendEmailAccount(), mailInfo.getSendEmailPassword());

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }


    private static MimeMessage createMimeMessage(Session session, MailInfo mailInfo) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(mailInfo.getSendEmailAccount(), mailInfo.getSendPersonName(), "UTF-8"));

        // 3. To: 收件人
        message.setRecipient(MimeMessage.RecipientType.TO,
                new InternetAddress(mailInfo.getReceiveMailAccount(), mailInfo.getReceivePersonName(), "UTF-8"));

        // 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
        message.setSubject(mailInfo.getMailTitle(), "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
        message.setContent(mailInfo.getMailContent(), "text/html;charset=UTF-8");

        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }
}
