package com.mwsfot.socket.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * @author MinChang
 * @description 分数
 * @date 2024/5/21 16:57
 */
@Data
public class Score implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 对应游戏ID
     */
    private String gameId;
    /**
     * 对应游戏名称
     */
    private String gameName;
    /**
     * 当前得分
     */
    private String currentScore;
    /**
     * 最高得分
     */
    private String maxScore;
    /**
     * 使用时间
     */
    private String seconds;
}
