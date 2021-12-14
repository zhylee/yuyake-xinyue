package cn.yuyake.gateway.message.rpc;

import cn.yuyake.game.common.EnumMessageType;
import cn.yuyake.game.common.GameMessageHeader;
import cn.yuyake.game.common.IGameMessage;
import cn.yuyake.gateway.message.channel.AbstractGameChannelHandlerContext;

public class RPCEventContext<T> {
    // 收到的RPC请求消息的实例
    private final IGameMessage request;
    // 这个用于存储缓存的数据，因为不同的服务的数据结构是不同的，所以这里使用泛型
    private final T data;
    // 处理RPC请求消息的GameChannel的Handler上下文，用于向消息总线服务中发送消息
    private final AbstractGameChannelHandlerContext ctx;

    public RPCEventContext(T data, IGameMessage request, AbstractGameChannelHandlerContext ctx) {
        super();
        this.request = request;
        this.ctx = ctx;
        this.data = data;
    }

    // 返回相应的数据管理类
    public T getData() {
        return data;
    }

    // 发送RPC响应消息
    public void sendResponse(IGameMessage response) {
        GameMessageHeader responseHeader = response.getHeader();
        EnumMessageType messageType = responseHeader.getMessageType();
        if (messageType != EnumMessageType.RPC_RESPONSE) {
            // 进行消息类型检测，防止开发人员不小心传错消息
            throw new IllegalArgumentException(response.getClass().getName() + " 参数类型不对，不是RPC的响应数据对象");
        }
        GameMessageHeader requestHeader = request.getHeader();
        // 响应消息要到达的目标服务实例ID就是请求消息发送的服务实例ID
        responseHeader.setToServerId(requestHeader.getFromServerId());
        // 响应消息的发送服务实例ID就是请求消息要到达的目标服务实例ID
        responseHeader.setFromServerId(requestHeader.getToServerId());
        // 获取请求消息携带的唯一序号，原样返回
        responseHeader.setClientSeqId(requestHeader.getClientSeqId());
        // 客户端发送时间原样返回
        responseHeader.setClientSendTime(requestHeader.getClientSendTime());
        // 获取发送消息的角色ID
        responseHeader.setPlayerId(requestHeader.getPlayerId());
        // 设置响应消息的时间
        responseHeader.setServerSendTime(System.currentTimeMillis());
        // 响应消息不需要回调结果，这里传null
        ctx.writeRPCMessage(response, null);
    }
}
