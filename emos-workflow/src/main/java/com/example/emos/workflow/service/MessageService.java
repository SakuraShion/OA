package com.example.emos.api.service;

import com.example.emos.api.db.pojo.MessageEntity;
import com.example.emos.api.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

/**
 * Created By zf
 * 描述:
 */
public interface MessageService {
    String insertMessage(MessageEntity entity);

    List<HashMap> searchMessageByPage(int userId, long start, int length);

    HashMap searchMessageById(String id);

    String insertRef(MessageRefEntity entity);

    long searchUnreadCount(int userId);

    long searchMessageCount(int userId);

    long searchLastCount(int userId);

    long updateUnreadMessage(String id);

    long deleteMessageRefById(String id);

    long deleteUserMessageRef(int userId);
}
