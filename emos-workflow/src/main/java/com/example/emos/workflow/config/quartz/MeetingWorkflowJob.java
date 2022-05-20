package com.example.emos.workflow.config.quartz;


import com.example.emos.workflow.service.MeetingService;
import com.example.emos.workflow.service.WorkflowService;
import com.example.emos.workflow.task.MailTask;
import com.example.emos.workflow.util.MailConst;
import com.example.emos.workflow.util.MailSendUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 检查工作流的会议审批任务
 */
@Slf4j
@Component
public class MeetingWorkflowJob extends QuartzJobBean {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private MailTask mailTask;


    /**
     * 检查工作流的审批状态
     * @param ctx
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        Map map = ctx.getJobDetail().getJobDataMap();
        String uuid = map.get("uuid").toString();
        String instanceId = map.get("instanceId").toString();

        // 判断会议审批是否未结束，当到了会议开始时间会议实例仍存在，就意味着会议审批未通过，通过了会删除工作流实例
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (instance != null) {
            map.put("processStatus", "未结束");
            // 删除对应的工作流实例
            workflowService.deleteProcessById(uuid, instanceId, "会议", "会议过期");
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("status", 2); // 拒绝
            // 修改会议状态为审批未通过
            meetingService.updateMeetingStatus(param);
            mailTask.sendMail(MailConst.MailType.MEETING.getType(), uuid, "拒绝");
            log.debug("会议已失效");
        }
    }
}

