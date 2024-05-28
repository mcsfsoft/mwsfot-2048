package com.mwsfot.socket.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * @author MinChang
 * @description 这是个超类, 具体的游戏由实现类实现
 * @date 2024/5/21 16:36
 */
@Data
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private String gameId;
    private String gameName;

}
