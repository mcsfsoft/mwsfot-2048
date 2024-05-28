package com.mwsfot.socket.enums;

import com.mwsfot.socket.constants.CacheConstants;
import java.util.Stack;

/**
 * @author MinChang
 * @description type 对应关系
 * @date 2024/5/21 16:45
 */
public enum SocketTypeEnum {
    GAME("0", CacheConstants.PREFIX_GAME_KEY),
    ROOM("1",CacheConstants.PREFIX_ROOM_KEY),
    SCORE("2",CacheConstants.PREFIX_SCORE_KEY),
    USER("3", CacheConstants.PREFIX_USER_KEY),
    ERROR("999","999");
    private final String type;
    private final String key;

    SocketTypeEnum(String type, String key) {
        this.type = type;
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public static SocketTypeEnum getEnum(String key){
        if(key == null){
            return ERROR;
        }
        for (SocketTypeEnum value : values()) {
            if(value.getKey().equals(key)){
                return value;
            }
        }
        return ERROR;
    }
}
