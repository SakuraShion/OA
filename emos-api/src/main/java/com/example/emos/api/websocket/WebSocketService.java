package com.example.emos.api.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created By zf
 * 描述:
 */
@Slf4j
@ServerEndpoint(value = "/socket")
@Component
public class WebSocketService {
    // 缓存 session，通过提取缓存的 session 主动向客户端发送消息
    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        //创建WebSocket连接时候执行
    }

    @OnClose
    public void onClose(Session session) {
        // 在 session 中获取 userId，通过 userId 将 sessionMap 中对应 session 进行清除
        Map map =session.getUserProperties();
        if (map.containsKey("userId")) {
            String userId = MapUtil.getStr(map, "userId");
            sessionMap.remove(userId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        //接收消息时候执行
        JSONObject json = JSONUtil.parseObj(message);
        String opt = json.getStr("opt");
        /**
         * 虽然 WebSocket 是长连接，但也存在超时时间，如果超过这个超时时间没有使用这个 WebSocket，连接就会被切断；
         * 所以为了避免触发连接超时，我们让客户端每隔一段时间发送一些数据，数据中携带 opt 字段，如果 opt 字段的值为 ping 就说明是客户端发送的轮询请求;
         * WebSocket 如果断掉的话，前端就无法接收到后端发送的消息了
         */

        if ("ping".equals(opt)) {
            return;
        }

        String token = json.getStr("token");
        // 根据 token 进行转化获取到 userId 转换成字符串，做为 key 保存 Session 到 map 中
        String userId = StpUtil.stpLogic.getLoginIdByToken(token).toString();
        // 向session 中绑定userId 属性，因为这样的话在关闭WebSocket 时，就可以通过session 中绑定的userId 去sessionMap 中将对应 session清除
        Map map = session.getUserProperties();
        if (!map.containsKey("userId")) {
            map.put("userId", userId);
        }

        // 判断当前 session 在 sessionMap 中是否有缓存，有点话就进行替换，没有就添加进去
        if (sessionMap.containsKey(userId)) {
            sessionMap.replace(userId, session);
        } else {
            sessionMap.put(userId, session);
        }


    }

    @OnError
    public void onError(Session session, Throwable error) {
        //发生错误时候执行
        log.error("发生错误", error);
    }

    /**
     * 发送消息给客户端
     * @param message 要发送的消息
     * @param userId 要发送的对象，通过userId 获取到 对应的 session 调用封装的静态方法，将消息发送给 session
     */
    public static void sendInfo(String message, String userId) {
        if (StrUtil.isNotBlank(userId) && sessionMap.containsKey(userId)) {
            Session session = sessionMap.get(userId);
            sendMessage(session, message);
        }
    }

    /**
     * 封装发送消息给客户端
     */
    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("执行异常", e);
        }
    }
}
