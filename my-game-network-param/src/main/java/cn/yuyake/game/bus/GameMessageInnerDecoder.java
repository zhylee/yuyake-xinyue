package cn.yuyake.game.bus;

import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.GameMessagePackage;
import cn.yuyake.game.common.HeaderAttribute;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * <p>
 * 序列化发送消息并送到消息总线中
 * 反序列化消息从消息总线中接收消息
 * </p>
 */
public class GameMessageInnerDecoder {
    private final static int HEADER_FIX_LEN = 84;

    public static void sendMessage(KafkaTemplate<String, byte[]> kafkaTemplate, GameMessagePackage gameMessagePackage, String topic) {
        int initialCapacity = HEADER_FIX_LEN;
        GameMessageHeader header = gameMessagePackage.getHeader();
        // 把包头的属性类序列化为JSON
        String headerAttJson = JSON.toJSONString(header.getAttribute());
        byte[] headerAttBytes = headerAttJson.getBytes();
        initialCapacity += headerAttBytes.length;
        if (gameMessagePackage.getBody() != null) {
            initialCapacity += gameMessagePackage.getBody().length;
        }
        // 这里使用 Unpooled 创建 ByteBuf，可以直接使用 byteBuf.array() 获取 byte[]
        ByteBuf byteBuf = Unpooled.buffer(initialCapacity);
        // 依次写入包头的数据
        byteBuf.writeInt(initialCapacity);
        byteBuf.writeInt(header.getToServerId());
        byteBuf.writeInt(header.getFromServerId());
        byteBuf.writeInt(header.getClientSeqId());
        byteBuf.writeInt(header.getMessageId());
        byteBuf.writeInt(header.getServiceId());
        byteBuf.writeInt(header.getVersion());
        byteBuf.writeLong(header.getClientSendTime());
        byteBuf.writeLong(header.getServerSendTime());
        byteBuf.writeLong(header.getPlayerId());
        byteBuf.writeInt(headerAttBytes.length);
        byteBuf.writeBytes(headerAttBytes);
        byteBuf.writeInt(header.getErrorCode());
        byte[] value;
        if (gameMessagePackage.getBody() != null) { // 写入包体信息
            // 使用 byte[] 包装为 ByteBuf，减少一次 byte[] 拷贝
            ByteBuf bodyBuf = Unpooled.wrappedBuffer(gameMessagePackage.getBody());
            ByteBuf allBuf = Unpooled.wrappedBuffer(byteBuf, bodyBuf);
            // 获取消息包的最终 byte[]
            value = allBuf.array();
        } else {
            value = byteBuf.array();
        }
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, String.valueOf(header.getPlayerId()), value);
        // 向消息总线中发布消息
        kafkaTemplate.send(record);
    }

    public static GameMessagePackage readGameMessagePackage(byte[] value) {
        // 直接使用 byte[] 包装为 ByteBuf，减少一次数据复制
        ByteBuf byteBuf = Unpooled.wrappedBuffer(value);
        // 依次读取包头信息
        int messageSize = byteBuf.readInt();//依次读取包头信息
        int toServerId = byteBuf.readInt();
        int fromServerId = byteBuf.readInt();
        int clientSeqId = byteBuf.readInt();
        int messageId = byteBuf.readInt();
        int serviceId = byteBuf.readInt();
        int version = byteBuf.readInt();
        long clientSendTime = byteBuf.readLong();
        long serverSendTime = byteBuf.readLong();
        long playerId = byteBuf.readLong();
        int headerAttrLength = byteBuf.readInt();
        HeaderAttribute headerAttr = null;
        if (headerAttrLength > 0) {
            // 读取包头属性
            byte[] headerAttrBytes = new byte[headerAttrLength];
            byteBuf.readBytes(headerAttrBytes);
            String headerAttrJson = new String(headerAttrBytes);
            headerAttr = JSON.parseObject(headerAttrJson, HeaderAttribute.class);
        }
        int errorCode = byteBuf.readInt();
        byte[] body = null;
        if (byteBuf.readableBytes() > 0) {
            body = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(body);
        }
        GameMessageHeader header = new GameMessageHeader();
        // 向包头对象中添加数据
        header.setAttribute(headerAttr);
        header.setClientSendTime(clientSendTime);
        header.setClientSeqId(clientSeqId);
        header.setErrorCode(errorCode);
        header.setFromServerId(fromServerId);
        header.setMessageId(messageId);
        header.setMessageSize(messageSize);
        header.setPlayerId(playerId);
        header.setServerSendTime(serverSendTime);
        header.setServiceId(serviceId);
        header.setToServerId(toServerId);
        header.setVersion(version);
        // 创建消息对象
        GameMessagePackage gameMessagePackage = new GameMessagePackage();
        gameMessagePackage.setHeader(header);
        gameMessagePackage.setBody(body);
        return gameMessagePackage;
    }
}
