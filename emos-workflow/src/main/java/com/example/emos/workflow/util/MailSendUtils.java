package com.example.emos.workflow.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.example.emos.workflow.db.dao.TbLeaveDao;
import com.example.emos.workflow.db.dao.TbMeetingDao;
import com.example.emos.workflow.db.dao.TbReimDao;
import com.example.emos.workflow.db.dao.TbUserDao;
import com.example.emos.workflow.db.pojo.MailInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private TbReimDao reimDao;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.name}")
    private String name;

    public void sendEmail(String type, String id, String result) {
        String mailTitle = null;
        String mailContent = null;

        HashMap map = null;
        // 会议
        if (type.equals(MailConst.MailType.MEETING.getType())) {
            map = meetingDao.searchStartAndEndByUserId(id);

            String date = MapUtil.getStr(map, "date");
            String start = MapUtil.getStr(map, "start");
            String end = MapUtil.getStr(map, "end");
            map.put("start", DateUtil.parse(date + " " + start));
            map.put("end", DateUtil.parse(date + " " + end));

            mailTitle = MailConst.MailTitle.MEETING;
            mailContent = "同意".equals(result) ? MailConst.MailContent.MEETING_S : MailConst.MailContent.MEETING_E;
        }

        // 报销
        else if (type.equals(MailConst.MailType.REIM.getType())) {
            map = reimDao.searchStartInfo(id);
            map.put("start", map.get("create_time"));
            map.put("end", "所");
            mailTitle = MailConst.MailTitle.REIM;
            mailContent = "同意".equals(result) ? MailConst.MailContent.REIM_S : MailConst.MailContent.REIM_E;
        }

        // 请假
        else if (type.equals(MailConst.MailType.LEAVE.getType())) {
            map = leaveDao.searchStartAndEndById(id);
            mailTitle = MailConst.MailTitle.LEAVE;
            mailContent = "同意".equals(result) ? MailConst.MailContent.LEAVE_S : MailConst.MailContent.LEAVE_E;
        }


        try {
            MailInfo mailInfo = new MailInfo(
                    username, password,
                    MapUtil.getStr(map, "email"),
                    name,
                    MapUtil.getStr(map, "name"),
                    mailTitle,
                    String.format(mailContent, MapUtil.getStr(map, "name"),
                            MapUtil.getStr(map, "start"),
                            MapUtil.getStr(map, "end")
                    )) ;
            MailSendUtils.sendEmail(mailInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendEmail(MailInfo mailInfo) throws Exception {

        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", "smtp.aliyun.com");   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证

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
