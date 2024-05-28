package com.mwsfot.socket.enums;

/**
 * @author MinChang
 * @description 房间状态
 * @date 2024/5/21 16:40
 */
public enum RoomEnum {
    CREATED(0, "新建房间"),
    RUNNING(1, "进行中"),
    DESTROY(2,"销毁房间")
    ;
    private final Integer type;
    private final String name;

    RoomEnum(Integer type, String name) {
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
