package cn.yuyake.game.common;

public abstract class AbstractGameMessage implements IGameMessage {
    private GameMessageHeader header;
    private byte[] body;

    public AbstractGameMessage() {
        GameMessageMetadata gameMessageMetadata = this.getClass().getAnnotation(GameMessageMetadata.class);
        if (gameMessageMetadata == null) {
            throw new IllegalArgumentException("消息没有添加元数据注解：" + this.getClass().getName());
        }
        header = new GameMessageHeader();
        header.setMessageId(gameMessageMetadata.messageId());
        header.setServiceId(gameMessageMetadata.serviceId());
    }

    @Override
    public GameMessageHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(GameMessageHeader header) {
        this.header = header;
    }

    @Override
    public void read(byte[] body) {
        if (body != null) {
            // 如果不为 null，才反序列化，这样不用考虑为 null 的情况，防止忘记判断
            this.decode(body);
        }
    }

    @Override
    public byte[] body() {
        if (body == null) {
            // 有可能会复用 body，所以如果不为空才序列化
            if (!this.isBodyMsgNull()) {
                // 如果内容不为 null，再去序列化，这样子类实现的时候，不需要考虑 null 的问题了
                body = this.encode();
                if (body == null) {
                    // 检测是否返回的空，防止开发者默认返回 null
                    throw new IllegalArgumentException("消息序列化之后的值为null：" + this.getClass().getName());
                }
            }
        }
        return body;
    }

    // 这些方法由子类自己实现，因为每个子类的这些行为是不一样的。

    protected abstract byte[] encode(); // 子类具体实现包体的编码操作

    protected abstract void decode(byte[] body); // 子类具体实现包体的解码操作

    protected abstract boolean isBodyMsgNull(); // 子类判断包体是否为 null
}
