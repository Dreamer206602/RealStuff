package com.example.ivor_hu.meizhi;

import android.app.Application;
import android.content.Context;

/**
 * Created by Ivor on 2016/2/12.
 */
public class APP extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
