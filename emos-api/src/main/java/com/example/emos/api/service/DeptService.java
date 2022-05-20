package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.pojo.TbDept;

import java.util.ArrayList;
import java.util.HashMap;

public interface DeptService {
    ArrayList<HashMap> searchAllDept();

    HashMap searchById(int id);

    PageUtils searchDeptByPage(HashMap param);

    int insert(TbDept dept);

    int update(TbDept dept);

    int deleteDeptIds(Integer[] ids);
}
