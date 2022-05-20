package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {
    Set<String> searchUserPermissions(int userId);

    HashMap searchById(int userId);

    Integer searchIdByOpenId(String openId);

    HashMap searchUserSummary(int userId);

    HashMap searchUserInfo(int userId);

    Integer searchDeptManagerId(int id);

    Integer searchGmId();

    ArrayList<HashMap> searchAllUser();

    Integer login(HashMap param);

    int updatePassword(HashMap param);

    ArrayList<HashMap> searchUserByPage(HashMap param);

    long searchUserCount(HashMap param);

    int insert(TbUser user);

    int update(HashMap param);

    int deleteUserByIds(Integer[] ids);

    ArrayList<String> searchUserRoles(int userId);

    HashMap searchNameAndDept(int userId);

    int setPhoto(HashMap param);

    HashMap searchEmailAndName(int id);

}