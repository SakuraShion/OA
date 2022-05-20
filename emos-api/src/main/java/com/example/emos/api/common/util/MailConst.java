package com.example.emos.api.common.util;

/**
 * Created By zf
 * 描述:
 */
public class MailConst {

    public interface MailTitle {
        String INSERT_USER = "入职通知";
    }

    public interface MailContent {
        String INSERT_USER = "%s%s，您好！您将成为本公司%s的一员，我们对您加入华夏科技大家庭表示热烈欢迎！" +
                "<br/>" +
                "此外，您在本公司 OA 系统中账号信息如下：" +
                "<br/>" +
                "<b>用户名：</b>%s\n" +
                "<br/>" +
                "<b>密 码：</b>%s\n" +
                "<br/>" +
                "<b>官网地址为：</b>www.zhoustudy.com/emos-vue";
    }

    public enum MailType {
        USER("user");

        MailType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        private String type;
    }
}
