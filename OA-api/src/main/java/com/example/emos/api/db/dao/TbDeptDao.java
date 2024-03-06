package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbDeptDao {
    ArrayList<HashMap> searchAllDept();

    HashMap searchById(int id);

    ArrayList<HashMap> searchDeptByPage(HashMap param);

    long searchDeptCount(HashMap param);

    int insert(TbDept dept);

    int update(TbDept dept);

    boolean searchCanDelete(Integer[] ids);

    int deleteDeptIds(Integer[] ids);
}