package com.example.emos.api.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbLeaveDao;
import com.example.emos.api.db.pojo.TbLeave;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.LeaveService;
import com.example.emos.api.task.LeaveWorkflowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveWorkflowTask leaveWorkflowTask;

    @Autowired
    private TbLeaveDao leaveDao;

    @Override
    public PageUtils searchLeaveByPage(HashMap param) {
        ArrayList<HashMap> list = leaveDao.searchLeaveByPage(param);
        int count = leaveDao.searchLeaveCount(param);
        int start = (int) param.get("start");
        int length = (int)param.get("length");

        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    /**
     * 判断选择的请假时间是否和已申请的有交集
     */
    @Override
    public boolean searchContradiction(HashMap param) {
        long count = leaveDao.searchContradiction(param);
        return count > 0;
    }

    @Override
    public int insert(TbLeave leave) {
        int rows = leaveDao.insert(leave);
        if (rows == 1) {
            // 开启工作流
            leaveWorkflowTask.startLeaveWorkflow(leave.getId(), leave.getUserId(), leave.getDays());
        } else {
            throw new EmosException("请假添加失败");
        }
        return rows;
    }

    @Override
    public int deleteLeaveById(HashMap param) {
        int id = MapUtil.getInt(param, "id");
        String instanceId = leaveDao.searchInstanceIdById(id);
        int rows = leaveDao.deleteLeaveById(param);
        if (rows == 1) {
            leaveWorkflowTask.deleteWorkflow(instanceId, "员工请假", "删除请假申请");
        } else {
            throw new EmosException("删除请假申请失败");
        }
        return rows;
    }

    @Override
    public HashMap searchLeaveById(HashMap param) {
        return leaveDao.searchLeaveById(param);
    }
}
