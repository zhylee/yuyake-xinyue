package cn.yuyake.client.common;

import cn.yuyake.http.response.GameGatewayInfoMsg;
import org.springframework.stereotype.Service;

@Service
public class ClientPlayerInfo {

    private String userName;
    private String password;
    private long playerId;
    private String token;
    private long userId;
    private GameGatewayInfoMsg gameGatewayInfoMsg;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public GameGatewayInfoMsg getGameGatewayInfoMsg() {
        return gameGatewayInfoMsg;
    }

    public void setGameGatewayInfoMsg(GameGatewayInfoMsg gameGatewayInfoMsg) {
        this.gameGatewayInfoMsg = gameGatewayInfoMsg;
    }
}
