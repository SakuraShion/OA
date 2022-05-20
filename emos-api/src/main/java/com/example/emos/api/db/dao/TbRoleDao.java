package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;


@Mapper
public interface TbRoleDao {
    ArrayList<HashMap> searchAllRole();

    HashMap searchById(int id);

    ArrayList<HashMap> searchRoleByPage(HashMap param);

    long searchRoleCount(HashMap param);

    int insert(TbRole tbRole);

    ArrayList<Integer> searchUserIdByRoleId(int roleId);

    int update(TbRole tbRole);

    boolean searchCanDelete(Integer[] ids);

    int deleteRoleByIds(Integer[] ids);
}