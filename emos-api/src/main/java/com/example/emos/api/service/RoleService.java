package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbRoleDao;
import com.example.emos.api.db.pojo.TbRole;

import java.util.ArrayList;
import java.util.HashMap;

public interface RoleService {
    ArrayList<HashMap> searchAllRole();

    HashMap searchById(int id);

    PageUtils searchRoleByPage(HashMap param);

    int insert(TbRole tbRole);

    ArrayList<Integer> searchUserIdByRoleId(int roleId);

    int update(TbRole tbRole);

    int deleteRoleByIds(Integer[] ids);
}
