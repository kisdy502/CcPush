package tv.fengmang.ccpush.cc;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import tv.fengmang.ccpush.proto.CcMessage;

public class HeartbeatRespHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "HeartbeatRespHandler";
    private CcPushManager ccPushManager;

    public HeartbeatRespHandler(CcPushManager ccPushManager) {
        this.ccPushManager = ccPushManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CcMessage.Msg heartbeatRespMsg = (CcMessage.Msg) msg;
        if (heartbeatRespMsg.getHeader().getType() == CcMessage.Msg.MsgType.TYPE_HEART_BEAT) {
            Log.d(TAG, "收到服务端心跳响应消息，message=" + heartbeatRespMsg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
