package com.mwsfot.socket.constants;

/**
 * @author MinChang
 * @description 订阅常量池
 * @date 2024/5/21 14:34
 */
public interface SubscribeConstants {

    /**
     * 成功标记
     */
    Integer SUCCESS = 0;

    /**
     * 失败标记
     */
    Integer FAIL = 1;
    // 心跳包
    String PING = "ping";
    // 取消订阅
    String UNSUBSCRIBE = "unsubscribe";
    String SUBSCRIBE = "subscribe";
    String PUBLISH= "publish";
}
