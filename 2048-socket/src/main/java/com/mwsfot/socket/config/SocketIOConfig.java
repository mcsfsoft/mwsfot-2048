package com.mwsfot.socket.config;

import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.corundumstudio.socketio.protocol.JsonSupport;
import com.mwsfot.socket.game.handler.Game2048Handler;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author MinChang
 * @description socketIO 配置
 * @date 2024/5/24 13:57
 */
@Configuration
public class SocketIOConfig {

    @Value("${mwsfot.socketIO.host}")
    private String host;

    @Value("${mwsfot.socketIO.port}")
    private Integer port;

    @Value("${mwsfot.socketIO.bossCount}")
    private Integer bossCount;

    @Value("${mwsfot.socketIO.workCount}")
    private Integer workCount;

    @Value("${mwsfot.socketIO.allowCustomRequests}")
    private Boolean allowCustomRequests;

    @Value("${mwsfot.socketIO.upgradeTimeout}")
    private Integer upgradeTimeout;

    @Value("${mwsfot.socketIO.pingTimeout}")
    private Integer pingTimeout;
    @Value("${mwsfot.socketIO.pingInterval}")
    private Integer pingInterval;
    @Value("${mwsfot.socketIO.maxFramePayloadLength}")
    private Integer maxFramePayloadLength;
    @Value("${mwsfot.socketIO.maxHttpContentLength}")
    private Integer maxHttpContentLength;
    @Value("${mwsfot.socketIO.namespaces}")
    private String[] namespaces;


    @Bean
    public SocketIOServer socketIOServer() {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        com.corundumstudio.socketio.Configuration config =
            new com.corundumstudio.socketio.Configuration();
        config.setSocketConfig(socketConfig);
        config.setHostname(host);
        config.setPort(port);
        config.setBossThreads(bossCount);
        config.setWorkerThreads(workCount);
        config.setAllowCustomRequests(allowCustomRequests);
        config.setUpgradeTimeout(upgradeTimeout);
        config.setPingTimeout(pingTimeout);
        config.setPingInterval(pingInterval);
        config.setMaxHttpContentLength(maxHttpContentLength);
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setOrigin("*");
        SocketIOServer server = new SocketIOServer(config);
        Optional.ofNullable(namespaces).ifPresent(v-> Arrays.stream(v).forEach(server::addNamespace));
        return server;
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner() {
        return new SpringAnnotationScanner(socketIOServer());
    }
}
