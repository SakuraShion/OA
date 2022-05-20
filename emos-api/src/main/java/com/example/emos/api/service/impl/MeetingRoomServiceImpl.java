package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbMeetingRoomDao;
import com.example.emos.api.db.pojo.TbMeetingRoom;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class MeetingRoomServiceImpl implements MeetingRoomService {
    @Autowired
    private TbMeetingRoomDao meetingRoomDao;

    @Override
    public ArrayList<HashMap> searchAllMeetingRoom() {
        ArrayList<HashMap> list = meetingRoomDao.searchAllMeetingRoom();
        return list;
    }

    @Override
    public HashMap searchById(int id) {
        HashMap map = meetingRoomDao.searchById(id);
        return map;
    }

    @Override
    public ArrayList<String> searchFreeMeetingRoom(HashMap param) {
        ArrayList<String> list = meetingRoomDao.searchFreeMeetingRoom(param);
        return list;
    }

    @Override
    public PageUtils searchMeetingRoomByPage(HashMap param) {
        ArrayList<HashMap> list = meetingRoomDao.searchMeetingRoomByPage(param);
        int count = meetingRoomDao.searchMeetingRoomCount(param);
        int start = (int) param.get("start");
        int length = (int) param.get("length");
        PageUtils page = new PageUtils(list, count, start, length);
        return page;
    }

    @Override
    public int insert(TbMeetingRoom meetingRoom) {
        return meetingRoomDao.insert(meetingRoom);
    }

    @Override
    public int update(TbMeetingRoom meetingRoom) {
        return meetingRoomDao.update(meetingRoom);
    }

    @Override
    public int deleteMeetingRoomByIds(Integer[] ids) {
        if (!meetingRoomDao.searchCanDelete(ids))
            throw new EmosException("无法删除关联会议的会议室");
        return meetingRoomDao.deleteMeetingRoomByIds(ids);
    }
}