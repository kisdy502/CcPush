package tv.fengmang.ccpush.server;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import tv.fengmang.ccpush.cc.CcConfig;
import tv.fengmang.ccpush.cc.CcMessageHelper;
import tv.fengmang.ccpush.proto.CcMessage;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = ServerHandler.class.getSimpleName();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.d(TAG, "ServerHandler channelActive()" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.d(TAG, "ServerHandler channelInactive()");
        // 用户断开连接后，移除channel
        ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.d(TAG, "ServerHandler exceptionCaught()" + cause.getMessage());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        Log.d(TAG, "ServerHandler userEventTriggered()");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CcMessage.Msg message = (CcMessage.Msg) msg;
        Log.d(TAG, "收到来自客户端的消息：" + message);
        CcMessage.Msg.MsgType msgType = message.getHeader().getType();
        switch (msgType.getNumber()) {
            // 握手消息
            case CcMessage.Msg.MsgType.TYPE_HAND_SHARK_VALUE: {
                String fromId = message.getHeader().getFromId();
                JSONObject jsonObj = JSON.parseObject(message.getHeader().getExtend());
                String token = jsonObj.getString("token");
                JSONObject resp = new JSONObject();
                if (token.equals("token_" + fromId)) {
                    resp.put("status", CcConfig.HANDSHAKE_SUCCESS);   // 握手成功后，保存用户通道
                    ChannelContainer.getInstance().saveChannel(new NettyChannel(fromId, ctx.channel()));
                } else {
                    resp.put("status", CcConfig.HANDSHAKE_FAILED);
                    ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());
                }

                message = message.toBuilder().setHeader(message.getHeader().toBuilder()
                        .setExtend(resp.toString()).build()).build();
                NettyChannel nettyChannel = ChannelContainer.getInstance().getActiveChannelByUserId(fromId);
                if (nettyChannel != null) {
                    nettyChannel.getChannel().writeAndFlush(message);
                }
                break;
            }

            // 心跳消息
            case CcMessage.Msg.MsgType.TYPE_HEART_BEAT_VALUE: {
                // 收到心跳消息，原样返回
                String fromId = message.getHeader().getFromId();
                ChannelContainer.getInstance().getActiveChannelByUserId(fromId).getChannel().writeAndFlush(message);
                break;
            }
            case CcMessage.Msg.MsgType.TYPE_REPORT_VALUE: {
                Log.d(TAG, "服务器推送的消息已经被客户端收到了，可以不再推送了！");
                break;
            }

            case CcMessage.Msg.MsgType.TYPE_PUSH_VALUE: {
                String fromId = message.getHeader().getFromId();
                CcMessage.Msg reportMsg = CcMessageHelper.createReportMsg(fromId, message.getHeader().getId());
                ChannelContainer.getInstance().getActiveChannelByUserId(fromId).getChannel().writeAndFlush(reportMsg);
                break;
            }

            default:
                break;
        }
    }
}
