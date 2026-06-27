package com.union_test.toutiao;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;








import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Create by hanweiwei on 11/07/2018
 */
@SuppressWarnings("unused")
public class DemoApplication extends MultiDexApplication {

    public static String PROCESS_NAME_XXXX = "process_name_xxxx";
    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        DemoApplication.context = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();





    }

    public static Context getAppContext() {
        return DemoApplication.context;
    }
































































































































    
}
