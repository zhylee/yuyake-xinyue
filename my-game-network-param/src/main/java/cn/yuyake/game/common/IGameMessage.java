package cn.yuyake.game.common;

public interface IGameMessage {
    GameMessageHeader getHeader();
    void setHeader(GameMessageHeader header);

    void read(byte[] body);

    byte[] body();
}
