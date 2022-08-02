package com.example.emos.workflow.strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By zf
 * 描述: 策略模式 + 工厂模式
 */
public class WorkflowStrategyFactory {
    private final static Map<String, IWorkflowService> WORKFLOW_STRATEGY = new HashMap<>();

    public static void register(String code, IWorkflowService service) {
        if(code != null && !"".equals(code)) {
            WORKFLOW_STRATEGY.put(code, service);
        }
    }

    public static IWorkflowService get(String code) {
        return WORKFLOW_STRATEGY.get(code);
    }

    public static void toService(String code, String parameter) {
        get(code).service(parameter);
    }
}
