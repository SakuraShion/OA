package com.example.emos.workflow.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.workflow.common.util.Constants;
import com.example.emos.workflow.config.quartz.MeetingWorkflowJob;
import com.example.emos.workflow.config.quartz.QuartzUtil;
import com.example.emos.workflow.db.dao.TbMeetingDao;
import com.example.emos.workflow.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By zf
 * 描述:
 */
@Service
@Slf4j
public class StartMeetingProcessService implements IWorkflowService{
    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private QuartzUtil quartzUtil;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void service(String parameter) {
        HashMap param = JSONUtil.parse(parameter).toBean(HashMap.class);

        param.put("filing", false);
        param.put("type", "会议申请");
        param.put("createDate", DateUtil.today());

        param.remove("code");
        if (param.get("creatorId") == null) {

            param.put("identity", "总经理");
            param.put("result", "同意");

        } else {
            param.put("identity", "员工");
        }

        String instanceId = runtimeService.startProcessInstanceByKey("meeting", param).getProcessInstanceId();
        String uuid = param.get("uuid").toString();
        String date = param.get("date").toString();
        String start = param.get("start").toString();

        // 创建会议工作流任务组
        JobDetail jobDetail = JobBuilder.newJob(MeetingWorkflowJob.class).build();
        Map dataMap = jobDetail.getJobDataMap();
        dataMap.put("uuid", uuid);
        dataMap.put("instanceId", instanceId);

        Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm:ss");
        quartzUtil.addJob(jobDetail, uuid, "会议工作流组", executeDate);

        if (StringUtils.isNotBlank(instanceId)) {

            param.clear();
            param.put("uuid", uuid);
            param.put("instanceId", instanceId);

            int row = meetingDao.updateMeetingInstanceId(param);
            if (row != 1) {
                throw new EmosException("保存会议工作流id 失败");
            }
        } else {
            log.error("instanceId 为空，instanceId：" + instanceId);
        }
    }

    @PostConstruct
    public void init() {
        WorkflowStrategyFactory.register(Constants.TARGET_START_MEETING_PROCESS, this);
    }
}
