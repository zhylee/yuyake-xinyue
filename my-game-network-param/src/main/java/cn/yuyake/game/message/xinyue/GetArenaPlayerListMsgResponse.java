package cn.yuyake.game.message.xinyue;

import cn.yuyake.game.common.AbstractJsonGameMessage;
import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageMetadata;
import cn.yuyake.game.message.xinyue.GetArenaPlayerListMsgResponse.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GameMessageMetadata(messageId = 203, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetArenaPlayerListMsgResponse extends AbstractJsonGameMessage<ResponseBody> {
    public static class ResponseBody {
        private List<ArenaPlayer> arenaPlayers;

        public List<ArenaPlayer> getArenaPlayers() {
            return arenaPlayers;
        }

        public void setArenaPlayers(List<ArenaPlayer> arenaPlayers) {
            this.arenaPlayers = arenaPlayers;
        }
    }

    public static class ArenaPlayer {
        private long playerId;
        private String nickName;
        private Map<String, String> heroes = new HashMap<>();

        public long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(long playerId) {
            this.playerId = playerId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public Map<String, String> getHeroes() {
            return heroes;
        }

        public void setHeroes(Map<String, String> heroes) {
            this.heroes = heroes;
        }
    }

    @Override
    protected Class<ResponseBody> getBodyObjClass() {
        return ResponseBody.class;
    }
}