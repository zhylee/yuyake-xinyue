package cn.yuyake.client.service.handler.loginhandler;

import cn.yuyake.client.service.GameClientConfig;
import cn.yuyake.client.service.handler.GameClientChannelContext;
import cn.yuyake.client.service.handler.codec.DecodeHandler;
import cn.yuyake.client.service.handler.codec.EncodeHandler;
import cn.yuyake.common.utils.RSAUtils;
import cn.yuyake.game.message.ConfirmMsgResponse;
import cn.yuyake.game.messagedispatcher.GameMessageHandler;
import cn.yuyake.game.messagedispatcher.GameMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;

@GameMessageHandler
public class SystemMessageHandler {

    @Autowired
    private GameClientConfig gameClientConfig;

    private final Logger logger = LoggerFactory.getLogger(SystemMessageHandler.class);

    @GameMessageMapping(ConfirmMsgResponse.class)
    public void confirmResponse(ConfirmMsgResponse response, GameClientChannelContext ctx) {
        String encryptAesKey = response.getBodyObj().getSecretKey();
        byte[] content = Base64Utils.decodeFromString(encryptAesKey);
        try {
            byte[] privateKey = Base64Utils.decodeFromString(gameClientConfig.getRsaPrivateKey());
            byte[] valueBytes = RSAUtils.decryptByPrivateKey(content, privateKey);
            // 得到明文的aes加密密钥
            String value = new String(valueBytes);
            // 把密钥给解码Handler
            DecodeHandler decodeHandler = (DecodeHandler) ctx.getChannel().pipeline().get("DecodeHandler");
            decodeHandler.setAesSecretKey(value);
            // 把密钥给编码Handler
            EncodeHandler encodeHandler = (EncodeHandler) ctx.getChannel().pipeline().get("EncodeHandler");
            encodeHandler.setAesSecretKey(value);

            logger.debug("连接认证成功,channelId:{}", ctx.getChannel().id().asShortText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
