package cn.yuyake.game.common;

public class GameMessagePackage {

    private GameMessageHeader header;
    private byte[] body;

    public GameMessageHeader getHeader() {
        return header;
    }

    public void setHeader(GameMessageHeader header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
