package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
public interface MeetingService {

    PageUtils searchOfflineMeetingByPage(HashMap param);

    int insert(TbMeeting meeting);

    ArrayList<HashMap> searchOfflineMeetingInWeek(HashMap param);

    HashMap searchMeetingInfo(short status, long id);

    int deleteMeetingApplication(HashMap param);

    PageUtils searchOnlineMeetingByPage(HashMap param);

    Long searchRoomIdByUUID(String uuid);

    ArrayList<HashMap> searchOnlineMeetingMembers(HashMap param);

    boolean searchCanCheckingMeeting(HashMap param);

    int updateMeetingPresent(HashMap param);
}
