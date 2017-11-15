package com.sxonecard.http.heart;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.sxonecard.http.DateUtil;
import com.sxonecard.http.HttpDataListener;
import com.sxonecard.http.HttpDataSubscriber;
import com.sxonecard.http.HttpRequestProxy;
import com.sxonecard.http.bean.AlipayBean;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/4/29.
 */

public class PhpHeart4Timer extends TimerTask {
    private static PhpHeart4Timer instance;

//    private Handler mChildHandler;

    private Context context;


    @Override
    public void run() {
//        HttpDataListener listener = new HttpDataListener<AlipayBean>(){
//            @Override
//            public void onNext(AlipayBean o) {
//                System.out.println("aaaaaaaaaa");
//            }
//            @Override
//            public void onError(Context context, int code, String msg) {
//                System.out.println("bbbbbbbbbb");
//            }
//        };
//        Map<String, String> params = new HashMap<String, String>();
////        params.put("Act", "run");
////        String imei = DateUtil.getIMEI(context);
//        params.put("ImeiId", "021");
//        params.put("Status", "1");
//        params.put("Note", "");
//        params.put("Time", DateUtil.getCurrentTime());
//        HttpRequestProxy.getInstance().phphreat(new HttpDataSubscriber(listener,
//                context, false), params);
    }

    public static PhpHeart4Timer getInstance() {
        if(instance==null){
            instance=new PhpHeart4Timer();
        }
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
