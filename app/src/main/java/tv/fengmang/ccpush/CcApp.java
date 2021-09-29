package tv.fengmang.ccpush;

import android.app.Application;

public class CcApp extends Application {

    private static CcApp instance;

    public static CcApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
