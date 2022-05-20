package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbLeave;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbLeaveDao {
    ArrayList<HashMap> searchLeaveByPage(HashMap param);

    int searchLeaveCount(HashMap param);

    int updateLeaveInstanceId(HashMap param);

    long searchContradiction(HashMap param);

    int insert(TbLeave leave);

    String searchInstanceIdById(int id);

    int deleteLeaveById(HashMap param);

    HashMap searchLeaveById(HashMap param);
}
