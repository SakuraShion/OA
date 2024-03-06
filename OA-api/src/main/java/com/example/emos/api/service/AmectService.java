package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.pojo.TbAmect;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
public interface AmectService {
    PageUtils searchAmectByPage(HashMap param);

    int insert(ArrayList<TbAmect> params);

    HashMap searchById(int id);

    int update(HashMap param);

    int delete(Integer[] ids);

    String createNativeAmectPayOrder(HashMap param);

    int updateStatus(HashMap param);

    Integer searchUserByUUID(String uuid);

    void searchNativeAmectPayResult(HashMap param);

    int searchStatus(int amectId);

    HashMap searchChart(HashMap param);
}
