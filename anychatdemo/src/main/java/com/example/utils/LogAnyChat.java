package com.example.utils;

import android.util.Log;

import com.example.AppApplication;

/**
 * Created by ChneY on 2017/3/20.
 */

public class LogAnyChat {
    private final static String TAG = "AnyChat__AAA";
    private final static boolean Debug = AppApplication.Debug;

    public static void i(String text) {
        if (Debug)
            Log.i(TAG, "" + text);
    }

    public static void w(String text) {
        if (Debug)
            Log.w(TAG, "" + text);
    }

    public static void d(String text) {
        if (Debug)
            Log.d(TAG, "" + text);
    }

    public static void e(String text) {
        if (Debug)
            Log.e(TAG, "" + text);
    }
}
