package com.example;

import android.app.Application;

/**
 * Created by ChneY on 2017/3/22.
 */

public class AppApplication extends Application {
    static AppApplication intance;
    public static boolean Debug = true;

    public static AppApplication getIntance() {
        return intance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intance = this;
    }


}
