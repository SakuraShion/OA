package com.example.emos.workflow.util;

/**
 * Created By zf
 * 描述:
 */
public class MailConst {

    public interface MailTitle {
        String INSERT_USER = "入职通知";
        String MEETING = "会议申请通知";
        String REIM = "报销通知";
        String LEAVE = "请假通知";
    }

    public interface MailContent {
        String INSERT_USER = "%s%s，您好！您将c成为本公司%s的一员，我们对您加入华夏科技大家庭表示热烈欢迎！" +
                "<br/>" +
                "此外，您在本公司 OA 系统中账号信息如下：" +
                "<br/>" +
                "<b>用户名：</b>%s\n" +
                "<br/>" +
                "<b>密 码：</b>%s\n" +
                "<br/>" +
                "<b>官网地址为：</b>www.zhoustudy.com/emos-vue";

        String MEETING_S = "员工%s，您于%s至%s的会议申请已经被批准，详情可登录官网进行查询！";

        String MEETING_E = "员工%s，您于%s至%s的会议申请已经被拒绝，详情可登录官网进行查询！";

        String REIM_S = "员工%s，您于%s%s提交的报销申请已经被批准，请及时把报销单签字交给HR归档！";

        String REIM_E = "员工%s，您于%s%s提交的报销申请已经被拒绝，详情可登录官网进行查询！";

        String LEAVE_S = "员工%s，您于%s至%s的请假申请已经被批准，请及时把请假单签字交给HR归档！";

        String LEAVE_E = "员工%s，您于%s至%s的请假申请已经被拒绝，详情可登录官网进行查询！";
    }

    public enum MailType {
        USER("user"),
        MEETING("meeting"),
        REIM("reim"),
        LEAVE("leave");

        MailType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        private String type;
    }
}
