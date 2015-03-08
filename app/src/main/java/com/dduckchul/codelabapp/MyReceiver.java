package com.dduckchul.codelabapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by DDuckchul on 2015-01-23.
 */
public class MyReceiver extends BroadcastReceiver{

    public static final String TAG = "MyReceiver";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private SharedPreferences mSharedPreference;

    @Override
    public void onReceive(Context context, Intent intent) {

        mSharedPreference = context.getSharedPreferences("setting", Context.MODE_PRIVATE);

        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            boolean isAlarmSet = mSharedPreference.getBoolean("isAlarmSet", false);
            if(isAlarmSet){
                int hour = mSharedPreference.getInt("hour", 8);
                int minute = mSharedPreference.getInt("minute", 00);
                settingAlarm(context, hour, minute);
            } else {
                disableAlarm(context);
            }
        }
    }

    public void settingAlarm(Context context, int hour, int minute){
        Calendar calendar = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 00);

        if(now.getTimeInMillis() >= calendar.getTimeInMillis()){
            calendar.add(Calendar.DATE, 1);
        }

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getService(context, 0, new Intent(context, MyService.class), 0);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void disableAlarm(Context context){
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getService(context, 0, new Intent(context, MyService.class), 0);
        alarmMgr.cancel(alarmIntent);
    }
}
