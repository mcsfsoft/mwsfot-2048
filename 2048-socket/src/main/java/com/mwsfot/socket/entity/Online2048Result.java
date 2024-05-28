package com.mwsfot.socket.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author MinChang
 * @description 返回体
 * @date 2024/5/27 11:42
 */
@Data
@Builder
public class Online2048Result {
    private String userId;
    private String roomId;
    private String mode;
    private String newTime;
    private String score;
    private String msg;
    private String holder;
    private String unHolder;
    private Integer power;
    private Boolean isPunish;
}
