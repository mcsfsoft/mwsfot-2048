package com.mwsfot.socket.socketIO;

import cn.hutool.core.util.StrUtil;
import com.corundumstudio.socketio.SocketIOClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * @author MinChang
 * @description SocketIO Session 管理
 * @date 2024/5/24 14:02
 */
@Component
public class SocketIOSessionManager {
    private static final Map<String, SocketIOClient> ONLINE_SESSION =
        new ConcurrentHashMap<>();

    public synchronized void add(String userId, SocketIOClient socketIOClient) {
        if ( StrUtil.isNotBlank(userId) ) {
            ONLINE_SESSION.put(userId, socketIOClient);
        }
    }

    public synchronized SocketIOClient get(String userId) {
        return ONLINE_SESSION.get(userId);
    }

    public synchronized void removeSession(String userId) {
        if ( !ONLINE_SESSION.containsKey(userId) ) {
            return;
        }
        ONLINE_SESSION.remove(userId);
    }
}
