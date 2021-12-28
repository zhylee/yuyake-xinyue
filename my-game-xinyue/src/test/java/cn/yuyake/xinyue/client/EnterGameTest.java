package cn.yuyake.xinyue.client;

import cn.yuyake.db.entity.Player;
import cn.yuyake.db.entity.manager.PlayerManager;
import cn.yuyake.game.message.xinyue.EnterGameMsgRequest;
import cn.yuyake.game.message.xinyue.EnterGameMsgResponse;
import cn.yuyake.test.AbstractXinYueIntegrationTest;
import cn.yuyake.xinyue.XinYueGameServerMain;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@SpringBootTest(classes = XinYueGameServerMain.class)
public class EnterGameTest extends AbstractXinYueIntegrationTest<PlayerManager> {
    private long playerId;
    private Player player; // 构造数据信息
    private PlayerManager playerManager;

    @BeforeMethod // 在每个测试方法执行之前都重置数据对象
    public void init() {
        playerId = 182222788;
        player = new Player();
        player.setPlayerId(playerId);
        playerManager = new PlayerManager(player);
    }

    @Test(description = "正常进入游戏测试")
    public void enterGameOk() {
        EnterGameMsgRequest request = new EnterGameMsgRequest();
        this.sendGameMessage(playerId, playerManager, request, c -> {
            EnterGameMsgResponse response = (EnterGameMsgResponse) c;
            assertEquals(response.getHeader().getPlayerId(), playerId);
            assertEquals(response.getBodyObj().getPlayerId(), playerId);
        });
    }
}
