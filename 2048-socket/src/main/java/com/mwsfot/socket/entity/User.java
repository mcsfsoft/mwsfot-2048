package com.mwsfot.socket.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author MinChang
 * @description 用户socket
 * @date 2024/5/21 16:30
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名称
     */
    private String userName;
    private String sessionId;
    private Date lastTime;

}
