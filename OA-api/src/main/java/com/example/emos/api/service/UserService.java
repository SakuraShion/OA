package com.example.emos.api.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public interface UserService {
    HashMap createQrCode();

    boolean checkQrCode(String code,String uuid);

    HashMap wechatLogin(String uuid);

    Set<String> searchUserPermissions(int userId);

    HashMap searchUserSummary(int userId);

    HashMap searchById(int userId);

    ArrayList<HashMap> searchAllUser();

    Integer login(HashMap param);

    int updatePassword(HashMap param);

    PageUtils searchUserByPage(HashMap param);

    int insert(TbUser user);

    int update(HashMap param);

    int deleteUserByIds(Integer[] ids);

    ArrayList<String> searchUserRoles(int userId);

    HashMap searchNameAndDept(int userId);

    int setPhoto(HashMap param);
}
