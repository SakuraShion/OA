package com.example.emos.api.task;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.Constants;
import com.example.emos.api.db.dao.TbReimDao;
import com.example.emos.api.db.dao.TbUserDao;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.task.rabbit.RabbitSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAKey;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Component
@Slf4j
public class ReimWorkflow {

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private RabbitSender rabbitSender;

    @Async("AsyncTaskExecutor")
    public void startReimWorkflow(int id, int creatorId) {
        HashMap info = userDao.searchUserInfo(creatorId);
        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("creatorId", creatorId);
        json.set("creatorName", info.get("name").toString());
        json.set("title", info.get("dept").toString() + info.get("name").toString() + "的报销");
        Integer managerId = userDao.searchDeptManagerId(creatorId);
        json.set("managerId", managerId);
        Integer gmId = userDao.searchGmId();
        json.set("gmId", gmId);
        json.set("id", id);

        String param = JSONUtil.toJsonStr(json);

        try {
            rabbitSender.send(param, Constants.TARGET_START_REIM_PROCESS);
        } catch (Exception e) {
            log.error("生产者发送异常", e);
        }

//        if (resp.getStatus() == 200) {
//            json = JSONUtil.parseObj(resp.body());
//            String instanceId = json.getStr("instanceId");
//
//            HashMap param = new HashMap();
//            param.put("instanceId", instanceId);
//            param.put("id", id);
//
//            int rows = reimDao.updateReimInstanceId(param);
//
//            if (rows != 1) {
//                throw new EmosException("保存报销申请工作流实例ID失败");
//            }
//        } else {
//            log.error(resp.body());
//            throw new EmosException("工作流回调异常");
//        }
    }

    public void deleteReimWorkflow(String instanceId, String type, String reason) {
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("type", type);
        json.set("reason", reason);

        String param = JSONUtil.toJsonStr(json);

        try {
            rabbitSender.send(param, Constants.TARGET_DELETE_PROCESS_BY_ID);
        } catch (Exception e) {
            log.error("生产者发送异常", e);
        }

    }
}
