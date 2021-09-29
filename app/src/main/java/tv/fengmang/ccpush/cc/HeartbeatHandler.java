package tv.fengmang.ccpush.cc;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "HeartbeatHandler";
    private CcPushManager ccPushManager;

    public HeartbeatHandler(CcPushManager ccPushManager) {
        this.ccPushManager = ccPushManager;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE: {
                    // 规定时间内没收到服务端心跳包响应，进行重连操作
                    Log.d(TAG, "服务器规定时间内无心跳响应回复！");
                    break;
                }

                case WRITER_IDLE: {
                    // 规定时间内没向服务端发送心跳包，即发送一个心跳包
                    Log.d(TAG, "客户端写空闲了，是时候发个心跳给服务器了！");
                    break;
                }
            }
        }
    }
}
