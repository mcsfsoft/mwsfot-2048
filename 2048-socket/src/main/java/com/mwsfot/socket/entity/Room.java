package com.mwsfot.socket.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author MinChang
 * @description 房间Socket信息
 * @date 2024/5/21 16:31
 */
@Data
public class Room<Game> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 房间ID
     */
    private String roomId;
    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 房间状态(必填)
     * @see com.mwsfot.socket.enums.RoomEnum
     */
    private Integer roomStatus;

    /**
     * 游戏状态(必填)
     * @see com.mwsfot.socket.enums.GameEnum
     */
    private Integer gameStatus;
    /**
     * 同一房间最大用户数
     */
    private Integer maxUserNum;

    private Game game;
    /**
     * 用户对应的游戏信息
     */
    private Map<String, Score> scoreMap;
    /**
     * 用户socket信息
     */
    private List<User> userList;

    /**
     * 用户权限
     */
    private List<UserPermission> userPermission;
    /**
     * 时间戳自增值
     */
    private Integer timestamp;

}
