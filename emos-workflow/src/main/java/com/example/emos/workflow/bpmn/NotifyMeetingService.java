package com.example.emos.workflow.bpmn;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.workflow.config.quartz.MeetingRoomJob;
import com.example.emos.workflow.config.quartz.MeetingStatusJob;
import com.example.emos.workflow.config.quartz.QuartzUtil;
import com.example.emos.workflow.db.pojo.MessageEntity;
import com.example.emos.workflow.service.MeetingService;
import com.example.emos.workflow.service.MessageService;
import com.example.emos.workflow.task.MailTask;
import com.example.emos.workflow.task.MessageTask;
import com.example.emos.workflow.util.MailConst;
import com.example.emos.workflow.util.MailSendUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NotifyMeetingService implements JavaDelegate {
    @Autowired
    private QuartzUtil quartzUtil;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MailTask mailTask;

    @Autowired
    private MessageTask messageTask;

    @Autowired
    private MessageService messageService;

    @SuppressWarnings("all")
    @Override
    public void execute(DelegateExecution delegateExecution) {
        Map map = delegateExecution.getVariables();
        String uuid = MapUtil.getStr(map, "uuid");
        String url = MapUtil.getStr(map, "url");
        String result = MapUtil.getStr(map, "result");

        HashMap data = meetingService.searchMeetingByUUID(uuid);
        String title = MapUtil.getStr(data, "title");
        String date = MapUtil.getStr(data, "date");
        String start = MapUtil.getStr(data, "start");
        String end = MapUtil.getStr(data, "end");
        if (result.equals("同意")) {
            meetingService.updateMeetingStatus(new HashMap() {{
                put("uuid", uuid);
                put("status", 3);
            }});



            String meetingType = delegateExecution.getVariable("meetingType", String.class);
            if (meetingType.equals("线上会议")) {
                // 创建会议室ID任务组，当会议开始前15 分钟生成 roomId 存储在 redis 中
                JobDetail jobDetail = JobBuilder.newJob(MeetingRoomJob.class).build();
                Map param = jobDetail.getJobDataMap();
                param.put("uuid", uuid);
                Date expire = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
                param.put("expire", expire);
                Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm").offset(DateField.MINUTE, -15);
                quartzUtil.addJob(jobDetail, uuid, "创建会议室ID任务组", executeDate);

                // 创建会议开始任务组，会议开始前13分钟，修改会议状态，设置为会议进行中，此时可进入会议
                jobDetail = JobBuilder.newJob(MeetingStatusJob.class).build();
                param = jobDetail.getJobDataMap();
                param.put("uuid", uuid);
                param.put("status", 4);
                param.put("title",title);
                param.put("date",date);
                param.put("end", end);
                param.put("start", start);
                param.put("flag", "start");

                executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm").offset(DateField.MINUTE, -13);
                quartzUtil.addJob(jobDetail, uuid, "会议开始任务组", executeDate);

                // 创建会议结束任务组，当会议结束时将会议状态修改为结束，并将未签到的会议人员添加到缺勤名单中，并设置罚款
                jobDetail = JobBuilder.newJob(MeetingStatusJob.class).build();
                param = jobDetail.getJobDataMap();
                param.put("uuid", uuid);
                param.put("status", 5);
                param.put("title",title);
                param.put("date",date);
                param.put("end", end);
                param.put("start", start);
                param.put("flag", "end");

                executeDate = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
                quartzUtil.addJob(jobDetail, uuid, "会议结束任务组", executeDate);

                // 发送消息
                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统消息");
                entity.setMsg("您有一场新的会议，可在会议管理中进行查看！");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setSendTime(new Date());
                messageService.insertMessage(entity);


                ArrayList<Integer> list = meetingService.searchMeetingMembers(uuid);
                for (Integer userId : list) {
                    messageTask.sendAsync(userId + "", entity);
                }
            }
            // 此时会议申请已通过，所以会议工作流组不再需要，进行删除
            quartzUtil.deleteJob(uuid, "会议工作流组");            
        }

        mailTask.sendMail(MailConst.MailType.MEETING.getType(), uuid, result);

        JSONObject json = new JSONObject();
        String processId = delegateExecution.getProcessInstanceId();
        json.set("processId", processId);
        
    }
}
