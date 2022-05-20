package com.example.emos.workflow.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbUserDao {
    ArrayList<String> searchEmailByIds(int[] ids);

    ArrayList<String> searchEmailByRoles(String[] roles);

    HashMap searchEmailAndName(int id);
}
