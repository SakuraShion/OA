package com.example.emos.api.task;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.Constants;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.meetingform.RecieveNotifyForm;
import com.example.emos.api.db.dao.TbMeetingDao;
import com.example.emos.api.db.dao.TbUserDao;
import com.example.emos.api.db.pojo.MessageEntity;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.task.rabbit.RabbitSender;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:      线程任务类，使用线程池异步调用工作流接口
 */
@Component
@Slf4j

public class MeetingWorkflowTask {
    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private RabbitSender rabbitSender;


    /**
     * @Async("AsyncTaskExecutor"): 当执行此方法时会开启一个新的线程去异步执行此操作，调用 AsyncTaskExecutor 自定义线程池去开启线程
     * @param uuid 会议审批通过后，会根据 uuid 创建定时器，用于分组，确定定时器和哪个会议相关联
     * @param creatorId 申请会议人id，以此获取申请人信息
     * @param title 会议标题
     * @param date 日期
     * @param start 会议开始时间
     * @param meetingType 会议类型
     */
    @Async("AsyncTaskExecutor")
    public void startMeetingWorkflow(String uuid, int creatorId, String title, String date, String start, String meetingType){
        HashMap info = userDao.searchUserInfo(creatorId);

        // 保存要提交的数据
        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("creatorId",creatorId);
        json.set("creatorName",info.get("name").toString());
        json.set("title",title);
        json.set("date", date);
        json.set("start", start);
        json.set("meetingType",meetingType);

        // 将申请人信息中的 角色信息转换成字符串，然后用 "," 进行分割，获得其角色信息，判断是否包含总经理，总经理不需要他人审批
        String[] roles = info.get("roles").toString().split(",");
        // 不包括总经理角色就需要部门经理和总经理审批
        if (!ArrayUtil.contains(roles, "总经理")) {
            // 根据申请人ID 获取到部门经理 ID ，并获取总经理 ID
            Integer managerId = userDao.searchDeptManagerId(creatorId);
            Integer gmId = userDao.searchGmId();
            json.set("managerId", managerId);
            json.set("gmId", gmId);

            // 查询会议人员是否是同一部门的
            boolean bool = meetingDao.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept", bool);
        }

        String param = JSONUtil.toJsonStr(json);

        try {
            rabbitSender.send(param, Constants.TARGET_START_MEETING_PROCESS);
        } catch (Exception e) {
            log.error("生产者发送异常", e);
        }

        // 响应成功，对会议进行修改，修改流程实例 id
//        if (resp.getStatus() == 200) {
//            json = JSONUtil.parseObj(resp.body());
//            String instanceId = json.getStr("instanceId");
//
//            HashMap param = new HashMap();
//            param.put("uuid", uuid);
//            param.put("instanceId", instanceId);
//
//            int row = meetingDao.updateMeetingInstanceId(param);
//            if (row != 1) {
//                throw new EmosException("保存会议工作流id 失败");
//            }
//        } else {
//            log.error(resp.body());
//        }
    }

    @Async("AsyncTaskExecutor")
    public void deleteMeetingApplication(String uuid, String instanceId, String reason) {
        JSONObject json = new JSONObject();
        json.set("uuid", uuid);
        json.set("instanceId", instanceId);
        json.set("reason", reason);
        json.set("type", "会议申请");

        String param = JSONUtil.toJsonStr(json);
        try {
            rabbitSender.send(param, Constants.TARGET_DELETE_PROCESS_BY_ID);
        } catch (Exception e) {
            log.error("删除会议工作流异常", e);
        }
    }

}
