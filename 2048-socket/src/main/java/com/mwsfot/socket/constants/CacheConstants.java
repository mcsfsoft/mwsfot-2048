package com.mwsfot.socket.constants;

import cn.hutool.core.util.StrUtil;

/**
 * @author MinChang
 * @description 待完善
 * @date 2024/5/21 17:34
 */
public interface CacheConstants {
    String PREFIX_KEY = "com" + StrUtil.COLON + "mwsfot" + StrUtil.COLON;
    String PREFIX_USER_KEY = PREFIX_KEY + "user";
    String PREFIX_ROOM_KEY = PREFIX_KEY + "room";
    String PREFIX_SCORE_KEY = PREFIX_KEY + "score";
    String PREFIX_GAME_KEY = PREFIX_KEY + "game";


}
