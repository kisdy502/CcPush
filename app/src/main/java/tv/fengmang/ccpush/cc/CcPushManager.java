package tv.fengmang.ccpush.cc;


import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import tv.fengmang.ccpush.CcApp;
import tv.fengmang.ccpush.proto.CcMessage;
import tv.fengmang.ccpush.utils.DmpNetUtils;

public class CcPushManager {

    private static final String TAG = "CcPushManager";
    private String ip;
    private int port;
    private Channel channel;
    private int appStatus;

    private Bootstrap bootstrap;

    public void init(String ip, int port) {
        this.ip = ip;
        this.port = port;

        connect();
    }

    public void connect() {
        EventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);// TCP会自动发送一个活动探测数据报文
        bootstrap.option(ChannelOption.TCP_NODELAY, true);// 设置禁用nagle算法
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CcConfig.ConnectTimeOutMs); //连接超时
        bootstrap.handler(new TCPChannelInitializerHandler(this));// 设置初始化Channel

        try {
            channel = bootstrap.connect(ip, port).sync().channel();
            if (channel != null) {
                handShake();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handShake() {
        String userId = DmpNetUtils.getMacAddress(CcApp.getInstance());
        String token = "token_" + userId;
        CcMessage.Msg msg = CcMessageHelper.createHandshakeMsg(userId, token);
        sendMsg(msg);

    }

    public void disconnect() {
        try {
            closeChannel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (bootstrap != null) {
                bootstrap.group().shutdownGracefully();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        channel = null;
        bootstrap = null;
    }

    private void closeChannel() {
        try {
            if (channel != null) {
                try {
                    removeHandler(HeartbeatHandler.class.getSimpleName());
                    removeHandler(TCPReadHandler.class.getSimpleName());
                    removeHandler(IdleStateHandler.class.getSimpleName());
                } finally {
                    try {
                        channel.close();
                    } catch (Exception ex) {
                    }
                    try {
                        channel.eventLoop().shutdownGracefully();
                    } catch (Exception ex) {
                    }

                    channel = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("关闭channel出错，reason:" + ex.getMessage());
        }
    }

    private void removeHandler(String handlerName) {
        try {
            if (channel.pipeline().get(handlerName) != null) {
                channel.pipeline().remove(handlerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("移除handler失败，handlerName=" + handlerName);
        }
    }


    public void sendMsg(CcMessage.Msg msg) {
        try {
            channel.writeAndFlush(msg);
        } catch (Exception ex) {
            Log.e(TAG, "发送消息失败，reason:" + ex.getMessage() + "\tmessage=" + msg);
        }
    }

    public void addHeartBeatHandler() {
        if (channel == null || !channel.isActive() || channel.pipeline() == null) {
            return;
        }

        try {
            // 之前存在的读写超时handler，先移除掉，再重新添加
            if (channel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(IdleStateHandler.class.getSimpleName());
            }
            // 3次心跳没响应，代表连接已断开
            channel.pipeline().addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    CcConfig.heartbeatInterval * 3, CcConfig.heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            // 重新添加HeartbeatHandler
            if (channel.pipeline().get(HeartbeatHandler.class.getSimpleName()) != null) {
                channel.pipeline().remove(HeartbeatHandler.class.getSimpleName());
            }
            if (channel.pipeline().get(TCPReadHandler.class.getSimpleName()) != null) {
                channel.pipeline().addBefore(TCPReadHandler.class.getSimpleName(), HeartbeatHandler.class.getSimpleName(),
                        new HeartbeatHandler(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("添加心跳消息管理handler失败，reason：" + e.getMessage());
        }
    }


    private final static class Holder {
        private final static CcPushManager manager = new CcPushManager();
    }

    public static CcPushManager getInstance() {
        return Holder.manager;
    }
}
