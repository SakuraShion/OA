package com.example.emos.workflow.bpmn;

import cn.hutool.core.map.MapUtil;
import com.example.emos.workflow.db.dao.TbLeaveDao;
import com.example.emos.workflow.db.dao.TbUserDao;
import com.example.emos.workflow.exception.EmosException;

import com.example.emos.workflow.task.MailTask;
import com.example.emos.workflow.util.MailConst;
import com.example.emos.workflow.util.MailSendUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class NotifyLeaveService implements JavaDelegate {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private MailTask mailTask;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                .includeTaskLocalVariables().processInstanceId(delegateExecution.getProcessInstanceId())
                .orderByHistoricTaskInstanceEndTime().orderByTaskCreateTime().asc().list().get(0);
        String result = taskInstance.getTaskLocalVariables().get("result").toString();
        delegateExecution.setVariable("result", result);
        String instanceId = delegateExecution.getProcessInstanceId();
        
        HashMap param = new HashMap() {{
            put("status", "同意".equals(result) ? 3 : 2);
            put("instanceId", instanceId);
        }};

        int rows = leaveDao.updateLeaveStatus(param);
        if (rows != 1) {
            throw new EmosException("更新请假记录状态失败");
        }

        mailTask.sendMail(MailConst.MailType.LEAVE.getType(), instanceId, result);
        delegateExecution.setVariable("filing", true);
    }
}
