package cn.yuyake.client.service;

import cn.yuyake.common.utils.CommonField;
import cn.yuyake.common.utils.GameHttpClient;
import cn.yuyake.http.MessageCode;
import cn.yuyake.http.request.SelectGameGatewayParam;
import cn.yuyake.http.response.GameGatewayInfoMsg;
import cn.yuyake.http.response.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class GameClientInitService {

    private Logger logger = LoggerFactory.getLogger(GameClientInitService.class);

    @Autowired
    private GameClientConfig gameClientConfig;

    // 服务启动后，自动调用这个方法
    @PostConstruct
    public void init() {
        this.selectGateway();
    }

    private void selectGateway() {
        if (gameClientConfig.isUseGameCenter()) {
            // 因为是测试环境，这里使用一些默认参数
            SelectGameGatewayParam param = new SelectGameGatewayParam();
            param.setOpenId("test_openId");
            param.setPlayerId(1);
            param.setUserId(1);
            param.setZoneId("1");
            GameGatewayInfoMsg gateGatewayMsg = this.selectGatewayInfoFromGameCenter(param);
            // 替换默认的游戏网关信息
            if (gateGatewayMsg != null) {
                gameClientConfig.setDefaultGameGatewayHost(gateGatewayMsg.getIp());
                gameClientConfig.setDefaultGameGatewayPort(gateGatewayMsg.getPort());
                gameClientConfig.setGatewayToken(gateGatewayMsg.getToken());
                gameClientConfig.setRsaPrivateKey(gateGatewayMsg.getRsaPrivateKey());
            } else {
                throw new IllegalArgumentException("从服务中心获取游戏网关信息失败，没有可使用的游戏网关信息");
            }
        }
    }

    /**
     * 从游戏服务中心获取游戏网关信息
     */
    private GameGatewayInfoMsg selectGatewayInfoFromGameCenter(SelectGameGatewayParam selectGameGatewayParam) {
        // 构建请求游戏服务中心的URI
        String uri = gameClientConfig.getGameCenterUrl()
                + CommonField.GAME_CENTER_PATH
                + MessageCode.SELECT_GAME_GATEWAY;
        String response = GameHttpClient.post(uri, selectGameGatewayParam);
        if (response == null) {
            logger.warn("从游戏服务中心[{}]获取游戏网关信息失败", uri);
            return null;
        }
        // 转换数据
        ResponseEntity<GameGatewayInfoMsg> responseEntity =
                ResponseEntity.parseObject(response, GameGatewayInfoMsg.class);
        GameGatewayInfoMsg gateGatewayMsg = responseEntity.getData();
        return gateGatewayMsg;
    }
}
