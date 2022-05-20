package com.example.emos.api.service.impl;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbDeptDao;
import com.example.emos.api.db.pojo.TbDept;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class DeptServiceImpl implements DeptService {
    @Autowired
    private TbDeptDao deptDao;

    @Override
    public ArrayList<HashMap> searchAllDept() {
        ArrayList<HashMap> list = deptDao.searchAllDept();
        return list;
    }

    @Override
    public HashMap searchById(int id) {
        HashMap map = deptDao.searchById(id);
        return map;
    }

    @Override
    public PageUtils searchDeptByPage(HashMap param) {
        ArrayList<HashMap> list = deptDao.searchDeptByPage(param);
        long count = deptDao.searchDeptCount(param);
        int start = (int) param.get("start");
        int length = (int) param.get("length");
        PageUtils page = new PageUtils(list, count, start, length);
        return page;
    }

    @Override
    public int insert(TbDept dept) {
        return deptDao.insert(dept);
    }

    @Override
    public int update(TbDept dept) {
        return deptDao.update(dept);
    }

    @Override
    public int deleteDeptIds(Integer[] ids) {
        if (!deptDao.searchCanDelete(ids))
            throw new EmosException("无法删除关联用户的部门");

        return deptDao.deleteDeptIds(ids);
    }
}
