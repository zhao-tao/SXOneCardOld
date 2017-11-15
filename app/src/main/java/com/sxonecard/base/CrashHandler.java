package com.sxonecard.base;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sxonecard.CardApplication;
import com.sxonecard.http.HttpDataListener;
import com.sxonecard.http.HttpDataSubscriber;
import com.sxonecard.http.HttpRequestProxy;
import com.sxonecard.ui.CardActivity;
import com.sxonecard.util.PackageUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HeQiang on 2017/8/2.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static void upload(String type,String msg,Context context){
        HttpDataListener listener = new HttpDataListener<String>() {
            @Override
            public void onNext(String o) {
                Log.d("test","ok");
            }

            @Override
            public void onError(Context context, int code, String msg) {
                super.onError(context, code, msg);
            }
        };
        Map<String,String> param = new HashMap<>();
        param.put("error",type);
        param.put("des",msg);
        param.put("imeiid", CardApplication.IMEI);
        param.put("version", ""+PackageUtil.getVersion(context));
        HttpRequestProxy.getInstance().uploadLog(new HttpDataSubscriber(listener,
                context, false),param);
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        String msg = result.toString();
        Log.d("Exception",msg);
        upload("error",msg,mContext);
        Intent intent = new Intent(mContext, CardActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
