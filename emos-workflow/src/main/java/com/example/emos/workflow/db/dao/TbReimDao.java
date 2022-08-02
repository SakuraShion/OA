package com.example.emos.workflow.db.dao;

import com.example.emos.workflow.db.pojo.TbAmect;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbReimDao {
    HashMap searchReimByInstanceId(String instanceId);

    int updateReimStatus(HashMap param);

    int updateReimInstanceId(HashMap param);

    HashMap searchStartInfo(String id);
}
