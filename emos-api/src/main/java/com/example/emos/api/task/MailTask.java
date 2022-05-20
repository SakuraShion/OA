package com.example.emos.api.task;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.MailConst;
import com.example.emos.api.common.util.MailSendUtils;
import com.example.emos.api.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Slf4j
@Component
public class MailTask {
    @Autowired
    private MailSendUtils mailSendUtils;

    @Async("AsyncTaskExecutor")
    public void sendMail(int id) {
        mailSendUtils.sendEmail(MailConst.MailType.USER.getType(), id);
    }
}
