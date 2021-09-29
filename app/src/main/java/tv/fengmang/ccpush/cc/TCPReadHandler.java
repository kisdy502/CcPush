package tv.fengmang.ccpush.cc;

import android.util.Log;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import tv.fengmang.ccpush.proto.CcMessage;
import tv.fengmang.ccpush.server.ChannelContainer;

public class TCPReadHandler extends ChannelInboundHandlerAdapter {
    private static final String TAG = "TCPReadHandler";
    private CcPushManager ccPushManager;

    public TCPReadHandler(CcPushManager ccPushManager) {
        this.ccPushManager = ccPushManager;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.err.println("TCPReadHandler channelInactive()");
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.err.println("TCPReadHandler exceptionCaught()");
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CcMessage.Msg message = (CcMessage.Msg) msg;
        CcMessage.Msg.MsgType msgType = message.getHeader().getType();
        if (msgType == CcMessage.Msg.MsgType.TYPE_REPORT) {
            int statusReport = message.getHeader().getMsgStatus();
            if (statusReport == CcConfig.REPORT_SUCCESS) {
                Log.i(TAG, "消息已经被服务器收到了，可以不再发送了，message=" + message);

            }
        } else {
            // 其它消息
            // 收到消息后，立马给服务端回一条消息接收状态报告
            Log.d(TAG, "收到服务器推送过来的消息，message=" + message);
            String fromId = message.getHeader().getFromId();
            CcMessage.Msg reportMsg = CcMessageHelper.createReportMsg(fromId, message.getHeader().getId());
            ccPushManager.sendMsg(reportMsg);

        }

    }
}
