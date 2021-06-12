package cn.yuyake.common.utils;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JWTUtil {

    private final static String TOKEN_SECRET = "game_token#$%Abc";
    private final static long TOKEN_EXPIRE = 86400000L * 7; // TOKEN有效期 七天

    public static String getUserToken(String openId, long userId) {
        return getUserToken(openId, userId, 0, "-1");
    }

    private static String getUserToken(String openId, long userId, long playerId, String serverId, String... params) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;//使用对称加密算法生成签名
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        TokenBody tokenBody = new TokenBody();
        tokenBody.setOpenId(openId);
        tokenBody.setPlayerId(playerId);
        tokenBody.setUserId(userId);
        tokenBody.setServerId(serverId);
        tokenBody.setParam(params);
        String subject = JSON.toJSONString(tokenBody);
        JwtBuilder builder = Jwts.builder().setId(String.valueOf(nowMillis)).setIssuedAt(now).setSubject(subject).signWith(signatureAlgorithm, TOKEN_SECRET);
        long expMillis = nowMillis + TOKEN_EXPIRE;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
        return builder.compact();
    }

    public static class TokenBody {
        private String openId;
        private long userId;
        private long playerId;
        private String serverId = "1";
        private String[] param;//其它的额外参数

        public String[] getParam() {
            return param;
        }

        public void setParam(String[] param) {
            this.param = param;
        }

        public String getServerId() {
            return serverId;
        }

        public void setServerId(String serverId) {
            this.serverId = serverId;
        }

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(long playerId) {
            this.playerId = playerId;
        }

    }
}
