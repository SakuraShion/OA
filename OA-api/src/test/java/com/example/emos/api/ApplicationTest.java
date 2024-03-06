package com.example.emos.api;

import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.Constants;
import com.example.emos.api.common.util.RedisCache;
import com.example.emos.api.task.rabbit.RabbitSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created By zf
 * 描述:
 */
@SpringBootTest
public class ApplicationTest {
    @Test
    public void contextLoads() {

    }

    @Autowired
    private RabbitSender rabbitSender;

    @Test
    public void send() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "新绛");
        properties.put("password", "123456");
        properties.put("num", "1");
        String j = JSONUtil.parse(properties).toStringPretty();



        rabbitSender.send(j, "ps");
        while (true) {

        }


    }
}
