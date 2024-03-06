package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.pojo.TbAmectType;

import java.util.ArrayList;
import java.util.HashMap;

public interface AmectTypeService {
    ArrayList<TbAmectType> searchAllAmectType();

    PageUtils searchAmectTypeByPage(HashMap param);

    int insert(TbAmectType amectType);

    HashMap searchById(int id);

    int update(HashMap param);

    int deleteAmectTypeByIds(Integer[] ids);
}
