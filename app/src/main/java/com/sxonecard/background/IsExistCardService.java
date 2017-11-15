package com.sxonecard.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by pc on 2017-06-19.
 */

public class IsExistCardService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Intent isExistCardIntent = new Intent(this, IsExistCardBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, isExistCardIntent, 0);
        long time = SystemClock.elapsedRealtime() + 500;
        Log.d("IsExistCardService", String.valueOf(time));

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,time , sender);
        return super.onStartCommand(intent, flags, startId);
    }
}
