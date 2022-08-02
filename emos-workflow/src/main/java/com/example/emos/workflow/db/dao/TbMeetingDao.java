package com.example.emos.workflow.db.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbMeetingDao {
    HashMap searchMeetingByInstanceId(String instanceId);

    boolean searchMeetingMembersInSameDept(String uuid);

    HashMap searchMeetingByUUID(String uuid);

    List<String> searchUserMeetingInMonth(HashMap param);

    int updateMeetingStatus(HashMap param);

    ArrayList<Integer> searchMeetingUnpresent(String uuid);

    ArrayList<Integer> searchMeetingMembers(@Param("uuid") String uuid);

    int updateMeetingUnpresent(HashMap param);

    int updateMeetingInstanceId(HashMap param);

    HashMap searchStartAndEndByUserId(String id);
}
