package com.dduckchul.codelabapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by DDuckchul on 2015-01-23.
 */
public class MyService extends Service {

    public static final int MSG_CONNECTED = 0;
    public static final int MSG_DISCONNECTED = 1;
    public static final int MSG_SET_TIME = 2;
    public static final int MSG_STOP_FOREGROUND = 3;

    public static final int NOTIFY_ID = 3333;
    public static final String TAG = "MyService";
    public static final String BROADCAST_ACTION = "com.dduckchul.codelabapp";

    public static boolean isRunning = false;
    public boolean isAlarmOn;

    public long startTime;
    public long endTime;
    public long timeInMillis;

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private SharedPreferences mSharedPreference;
    private Messenger mClient;
    private PowerManager.WakeLock mWakeLock;
    private Intent mIntent;
    private Handler myHandler = new Handler();
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {

            timeInMillis = SystemClock.uptimeMillis() - startTime;
            long seconds = timeInMillis/1000;

            sendBroadcastToActivity(seconds);
            myHandler.postDelayed(this, 500);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "SERVICE STARTED");

        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        isRunning = true;
        mSharedPreference = getSharedPreferences("setting", MODE_PRIVATE);
        mSharedPreference.edit().putBoolean("isAlarm", true).commit();
        mIntent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recordTime();
        makeNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "SERVICE DESTROYED");

        isRunning = false;
        mSharedPreference.edit().putBoolean("isAlarm", false).commit();

        finishRecord();
        stopForeground(true);
        mWakeLock.release();
    }

    public void makeNotification(){
        Resources resources = getResources();
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(resources.getString(R.string.notify_title))
                .setContentText(resources.getString(R.string.notify_text))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setContentIntent(pIntent).build();

        startForeground(NOTIFY_ID, notification);
    }

    public void recordTime(){
        if(startTime == 0) {
            startTime = SystemClock.uptimeMillis();
            myHandler.postDelayed(updateTimer, 0);
        }
    }

    public void finishRecord(){
        endTime = SystemClock.uptimeMillis();
        myHandler.removeCallbacks(updateTimer);
    }

    public static boolean isRunning(){
        return isRunning;
    }

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_CONNECTED :
                    break;
                case MSG_DISCONNECTED :
                    break;
                case MSG_SET_TIME :
                    mClient = msg.replyTo;
                    break;
                case MSG_STOP_FOREGROUND :
                    mClient = msg.replyTo;
                    break;
            }
        }
    }

    public void sendBroadcastToActivity(long seconds){
        try{
            mIntent.putExtra("seconds", seconds);
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
