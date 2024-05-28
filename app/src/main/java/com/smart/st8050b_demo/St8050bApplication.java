package com.smart.st8050b_demo;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2018/2/1.
 */

public class St8050bApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
    public static Context getContext() {
        return context;
    }
}
