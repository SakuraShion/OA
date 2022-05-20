package com.example.emos.api.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectTypeDao;
import com.example.emos.api.db.pojo.TbAmectType;
import com.example.emos.api.service.AmectTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class AmectTypeServiceImpl implements AmectTypeService {
    @Autowired
    private TbAmectTypeDao amectTypeDao;

    @Override
    public ArrayList<TbAmectType> searchAllAmectType() {
        ArrayList<TbAmectType> list = amectTypeDao.searchAllAmectType();
        return list;
    }

    @Override
    public PageUtils searchAmectTypeByPage(HashMap param) {
        ArrayList<HashMap> list = amectTypeDao.searchAmectTypeByPage(param);
        int count = amectTypeDao.searchAmectTypeCount(param);
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        return new PageUtils(list, count, start, length);
    }

    @Override
    public int insert(TbAmectType amectType) {
        return amectTypeDao.insert(amectType);
    }

    @Override
    public HashMap searchById(int id) {
        return amectTypeDao.searchById(id);
    }

    @Override
    public int update(HashMap param) {
        return amectTypeDao.update(param);
    }

    @Override
    public int deleteAmectTypeByIds(Integer[] ids) {
        return amectTypeDao.deleteAmectTypeByIds(ids);
    }
}