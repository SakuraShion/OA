package com.example.emos.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbMeetingDao;
import com.example.emos.api.db.pojo.TbMeeting;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.MeetingService;
import com.example.emos.api.task.MeetingWorkflowTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MeetingWorkflowTask meetingWorkflowTask;

    @Autowired
    private TbMeetingDao meetingDao;

    @Override
    public PageUtils searchOfflineMeetingByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingByPage(param);
        long count = meetingDao.searchOfflineMeetingCount(param);
        int start = (int) param.get("start");
        int length = (int) param.get("length");

        // SQL 返回中的meeting 字段内容是 json 数组，但类型是字符串，所以需要对其进行转换成 json 数组
        for (HashMap map : list) {
            String meeting = (String) map.get("meeting");
            
            if (meeting != null && meeting.length() != 0) {
                map.replace("meeting", JSONUtil.parseArray(meeting));
            }
        }
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public int insert(TbMeeting meeting) {
        int rows = meetingDao.insert(meeting);
        if (rows != 1) {
            throw new EmosException("会议添加失败");
        }

        // 异步线程去创建流程实例，并修改会议信息
        meetingWorkflowTask.startMeetingWorkflow(meeting.getUuid(), meeting.getCreatorId(), meeting.getTitle(),
                meeting.getDate(), meeting.getStart() + ":00", meeting.getType() == 1 ? "线上会议" : "线下会议");
        return rows;
    }

    @Override
    public ArrayList<HashMap> searchOfflineMeetingInWeek(HashMap param) {
        return meetingDao.searchOfflineMeetingInWeek(param);
    }

    @Override
    public HashMap searchMeetingInfo(short status, long id) {
        HashMap map;

        if (status == 4 || status == 5) {
            map = meetingDao.searchCurrentMeetingInfo(id);
        } else {
            map = meetingDao.searchMeetingInfo(id);
        }
        return map;
    }

    @Override
    public int deleteMeetingApplication(HashMap param) {
        Long id = MapUtil.getLong(param, "id");
        String uuid = MapUtil.getStr(param, "uuid");
        String instanceId = MapUtil.getStr(param, "instanceId");

        // 判断距离会议开始时间到当前时间是否不足二十分钟，大于二十分钟才可删除
        HashMap meeting = meetingDao.searchMeetingById(param);
        String date = MapUtil.getStr(meeting, "date");
        String start = MapUtil.getStr(meeting, "start");
        int status = MapUtil.getInt(meeting, "status");
        boolean isCreator = Boolean.parseBoolean(MapUtil.getStr(meeting, "isCreator"));

        DateTime dateTime = DateUtil.parse(date + " " + start);
        DateTime now = DateUtil.date();

        // 会议开始时间向前偏移二十分钟，然后和当前时间对比，当前时间是否不超过其偏移后的值
        if (now.isAfterOrEquals(dateTime.offset(DateField.MINUTE, -20))) {
            throw new EmosException("距离会议开始时间不足二十分钟，无法删除会议");
        }

        // 只有申请人才能删除
        if (!isCreator)
            throw new EmosException("只有会议创建人才能删除会议申请");

        // 未审批或未开始的会议可以删除
        if (status == 1 || status ==3) {
            int rows = meetingDao.deleteMeetingApplication(param);

            if (rows == 1) {
                // 修改成功后调用异步线程对工作流实例进行停止
                String reason = MapUtil.getStr(param, "reason");
                meetingWorkflowTask.deleteMeetingApplication(uuid, instanceId, reason);
            }

            return rows;
        } else {
            throw new EmosException("只有未审批或未开始的会议才可以删除");
        }

    }

    @Override
    public PageUtils searchOnlineMeetingByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingByPage(param);
        long count = meetingDao.searchOnlineMeetingCount(param);
        int start = (int) param.get("start");
        int length = (int) param.get("length");
        return new PageUtils(list, count, start, length);
    }

    @Override
    public Long searchRoomIdByUUID(String uuid) {
        if (redisTemplate.hasKey(uuid)) {
            Object temp = redisTemplate.opsForValue().get(uuid);
            long roomId = Long.parseLong(temp.toString());
            return roomId;
        }
        return null;
    }

    @Override
    public ArrayList<HashMap> searchOnlineMeetingMembers(HashMap param) {
        return meetingDao.searchOnlineMeetingMembers(param);
    }

    @Override
    public boolean searchCanCheckingMeeting(HashMap param) {
        long count = meetingDao.searchCanCheckingMeeting(param);
        return count == 1;
    }

    @Override
    public int updateMeetingPresent(HashMap param) {
        return meetingDao.updateMeetingPresent(param);
    }




}
