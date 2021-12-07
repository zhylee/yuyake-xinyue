package cn.yuyake.center.service;

import cn.yuyake.common.error.GameErrorException;
import cn.yuyake.common.utils.JWTUtil;
import cn.yuyake.dao.PlayerDao;
import cn.yuyake.db.entity.Player;
import cn.yuyake.error.GameCenterError;
import cn.yuyake.http.request.SelectGameGatewayParam;
import cn.yuyake.redis.EnumRedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PlayerDao playerDao;

    private Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private boolean saveNickNameIfAbsent(String zoneId, String nickName) {
        String key = this.getNickNameRedisKey(zoneId, nickName); // 生成存储的 key
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "0");// value 先使用一个默认值
        if (result == null) {
            return false;
        }
        return result; // 如果返回 true，表示存储成功，否则表示已存在
    }

    // 统一生成 redis 存储的 key
    private String getNickNameRedisKey(String zoneId, String nickName) {
        return EnumRedisKey.PLAYER_NICKNAME.getKey(zoneId + "_" + nickName);
    }

    // 角色创建成功之后，更新昵称与 playerId 的映射
    private void updatePlayerIdForNickName(String zoneId, String nickName, long playerId) {
        String key = this.getNickNameRedisKey(zoneId, nickName);
        // 更新昵称对应的 playerId
        this.redisTemplate.opsForValue().set(key, String.valueOf(playerId));
    }

    // 创建角色方法
    public Player createPlayer(String zoneId, String nickName) {
        boolean saveNickName = this.saveNickNameIfAbsent(zoneId, nickName);
        if (!saveNickName) { // 如果存储失败，抛出错误异常
            throw new GameErrorException.Builder(GameCenterError.NICKNAME_EXIST).message(nickName).build();
        }
        long playerId = this.nextPlayerId(zoneId); // 获取一个全局playerId
        Player player = new Player();
        player.setPlayerId(playerId);
        player.setNickName(nickName);
        player.setLastLoginTime(System.currentTimeMillis());
        player.setCreateTime(player.getLastLoginTime());
        // 再次更新一下 nickName 对应的 playerId
        this.updatePlayerIdForNickName(zoneId, nickName, playerId);
        playerDao.saveOrUpdate(player, playerId);
        logger.info("创建角色成功，{}", player);
        return player;
    }

    private long nextPlayerId(String zoneId) {
        String key = EnumRedisKey.PLAYER_ID_INCR.getKey(zoneId);
        return redisTemplate.opsForValue().increment(key);
    }

    public String createToken(SelectGameGatewayParam param, String gatewayIp, String publicKey) {
        String openId = param.getOpenId();
        String zoneId = param.getZoneId();
        long userId = param.getUserId();
        long playerId = param.getPlayerId();
        String token = JWTUtil.getUserToken(openId, userId, playerId, zoneId, gatewayIp, publicKey);
        return token;
    }
}
