package com.example.emos.workflow.task;


import com.example.emos.workflow.util.MailConst;
import com.example.emos.workflow.util.MailSendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
    public void sendMail(String type, String id, String result) {
        mailSendUtils.sendEmail(type, id, result);
    }
}
