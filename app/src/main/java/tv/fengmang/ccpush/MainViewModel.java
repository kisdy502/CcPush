package tv.fengmang.ccpush;

import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tv.fengmang.ccpush.cc.CcMessageHelper;
import tv.fengmang.ccpush.cc.CcPushManager;
import tv.fengmang.ccpush.proto.CcMessage;
import tv.fengmang.ccpush.server.CcPushServer;
import tv.fengmang.ccpush.server.ChannelContainer;
import tv.fengmang.ccpush.server.NettyChannel;
import tv.fengmang.ccpush.utils.DmpNetUtils;

public class MainViewModel extends ViewModel {

    public final ObservableField<String> msgContent = new ObservableField<>();

    public void startServer() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                CcPushServer.start();
                emitter.onNext(1);
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer result) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public void connect(View view) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                CcPushManager.getInstance().init("127.0.0.1", 55555);
                emitter.onNext(1);
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer result) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public void sendMsg(View view) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                String content = "测试消息";
                String userId = DmpNetUtils.getMacAddress(CcApp.getInstance());
                CcMessage.Msg normalMsg = CcMessageHelper.createNormalMsg(userId, content);
                CcPushManager.getInstance().sendMsg(normalMsg);
                emitter.onNext(1);
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer result) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public void pushMsg(View view) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                String content = "卧室服务器推送给客户端的消息";
                String fromId = DmpNetUtils.getMacAddress(CcApp.getInstance());
                CcMessage.Msg normalMsg = CcMessageHelper.createNormalMsg(fromId, content);
                NettyChannel nettyChannel = ChannelContainer.getInstance().getActiveChannelByUserId(fromId);
                if (nettyChannel != null) {
                    nettyChannel.getChannel().writeAndFlush(normalMsg);
                }
                emitter.onNext(1);
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer result) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
