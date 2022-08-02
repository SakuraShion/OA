package com.example.emos.workflow.strategy;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Service
@Slf4j
public class PsService implements IWorkflowService{
    @PostConstruct
    public void init() {
        WorkflowStrategyFactory.register("ps", this);
    }

    @Override
    public void service(String parameter) {
        HashMap map = JSONUtil.parse(parameter).toBean(HashMap.class);
        System.out.println("================ 业务逻辑： ===================");
        System.out.println(map);
    }
}
