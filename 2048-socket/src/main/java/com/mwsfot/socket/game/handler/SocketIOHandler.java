package com.mwsfot.socket.game.handler;

import cn.hutool.core.util.StrUtil;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.mwsfot.socket.constants.CacheConstants;
import com.mwsfot.socket.entity.User;
import com.mwsfot.socket.socketIO.SocketIOSessionManager;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * @author MinChang
 * @description socketIO 方法处理
 * @date 2024/5/24 14:01
 */

@Slf4j
@Component
public class SocketIOHandler {
    @Autowired
    private SocketIOSessionManager socketIOSessionManager;

    @Autowired
    private RedisTemplate<String, User> userRedisTemplate;


    /**
     * 建立连接
     * 客户端请求 http://localhost:9999?userId=12345
     * //下面两种是加了命名空间的，他会请求对应命名空间的方法（就类似你进了不同的房间玩游戏）
     * //因为我定义用户的参数为userId，你也可以定义其他名称 客户端请求 http://localhost:9999/test?userId=12345
     * //因为我定义用户的参数为userId，你也可以定义其他名称 客户端请求 http://localhost:9999/SocketIO?userId=12345
     *
     * @param client socketIO 客户端
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if ( userId == null ) {
            return;
        }
        String sessionId = client.getSessionId().toString();
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        User user = ops.get(userKey);
        //如果不存在,就新增一个进去,保存用户sessionId, 同一个页面sessionId不会变更. 将多个页面算为两个用户
        if ( user == null ||!user.getSessionId().equals(sessionId)) {
            ops.set(userKey, User.builder().userId(userId).sessionId(sessionId).build());
            socketIOSessionManager.add(userId, client);
        }
        log.info("ip = {}; sessionId = {};  userId = {} 连接成功", client.getRemoteAddress(), sessionId, userId);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        String sessionId = client.getSessionId().toString();
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        User user = ops.get(userKey);
        //只会删除某个用户页面缓存, 不会删除这个用户下所有
        if(user != null){
            userRedisTemplate.expire(userKey, 0, TimeUnit.SECONDS);
            socketIOSessionManager.removeSession(userId);
            log.info("用户id = {}; sessionId = {} 关闭连接成功", userId, client.getSessionId());
        }
    }
}

