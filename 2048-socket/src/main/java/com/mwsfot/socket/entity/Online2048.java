package com.mwsfot.socket.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * @author MinChang
 * @description 联机版2048游戏设置
 * @date 2024/5/21 17:02
 */
@Data
public class Online2048 extends Game implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 限制时间
     */
    private Integer limitTime;
    /**
     * 游戏等级难度
     */
    private Integer mode;
}
