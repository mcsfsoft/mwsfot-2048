package com.mwsfot.socket.enums;

/**
 * @author MinChang
 * @description 游戏状态
 * @date 2024/5/21 16:45
 */
public enum GameEnum {
    CREATED(0, "新建游戏"),
    START(1, "开始游戏"),
    PAUSE(2,"暂停游戏"),
    STOP(3, "游戏结束"),
    DESTROY(4,"销毁游戏");
    private final Integer type;
    private final String name;

    GameEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
