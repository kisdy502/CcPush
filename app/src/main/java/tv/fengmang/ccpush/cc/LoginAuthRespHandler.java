package tv.fengmang.ccpush.cc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import tv.fengmang.ccpush.CcApp;
import tv.fengmang.ccpush.proto.CcMessage;
import tv.fengmang.ccpush.utils.DmpNetUtils;

public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {
    private CcPushManager ccPushManager;

    public LoginAuthRespHandler(CcPushManager ccPushManager) {
        this.ccPushManager = ccPushManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        CcMessage.Msg realMsg = (CcMessage.Msg) msg;
        if (realMsg.getHeader().getType() == CcMessage.Msg.MsgType.TYPE_HAND_SHARK) {
            JSONObject jsonObj = JSON.parseObject(realMsg.getHeader().getExtend());
            int status = jsonObj.getIntValue("status");
            if (status == CcConfig.HANDSHAKE_SUCCESS) {
                CcMessage.Msg heartBeatMsg = CcMessageHelper.createHeartBeatMsg(DmpNetUtils.getMacAddress(CcApp.getInstance()));
                ccPushManager.sendMsg(heartBeatMsg);
                ccPushManager.addHeartBeatHandler();
            }
        }
    }
}
