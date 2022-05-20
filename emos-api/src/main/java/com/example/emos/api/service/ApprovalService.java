package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;

import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
public interface ApprovalService {

    PageUtils searchTaskByPage(HashMap param);

    HashMap searchApprovalContent(HashMap param);

    void approvalTask(HashMap param);

    void archiveTask(HashMap param);
}
