package com.example.emos.workflow.bpmn;

import cn.hutool.core.map.MapUtil;
import com.example.emos.workflow.db.dao.TbReimDao;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component("notifyReimService")
public class NotifyReimService implements JavaDelegate {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbReimDao reimDao;

    @Autowired
    private MailTask mailTask;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                .includeTaskLocalVariables().processInstanceId(delegateExecution.getProcessInstanceId())
                .orderByHistoricTaskInstanceEndTime().asc().list().get(0);
        String result = taskInstance.getTaskLocalVariables().get("result").toString();
        delegateExecution.setVariable("result", result);
        String instanceId = delegateExecution.getProcessInstanceId();
        HashMap param = new HashMap() {{
            put("status", "同意".equals(result) ? 3 : 2);
            put("instanceId", instanceId);
        }};

        int rows = reimDao.updateReimStatus(param);
        if (rows != 1) {
            throw new EmosException("更新报销记录状态失败");
        }

        mailTask.sendMail(MailConst.MailType.REIM.getType(), instanceId, result);
        delegateExecution.setVariable("filing", true);
    }
}
