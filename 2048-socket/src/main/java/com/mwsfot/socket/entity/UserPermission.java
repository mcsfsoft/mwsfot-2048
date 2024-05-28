package com.mwsfot.socket.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * @author MinChang
 * @description 用户权限对应关系
 * @date 2024/5/24 18:07
 */
@Data
public class UserPermission implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private Integer permission;
}
