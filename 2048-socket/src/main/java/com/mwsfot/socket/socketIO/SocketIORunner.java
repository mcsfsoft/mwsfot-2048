package com.mwsfot.socket.socketIO;

import com.corundumstudio.socketio.SocketIOServer;
import com.mwsfot.socket.game.handler.Game2048Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * @author MinChang
 * @description 启动器
 * @date 2024/5/24 14:04
 */
@Component
public class SocketIORunner implements SmartLifecycle {

    @Autowired
    private SocketIOServer server;

    @Autowired
    private Game2048Handler game2048Handler;
    @Override
    public void start() {
        this.server.start();
        //专门给2048添加处理
        server.getNamespace("/game2048").addListeners(game2048Handler);
        System.out.println("SocketIO Server Success To Start");
    }

    @Override
    public void stop() {
        this.server.stop();
        System.out.println("SocketIO Server Success To Stop");
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
