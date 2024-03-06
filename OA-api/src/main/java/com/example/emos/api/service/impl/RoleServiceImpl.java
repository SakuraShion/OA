package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbRoleDao;
import com.example.emos.api.db.pojo.TbRole;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private TbRoleDao roleDao;

    @Override
    public ArrayList<HashMap> searchAllRole() {
        ArrayList<HashMap> list = roleDao.searchAllRole();
        return list;
    }

    @Override
    public HashMap searchById(int id) {
        HashMap map = roleDao.searchById(id);
        return map;
    }

    @Override
    public PageUtils searchRoleByPage(HashMap param) {
        ArrayList<HashMap> list = roleDao.searchRoleByPage(param);
        long count = roleDao.searchRoleCount(param);
        int start = (int) param.get("start");
        int length = (int) param.get("length");

        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public int insert(TbRole tbRole) {
        return roleDao.insert(tbRole);
    }

    @Override
    public ArrayList<Integer> searchUserIdByRoleId(int roleId) {
        return roleDao.searchUserIdByRoleId(roleId);
    }

    @Override
    public int update(TbRole tbRole) {
        return roleDao.update(tbRole);
    }

    @Override
    public int deleteRoleByIds(Integer[] ids) {
        if (!roleDao.searchCanDelete(ids)) {
            throw new EmosException("无法删除存在用户关联的角色");
        }
        return roleDao.deleteRoleByIds(ids);
    }
}
