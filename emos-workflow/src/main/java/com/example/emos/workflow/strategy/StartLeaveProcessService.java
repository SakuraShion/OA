package com.example.emos.workflow.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.workflow.common.util.Constants;
import com.example.emos.workflow.db.dao.TbLeaveDao;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.apache.commons.lang3.StringUtils;
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
@SuppressWarnings("all")
public class StartLeaveProcessService implements IWorkflowService{
    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void service(String parameter) {
        HashMap param = JSONUtil.parse(parameter).toBean(HashMap.class);
        param.put("filing", false);
        param.put("type", "员工请假");
        param.put("createDate", DateUtil.today());
        int id = (int) param.get("id");

        String instanceId = runtimeService.startProcessInstanceByKey("leave", param).getProcessInstanceId();

        if (StringUtils.isNotBlank(instanceId)) {

            param.clear();
            param.put("instanceId", instanceId);
            param.put("id", id);

            int rows = leaveDao.updateLeaveInstanceId(param);

            if (rows != 1) {
                log.error("保存报销申请工作流实例ID失败");
            }
        } else {
            log.error("工作流异常");
        }
    }

    @PostConstruct
    public void init() {
        WorkflowStrategyFactory.register(Constants.TARGET_START_LEAVE_PROCESS, this);
    }
}
