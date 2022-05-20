package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbAmect;
import com.example.emos.api.db.pojo.TbReim;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbReimDao {
    ArrayList<HashMap> searchReimByPage(HashMap param);

    long searchReimCount(HashMap param);

    int updateReimInstanceId(HashMap param);

    int insert(TbReim reim);

    HashMap searchReimById(HashMap param);

    String searchInstanceIdById(int id);

    int deleteReimById(HashMap param);
}
