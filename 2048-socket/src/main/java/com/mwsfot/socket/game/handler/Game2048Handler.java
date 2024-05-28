package com.mwsfot.socket.game.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mwsfot.socket.constants.CacheConstants;
import com.mwsfot.socket.entity.Online2048;
import com.mwsfot.socket.entity.Online2048Result;
import com.mwsfot.socket.entity.Room;
import com.mwsfot.socket.entity.Score;
import com.mwsfot.socket.entity.User;
import com.mwsfot.socket.entity.UserPermission;
import com.mwsfot.socket.enums.GameEnum;
import com.mwsfot.socket.enums.RoomEnum;
import com.mwsfot.socket.socketIO.SocketIOSessionManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * @author MinChang
 * @description 2048游戏handler
 * @date 2024/5/24 14:40
 */
@Slf4j
@Component
public class Game2048Handler {


    private final static String GAME_NAME = "game2048";
    @Autowired
    private RedisTemplate<String, Room<Online2048>> roomRedisTemplate;
    @Autowired
    private RedisTemplate<String, User> userRedisTemplate;

    private static final Interner<String> weakInterner = Interners.newWeakInterner();
    @Autowired
    private SocketIOSessionManager sessionManager;

    @Autowired
    private SocketIOServer server;

    /**
     * 加入房间, 和Json.toJson没关系
     *
     * @param client  当前请求的client
     * @param ack     响应
     * @param message 消息
     */
    @OnEvent("joinRoom")
    public void onJointRoom(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        synchronized (weakInterner.intern(userId)) {


            String userKey =
                CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
            ValueOperations<String, User> userOps =
                userRedisTemplate.opsForValue();
            User user = userOps.get(userKey);
            if ( user == null ) {
                return;
            }
            log.info("userId = {}, sessionId = {} 加入, 房间 = {}", userId, client.getSessionId(),
                roomId);

            String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
            ValueOperations<String, Room<Online2048>> ops =
                roomRedisTemplate.opsForValue();
            Room<Online2048> room = ops.get(roomKey);
            if ( room != null ) {
                //如果正在进行中
                if ( room.getRoomStatus().equals(RoomEnum.RUNNING.getType()) ) {
                    //只向向当前请求客户端
                    client.sendEvent("running", JSONUtil.toJsonStr(room));
                    return;
                }
                //用户信息
                if ( CollectionUtil.isNotEmpty(room.getUserList()) ) {
                    //重复加入就跳过
                    Optional<UserPermission> first =
                        room.getUserPermission().stream().filter(v -> v.getUserId().equals(userId))
                            .findFirst();
                    if ( first.isPresent() ) {
                        ack.sendAckData(first.get());
                        return;
                    }
                    //房间已满
                    if ( room.getMaxUserNum() < room.getUserList().size() + 1 ) {
                        ack.sendAckData("full");
                        //只向当前请求客户端发送
                        client.sendEvent("full", Online2048Result.builder().roomId(roomId).build());
                        //此时都是房客,且加入房间成功
                    } else {
                        List<User> userList = room.getUserList();
                        userList.add(user);
                        List<UserPermission> userPermission = room.getUserPermission();
                        UserPermission raw = new UserPermission();
                        raw.setUserId(user.getUserId());
                        raw.setPermission(0);
                        userPermission.add(raw);
                        //更新缓存
                        ops.set(roomKey, room);
                        //加入房间
                        client.joinRoom(roomId);
                        //只发送权限信息
                        ack.sendAckData(raw);
                        //向整个房间发送消息
                        Online2048Result.Online2048ResultBuilder builder =
                            Online2048Result.builder();
                        builder.holder(
                            userPermission.stream().filter(v -> v.getPermission() == 1).findFirst()
                                .get().getUserId());
                        builder.unHolder(
                            userPermission.stream().filter(v -> v.getPermission() == 0).findFirst()
                                .get().getUserId());
                        client.getNamespace().getRoomOperations(roomId)
                            .sendEvent("findMate", builder.build());
                    }
                    //如果房间人数一致, 向双方发送人员信息
                    if ( room.getMaxUserNum() == room.getUserList().size() ) {
                        List<UserPermission> userPermission = room.getUserPermission();
                        //向整个房间发送消息
                        Online2048Result.Online2048ResultBuilder builder =
                            Online2048Result.builder();
                        builder.holder(
                            userPermission.stream().filter(v -> v.getPermission() == 1).findFirst()
                                .get().getUserId());
                        builder.unHolder(
                            userPermission.stream().filter(v -> v.getPermission() == 0).findFirst()
                                .get().getUserId());
                        client.getNamespace().getRoomOperations(roomId)
                            .sendEvent("findMate", builder.build());
                    }
                    //到了这里, 只可能为空, 所以必然是房主
                } else {
                    List<User> userList = room.getUserList();
                    userList.add(user);
                    List<UserPermission> userPermission = room.getUserPermission();
                    UserPermission raw = new UserPermission();
                    raw.setUserId(user.getUserId());
                    raw.setPermission(1);
                    userPermission.add(raw);
                    //更新缓存
                    ops.set(roomKey, room);
                    //向客户端发送事件
                    client.joinRoom(roomId);
                    ack.sendAckData(raw);
                }
                //到了这里说明房间为空, 需要新建一个房间, 且当前用户默认为房主
            } else {
                room = new Room<>();
                List<User> userList = new ArrayList<>();
                userList.add(user);
                room.setUserList(userList);
                room.setRoomId(roomId);
                room.setRoomName(roomId);
                room.setRoomStatus(RoomEnum.CREATED.getType());
                room.setMaxUserNum(2);
                List<UserPermission> userPermission = new ArrayList<>();
                UserPermission raw = new UserPermission();
                raw.setUserId(user.getUserId());
                raw.setPermission(1);
                userPermission.add(raw);
                //默认房主
                room.setUserPermission(userPermission);
                room.setTimestamp(0);
                room.setScoreMap(new HashMap<>());
                Online2048 online2048 = new Online2048();
                online2048.setGameId(GAME_NAME);
                online2048.setMode(1);
                online2048.setLimitTime(10);
                room.setGame(online2048);
                room.setGameStatus(GameEnum.CREATED.getType());
                //更新缓存
                ops.set(roomKey, room);
                //加入房间
                client.joinRoom(roomId);
                //发送当前权限
                ack.sendAckData(raw);
            }
        }
    }

    @OnEvent("leaveRoom")
    public void leaveRoom(SocketIOClient client, AckRequest ack, String message) {

        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");

        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 准备退出房间, 房间 = {}", userId,
            client.getSessionId(), roomId);

        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        List<User> userList = room.getUserList();
        List<UserPermission> userPermission = room.getUserPermission();
        Map<String, Integer> userPermissionMap = userPermission.stream()
            .collect(Collectors.toMap(UserPermission::getUserId, UserPermission::getPermission));
        Map<String, Score> scoreMap = room.getScoreMap();
        boolean isHolder = userPermissionMap.get(userId) == 1;
        if ( CollectionUtil.isNotEmpty(userList) ) {
            client.getNamespace().getRoomOperations(roomId).sendEvent("memberLeave",
                Online2048Result.builder().roomId(roomId).userId(userId).build());
            if ( isHolder ) {
                //然后退出所有
                client.getNamespace().getRoomOperations(roomId)
                    .sendEvent("quit", Online2048Result.builder().power(1)
                        .roomId(roomId).build());
            } else {
                client.sendEvent("quit", Online2048Result.builder().power(0)
                    .roomId(roomId).build());
            }
            //开始离开房间
            Set<String> set = userList.stream().map(User::getUserId).collect(Collectors.toSet());
            set.forEach(u -> {
                if ( !isHolder && !u.equals(userId) ) {
                    return;
                }
                SocketIOClient ioClient = sessionManager.get(u);
                if ( ioClient == null ) {
                    return;
                }
                ioClient.leaveRoom(roomId);
                //删除用户session信息
                sessionManager.removeSession(u);
                userPermission.removeIf(v -> u.equals(v.getUserId()));
                userList.removeIf(v -> u.equals(v.getUserId()));
                scoreMap.remove(u);
                //删除用户缓存
                userRedisTemplate.delete(
                    CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + u);
            });
        }

        //角色为零则解散房间
        if ( userList.size() == 0 ) {
            roomRedisTemplate.expire(roomKey, Duration.ZERO);
        } else { //否则更新配置信息
            ops.set(roomKey, room);
        }
    }

    /**
     * 修改时间设置
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("changeTime")
    public void onChangeTime(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String limitTime = obj.getStr("limitTime");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        if ( StrUtil.isBlank(limitTime) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 修改房间时间设置, 房间 = {}, 时间 = {}", userId,
            client.getSessionId(), roomId, limitTime);
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        List<UserPermission> permissions = room.getUserPermission();
        boolean match = permissions.stream().anyMatch(
            permission -> permission.getUserId().equals(userId) &&
                permission.getPermission().equals(1));
        //证明权限不够
        if ( !match ) {
            return;
        }
        client.getNamespace().getRoomOperations(roomId).sendEvent("changeT",
            Online2048Result.builder().newTime(limitTime).roomId(roomId).build());
    }

    /**
     * 修改Level设置
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("changeLevel")
    public void onChangeLevel(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String mode = obj.getStr("mode");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        if ( StrUtil.isBlank(mode) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 修改房间等级设置, 房间 = {}", userId,
            client.getSessionId(), roomId);
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        List<UserPermission> permissions = room.getUserPermission();
        boolean match = permissions.stream().anyMatch(
            permission -> permission.getUserId().equals(userId) &&
                permission.getPermission().equals(1));
        //证明权限不够
        if ( !match ) {
            return;
        }
        client.getNamespace().getRoomOperations(roomId).sendEvent("changeL",
            Online2048Result.builder().mode(mode).roomId(roomId).build());
    }

    /**
     * 修改时间设置
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("startGame")
    public void onStartGame(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 准备开始游戏, 房间 = {}", userId,
            client.getSessionId(), roomId);
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        List<UserPermission> permissions = room.getUserPermission();
        boolean match = permissions.stream().anyMatch(
            permission -> permission.getUserId().equals(userId) &&
                permission.getPermission().equals(1));
        //证明权限不够
        if ( !match ) {
            return;
        }
        client.getNamespace().getRoomOperations(roomId).sendEvent("start");
        room.setGameStatus(GameEnum.START.getType());
        room.setRoomStatus(RoomEnum.RUNNING.getType());
        ops.set(roomKey, room);
    }

    /**
     * 修改时间设置
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("syncScore")
    public void onSyncScore(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String score = obj.getStr("score");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        if ( StrUtil.isBlank(score) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 准备同步时间, 房间 = {}", userId,
            client.getSessionId(), roomId);
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        client.getNamespace().getRoomOperations(roomId).sendEvent("updateScore",
            Online2048Result.builder().userId(userId)
                .score(score).build());
    }

    /**
     * 接收消息并广播
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("send")
    public void onSend(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String sessionId = client.getSessionId().toString();
        String msg = obj.getStr("msg");
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        if ( StrUtil.isBlank(msg) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 准备发送消息, 房间 = {}", userId,
            client.getSessionId(), roomId);
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        client.getNamespace().getRoomOperations(roomId)
            .sendEvent("partnerMsg", Online2048Result.builder().userId(userId)
                .msg(msg).build());
        ack.sendAckData("ack");
    }

    /**
     * 接收消块惩罚
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("scoreChange")
    public void onScoreChange(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String score = obj.getStr("score");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        if ( StrUtil.isBlank(score) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 准备发送惩罚, 房间 = {}", userId,
            client.getSessionId(), roomId);
        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        client.getNamespace().getRoomOperations(roomId).sendEvent("score64",
            Online2048Result.builder().userId(userId)
                .score(score).build());
    }

    /**
     * TODO 待扩展,暂时搁置
     * 判断当前用户是否存在正在进行中游戏
     *
     * @param client  客户端
     * @param ack     应答
     * @param message 消息
     */
    @OnEvent("hasRunningGame")
    public void onHasRunningGame(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }

        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 当前用户有正在进行中游戏, 房间 = {}", userId,
            client.getSessionId(), roomId);
        //userId, userName, msg
        Map<String, String> param = new HashMap<>();
        param.put("userId", userId);
        param.put("room", JSONUtil.toJsonStr(room));
        client.sendEvent("continueGame", param);
        client.joinRoom(roomId);
        client.getNamespace().getRoomOperations(roomId)
            .sendEvent("reBack", JSONUtil.toJsonStr(param));
    }

    @OnEvent("punish")
    public void onPunish(SocketIOClient client, AckRequest ack, String message) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        JSONObject obj = JSONUtil.parseObj(message);
        String roomId = obj.getStr("roomId");
        Boolean isPunish = obj.getBool("isPunish");
        String sessionId = client.getSessionId().toString();
        if ( StrUtil.isBlank(userId) ) {
            return;
        }
        if ( StrUtil.isBlank(roomId) ) {
            return;
        }
        if ( isPunish == null ) {
            return;
        }
        String userKey =
            CacheConstants.PREFIX_USER_KEY + StrUtil.COLON + userId;
        ValueOperations<String, User> userOps =
            userRedisTemplate.opsForValue();
        User user = userOps.get(userKey);
        if ( user == null ) {
            return;
        }

        String roomKey = CacheConstants.PREFIX_ROOM_KEY + StrUtil.COLON + roomId;
        ValueOperations<String, Room<Online2048>> ops =
            roomRedisTemplate.opsForValue();
        Room<Online2048> room = ops.get(roomKey);
        if ( room == null ) {
            return;
        }
        log.info("userId = {}, sessionId = {} 当前用户有正在进行中游戏, 房间 = {}", userId,
            client.getSessionId(), roomId);
        client.getNamespace().getRoomOperations(roomId)
            .sendEvent("punishChange",
                Online2048Result.builder().isPunish(isPunish).roomId(roomId).build());
    }
}
