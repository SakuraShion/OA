package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbMeetingRoom;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbMeetingRoomDao {
    ArrayList<HashMap> searchAllMeetingRoom();
    
    HashMap searchById(int id);
    
    ArrayList<String> searchFreeMeetingRoom(HashMap param);

    ArrayList<HashMap> searchMeetingRoomByPage(HashMap param);

    int searchMeetingRoomCount(HashMap param);

    int insert(TbMeetingRoom meetingRoom);

    int update(TbMeetingRoom meetingRoom);

    boolean searchCanDelete(Integer[] ids);

    int deleteMeetingRoomByIds(Integer[] ids);

}
