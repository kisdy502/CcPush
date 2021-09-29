package tv.fengmang.ccpush.cc;

import com.alibaba.fastjson.JSONObject;

import java.util.UUID;

import tv.fengmang.ccpush.proto.CcMessage;

public class CcMessageHelper {

    public static CcMessage.Msg createHeartBeatMsg(String userId) {
        CcMessage.Msg.Builder builder = CcMessage.Msg.newBuilder();
        CcMessage.Header.Builder headBuilder = CcMessage.Header.newBuilder();
        headBuilder.setId(UUID.randomUUID().toString());
        headBuilder.setType(CcMessage.Msg.MsgType.TYPE_HEART_BEAT);
        headBuilder.setFromId(userId);
        headBuilder.setMsgTs(System.currentTimeMillis());
        builder.setHeader(headBuilder.build());

        return builder.build();
    }

    public static CcMessage.Msg createHandshakeMsg(String userId, String token) {
        CcMessage.Msg.Builder builder = CcMessage.Msg.newBuilder();
        CcMessage.Header.Builder headBuilder = CcMessage.Header.newBuilder();
        headBuilder.setId(UUID.randomUUID().toString());
        headBuilder.setType(CcMessage.Msg.MsgType.TYPE_HAND_SHARK);
        headBuilder.setFromId(userId);
        headBuilder.setMsgTs(System.currentTimeMillis());

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("token", token);
        headBuilder.setExtend(jsonObj.toString());
        builder.setHeader(headBuilder.build());

        return builder.build();
    }

    public static CcMessage.Msg createReportMsg(String userId, String msgId) {
        CcMessage.Msg.Builder builder = CcMessage.Msg.newBuilder();
        CcMessage.Header.Builder headBuilder = CcMessage.Header.newBuilder();
        headBuilder.setId(msgId);
        headBuilder.setType(CcMessage.Msg.MsgType.TYPE_REPORT);
        headBuilder.setFromId(userId);
        headBuilder.setMsgStatus(CcConfig.REPORT_SUCCESS);
        headBuilder.setMsgTs(System.currentTimeMillis());
        builder.setHeader(headBuilder.build());

        return builder.build();
    }

    public static CcMessage.Msg createNormalMsg(String userId, String content) {
        CcMessage.Msg.Builder builder = CcMessage.Msg.newBuilder();
        CcMessage.Header.Builder headBuilder = CcMessage.Header.newBuilder();
        CcMessage.Content.Builder contentBuilder = CcMessage.Content.newBuilder();
        headBuilder.setId(UUID.randomUUID().toString());
        headBuilder.setType(CcMessage.Msg.MsgType.TYPE_PUSH);
        headBuilder.setContentType(CcMessage.Msg.ContentType.CONTENT_TYPE_TEXT);
        headBuilder.setFromId(userId);
        headBuilder.setMsgTs(System.currentTimeMillis());
        builder.setHeader(headBuilder.build());

        contentBuilder.setBody(content);
        contentBuilder.setLength(content.length());
        builder.setContent(contentBuilder.build());
        return builder.build();
    }
}
