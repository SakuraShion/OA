package com.example.emos.workflow.strategy;

import cn.hutool.json.JSONUtil;
import com.example.emos.workflow.common.util.Constants;
import com.example.emos.workflow.config.quartz.QuartzUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Service
@Slf4j
public class DeleteMeetingProcessService implements IWorkflowService{
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private QuartzUtil quartzUtil;

    @Override
    public void service(String parameter) {
        HashMap map = JSONUtil.parse(parameter).toBean(HashMap.class);
        String instanceId = (String) map.get("instanceId");
        String type = (String) map.get("reason");
        String uuid = (String) map.get("uuid");

        long count = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).count();
        if (count > 0) {
            runtimeService.deleteProcessInstance(instanceId, (String) map.get("reason"));
        }
        count = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).count();
        if (count > 0) {
            historyService.deleteHistoricProcessInstance(instanceId);
        }
        if (type.equals("会议申请")) {
            quartzUtil.deleteJob(uuid, "会议开始任务组");
            quartzUtil.deleteJob(uuid, "会议结束任务组");
            quartzUtil.deleteJob(uuid, "会议工作流组");
            quartzUtil.deleteJob(uuid, "创建会议室ID任务组");
        }
    }

    @PostConstruct
    public void init() {
        WorkflowStrategyFactory.register(Constants.TARGET_DELETE_PROCESS_BY_ID, this);
    }
}
