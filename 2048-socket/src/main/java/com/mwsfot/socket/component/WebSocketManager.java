/*
package com.mwsfot.socket.component;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mwsfot.socket.constants.CacheConstants;
import com.mwsfot.socket.constants.SubscribeConstants;
import com.mwsfot.socket.entity.Online2048;
import com.mwsfot.socket.entity.Room;
import com.mwsfot.socket.entity.Score;
import com.mwsfot.socket.entity.User;
import com.mwsfot.socket.enums.GameEnum;
import com.mwsfot.socket.enums.RoomEnum;
import com.mwsfot.socket.enums.SocketTypeEnum;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

*/
/**
 * @author Administrator
 *//*

@Slf4j
@Component
public class WebSocketManager {

    @Autowired
    private RedisTemplate<String, User> userRedisTemplate;

    @Autowired
    private RedisTemplate<String, Room<Online2048>> roomRedisTemplate;


    */
/**
     * 新建连接(目前实现新链接不会挤掉旧链接)
     *
     * @param userId  用户标识
     * @param session session信息
     *//*

    public void add(String userId, Session session) {
        if ( StrUtil.isBlank(userId) || session == null ) {
            return;
        }
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        User user = ops.get(userKey);
        //如果不存在,就新增一个进去
        if ( user == null ) {
            SessionManager.addSession(userId, session);
            ops.set(userKey, User.builder().userId(userId).sessionId(session.getId()).build());
            //存在但session不一致 TODO 简单逻辑, 暂时不考虑复杂情况
        }
    }


    */
/**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户实体
     *//*

    public User getUserById(String userId) {
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        return ops.get(userKey);
    }

    */
/**
     * 删除用户
     *
     * @param userId 用户ID
     *//*

    public void remove(String userId) {
        if ( userId == null ) {
            return;
        }
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        if ( Boolean.TRUE.equals(userRedisTemplate.hasKey(userKey)) ) {
            userRedisTemplate.expire(userKey, 0, TimeUnit.SECONDS);
            SessionManager.removeSession(userId);
        }
    }

    */
/**
     * 客户端发送心跳包,为空则尝试关闭
     *//*

    public void ping(Session session, String userId) {
        if ( userId == null ) {
            return;
        }
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        //不考虑session不一致情况
        if ( Boolean.TRUE.equals(userRedisTemplate.hasKey(userKey)) ) {
            ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
            User user = ops.get(userKey);
            if ( user == null ) {
                closeSession(session, userId);
                return;
            }
            String sessionId = user.getSessionId();
            if ( StrUtil.isBlank(sessionId) || !sessionId.equals(session.getId()) ) {
                user.setSessionId(session.getId());
                ops.set(userKey, user);
            }
            sendMessage(session, "pong");
        } else {
            closeSession(session, userId);
        }
    }

    */
/**
     * 拒绝客户端的链接请求
     *
     * @param session session信息
     * @param userId  用户ID
     *//*

    void closeSession(Session session, String userId) {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("session断开异常{}", userId);
        }
    }


    */
/**
     * 获取所有连接用户信息
     *//*

    public Map<String, User> all() {
        String userKey = CacheConstants.PREFIX_USER_KEY;
        Set<String> keys = userRedisTemplate.keys(userKey);
        if ( keys == null ) {
            return MapUtil.empty();
        }
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        List<User> users = ops.multiGet(keys);
        if ( users == null ) {
            return MapUtil.empty();
        }
        return users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
    }

    */
/**
     * 服务端发送消息
     *//*

    private void sendMessage(Session session, Object msg) {
        try {
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    */
/**
     * 服务端发送数据(批量用户)
     *//*

    public void dispatchBatch(SubscribeObj obj) {
        dispatchFilter(obj, null);
    }

    */
/**
     * 服务端发送数据(带过滤条件的批量用户)
     *
     * @param obj    消息体
     * @param filter 用户过滤条件.只给这部分用户发送数据
     *//*

    public void dispatchFilter(SubscribeObj obj, Predicate<User> filter) {
        Collection<User> users = all().values();
        if ( filter != null ) {
            users = users.stream().filter(filter).collect(Collectors.toList());
        }
        //给剩余指定用户发送消息
        users.forEach(user -> sendMessage(SessionManager.getSession(user.getUserId()), obj));
    }


    */
/**
     * 服务端发送数据(给单个用户)
     *//*

    public void sendDataToUser(String userId, Object msg) {
        Session session = SessionManager.getSession(userId);
        log.info("推送消息：" + msg.toString());
        if ( session == null ) {
            log.info("消息发送失败，用户" + userId + "未上线");
            return;
        }
        sendMessage(session, msg);
    }

    */
/**
     * 向房间用户广播消息
     *
     * @param roomId 房间ID
     * @param msg    消息
     *//*

    public void sendDataToRoom(String roomId, Object msg) {
        ValueOperations<String, Room<Online2048>> opsRoom = roomRedisTemplate.opsForValue();
        Room<Online2048> room = opsRoom.get(roomId);
        if ( room == null ) {
            return;
        }
        List<User> userList = room.getUserList();
        userList.forEach(user -> sendMessage(SessionManager.getSession(user.getUserId()), msg));
    }

    */
/**
     * 客户端订阅消息
     *//*

    public void subscribe(SubscribeObj obj, String userId) {
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        User user = ops.get(userKey);
        if ( user == null ) {
            log.info("消息订阅失败，用户" + userId + "未上线");
            return;
        }
        Map<String, SubscribeObj> subscription = user.getSubscription();
        //SubscribeObj subscribeObj = subscription.get(obj.getType());
        subscription.put(obj.getType(), obj);
        ops.set(userKey, user);
        //TODO data 后续需要再处理
    }

    */
/**
     * 取消订阅
     *//*

    public void unSubscribe(SubscribeObj obj, String userId) {
        String userKey = CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> ops = userRedisTemplate.opsForValue();
        User user = ops.get(userKey);
        if ( user == null ) {
            log.info("消息订阅失败，用户" + userId + "未上线");
            return;
        }
        Map<String, SubscribeObj> subscription = user.getSubscription();
        subscription.remove(obj.getType());
        ops.set(userKey, user);
    }


    */
/**
     * 发布消息
     *
     * @param subscribeObj 数据
     * @param userId       请求的用户ID
     *//*

    public void publish(SubscribeObj subscribeObj, String userId) {
        String socketType = subscribeObj.getSocketType();
        String action = subscribeObj.getAction();
        JSONObject data = JSONUtil.parseObj(subscribeObj.getData());
        String roomId = data.getStr("roomId");
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        switch (SocketTypeEnum.getEnum(socketType)) {
            case GAME:
                if ( room == null ) {
                    return;
                }
                //开始游戏
                if ( "startGame".equals(action) ) {
                    if ( room.getUserList().size() != room.getMaxUserNum() ) {
                        Map<String, String> params = new HashMap<>();
                        params.put("message", "房间人数不满足,开始游戏失败");
                        sendDataToRoom(roomId,
                            SubscribeObj.builder().socketType(SocketTypeEnum.GAME.getType())
                                .action("warning")
                                .data(params).build());
                        return;
                    }
                    room.setRoomStatus(RoomEnum.RUNNING.getType());
                    room.setGameStatus(GameEnum.START.getType());
                    ops.set(roomKey, room);
                    sendDataToRoom(roomId, SubscribeObj.builder()
                        .socketType(SocketTypeEnum.GAME.getType())
                        .action("startGame")
                        .data(room).build());
                }
                //房主修改时间
                if ( "changeTime".equals(action) ) {
                    if ( room.getRoomStatus().equals(RoomEnum.RUNNING.getType()) ) {
                        Map<String, String> params = new HashMap<>();
                        params.put("message", "正在进行中, 修改失败");
                        sendDataToRoom(roomId,
                            SubscribeObj.builder().socketType(SocketTypeEnum.GAME.getType())
                                .action("warning")
                                .data(params).build());
                        return;
                    }
                    Integer permission = room.getUserPermission().get(userId);
                    if ( permission == 0 ) {
                        Map<String, String> params = new HashMap<>();
                        params.put("message", "无权限修改");
                        sendDataToRoom(roomId,
                            SubscribeObj.builder().socketType(SocketTypeEnum.GAME.getType())
                                .action("warning")
                                .data(params).build());
                        return;
                    }
                    Online2048 game = room.getGame();
                    game.setLimitTime(data.get("newTime", Integer.class));

                    ops.set(roomKey, room);
                    sendDataToRoom(roomId, SubscribeObj.builder()
                        .socketType(SocketTypeEnum.GAME.getType())
                        .action(action)
                        .type(SubscribeConstants.PUBLISH)
                        .data(room).build());

                }
                //修改等级
                if ( "changeLevel".equals(action) ) {
                    if ( room.getRoomStatus().equals(RoomEnum.RUNNING.getType()) ) {
                        Map<String, String> params = new HashMap<>();
                        params.put("message", "正在进行中, 修改失败");
                        sendDataToRoom(roomId,
                            SubscribeObj.builder().socketType(SocketTypeEnum.GAME.getType())
                                .action("warning")
                                .data(params).build());
                        return;
                    }
                    Integer permission = room.getUserPermission().get(userId);
                    if ( permission == 0 ) {
                        Map<String, String> params = new HashMap<>();
                        params.put("message", "无权限修改");
                        sendDataToRoom(roomId,
                            SubscribeObj.builder().socketType(SocketTypeEnum.GAME.getType())
                                .action("warning")
                                .data(params).build());
                        return;
                    }
                    Online2048 game = (Online2048) room.getGame();
                    game.setMode(data.get("mode", Integer.class));
                    ops.set(roomKey, room);

                    sendDataToRoom(roomId, SubscribeObj.builder()
                        .socketType(SocketTypeEnum.GAME.getType())
                        .action(action)
                        .type(SubscribeConstants.PUBLISH)
                        .data(room).build());
                }
                //游戏结束
                if ( "gameover".equals(action) ) {

                }
                break;
            case ROOM:
                //加入房间
                if ( "joinRoom".equals(action) ) {
                    if ( room != null ) {
                        //如果正在进行中
                        if ( room.getRoomStatus().equals(RoomEnum.RUNNING.getType()) ) {
                            sendDataToUser(userId,
                                SubscribeObj.builder()
                                    .socketType(SocketTypeEnum.ROOM.getType())
                                    .action("running")
                                    .type(SubscribeConstants.PUBLISH)
                                    .data(room).build());
                            return;
                        }

                        //用户信息
                        if ( CollectionUtil.isNotEmpty(room.getUserList()) ) {
                            //房间已满
                            if ( room.getMaxUserNum() < room.getUserList().size() + 1 ) {
                                sendDataToUser(userId,
                                    SubscribeObj.builder()
                                        .socketType(SocketTypeEnum.ROOM.getType())
                                        .action("full")
                                        .type(SubscribeConstants.PUBLISH)
                                        .data(room).build());
                                return;
                                //此时都是房客,且加入房间成功
                            } else {
                                User user = getUserById(userId);
                                List<User> userList = room.getUserList();
                                userList.add(user);
                                Map<String, Integer> userPermission = room.getUserPermission();
                                userPermission.put(userId, 0);
                                //更新缓存
                                ops.set(roomKey, room);
                                sendDataToUser(userId,
                                    SubscribeObj.builder()
                                        .socketType(SocketTypeEnum.ROOM.getType())
                                        .action("join_success")
                                        .type(SubscribeConstants.PUBLISH)
                                        .data(room).build());

                                return;
                            }
                            //到了这里, 只可能为空, 所以必然是房主
                        } else {
                            User user = getUserById(userId);
                            List<User> userList = room.getUserList();
                            userList.add(user);
                            Map<String, Integer> userPermission = room.getUserPermission();
                            userPermission.put(userId, 1);
                            //更新缓存
                            ops.set(roomKey, room);

                            sendDataToUser(userId,
                                SubscribeObj.builder()
                                    .socketType(SocketTypeEnum.ROOM.getType())
                                    .action("join_success")
                                    .type(SubscribeConstants.PUBLISH)
                                    .data(room).build());
                            return;
                        }
                        //到了这里说明房间为空, 需要新建一个房间, 且当前用户默认为房主
                    } else {
                        room = new Room<>();
                        User user = getUserById(userId);
                        room.setUserList(Arrays.asList(user));
                        room.setRoomId(roomId);
                        room.setRoomName(roomId);
                        room.setRoomStatus(RoomEnum.CREATED.getType());
                        room.setMaxUserNum(2);
                        Map<String, Integer> userPermission = new HashMap<>();
                        userPermission.put(userId, 1);
                        //默认房主
                        room.setUserPermission(userPermission);
                        room.setTimestamp(0);
                        room.setScoreMap(new HashMap<>());
                        Online2048 online2048 = new Online2048();
                        online2048.setGameId("online2048");
                        online2048.setMode(1);
                        online2048.setLimitTime(10);
                        room.setGame(online2048);
                        room.setGameStatus(GameEnum.CREATED.getType());

                        sendDataToUser(userId,
                            SubscribeObj.builder()
                                .socketType(SocketTypeEnum.ROOM.getType())
                                .action("join_success")
                                .type(SubscribeConstants.PUBLISH)
                                .data(room).build());
                        //更新缓存
                        ops.set(roomKey, room);
                    }
                }
                //发送消息
                if ( "send".equals(action) ) {
                    sendDataToRoom(roomId, data.get("msg"));
                }
                //离开房间
                if ( "leaveRoom".equals(action) ) {
                    if ( room == null ) {
                        return;
                    }
                    List<User> userList = room.getUserList();
                    Map<String, Integer> userPermission = room.getUserPermission();
                    Map<String, Score> scoreMap = room.getScoreMap();

                    userList.removeIf(user -> user.getUserId().equals(userId));
                    userPermission.remove(userId);
                    scoreMap.remove(userId);
                    //角色为零则解散房间
                    ops.set(roomKey, room, userList.size() == 0 ? 0 : -1, TimeUnit.SECONDS);
                }
                break;
            case USER:
                break;
            case SCORE:
                //惩罚
                if ( "scoreChange".equals(action) ) {

                }
                //同步分数
                if ( "newscore".equals(action) ) {

                }
                break;
            default:
        }
    }
}
*/
