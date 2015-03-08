package com.dduckchul.codelabapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

// SharedPreference description
// key = setting
// 1. isAlarm = now Alarm is Activating
// 2. isAlarmSet = Is Alarm on or off
// 3. TotalTime = sum of total 잉여시간
// 4. hour = Alarm's hour
// 5. minute = Alarm's minute
// 6. distance = alarm's disable distance
// 7. isRangeAlarmSet = Is Range Alarm is on or off
// 8. lat = Latitude of saved location
// 9. lng = Longitude of saved location

public class MainActivity extends ActionBarActivity {

    public static final int CHANGE = 1111;
    public static final int RESET = 2222;
    public static final int MSG_END_ALARM = 7777;
    public static final int MSG_START_ALARM = 8888;
    public static final int MSG_GET_TIME = 9999;

    public static final String TAG = "MainActivity";

    public boolean mIsBind = false;
    public long seconds;
    public boolean changeImage;

    public NotificationManager nManager;
    public SharedPreferences mSharedPref;
    public ImageView mainImage;
    public TextView timeMessage;
    public TextView totalTime;

    public Messenger mService = null;
    private Intent mIntent;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try{
                Message msg = Message.obtain(null, MyService.MSG_CONNECTED);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            seconds = intent.getLongExtra("seconds", 0);

            timeMessage = (TextView)findViewById(R.id.main_time);
            mainImage = (ImageView)findViewById(R.id.main_image);

            timeMessage.setText(makeTimeString(seconds));
            if(!changeImage){
                Drawable drawable = getResources().getDrawable(R.drawable.siren);
                setImageColor(true, drawable, mainImage);
                changeImage = true;
            }
        }
    };

    class IncomingHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_GET_TIME :
                    break;
                case MSG_START_ALARM :
                    break;
            }
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIntent = new Intent(this, MainActivity.class);
        mSharedPref = getSharedPreferences("setting", MODE_PRIVATE);

        boolean isAlarm = mSharedPref.getBoolean("isAlarm", false);
        long savedTime = mSharedPref.getLong("todayTime", 0);
        double savedLat = (double)mSharedPref.getFloat("lat", 0);
        double savedLng = (double)mSharedPref.getFloat("lng", 0);

        Location savedLocation = new Location("Here");
        savedLocation.setLatitude(savedLat);
        savedLocation.setLongitude(savedLng);

        mainImage = (ImageView)findViewById(R.id.main_image);
        timeMessage = (TextView)findViewById(R.id.main_time);
        totalTime = (TextView)findViewById(R.id.total_time);

        AdView adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().setLocation(savedLocation).build();
        adView.loadAd(adRequest);

        timeMessage.setText(makeTimeString(seconds));
        totalTime.setText(makeTimeString(savedTime));

        nManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Drawable drawable = getResources().getDrawable(R.drawable.siren);
        setImageColor(isAlarm, drawable, mainImage);

        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Drawable drawable = getResources().getDrawable(R.drawable.siren);
                boolean isAlarm = mSharedPref.getBoolean("isAlarm", false);
                boolean isRangeAlarmSet = mSharedPref.getBoolean("isRangeAlarmSet", false);
                long savedTime = mSharedPref.getLong("todayTime", 0);

                SharedPreferences.Editor editor = mSharedPref.edit();

                if(isAlarm){

                    if(isRangeAlarmSet){

                        if(compareDistance()){
                            if(seconds >= 1){
                                editor.putLong("todayTime", savedTime + seconds);
                                totalTime.setText(makeTimeString(savedTime + seconds));
                                DatabaseHandler dbHandler = new DatabaseHandler(MainActivity.this);
                                dbHandler.addTimeData((int)seconds);
                            }
                            isAlarm = false;
                            editor.putBoolean("isAlarm", false);
                            editor.commit();

                            setEndService();
                            sendMessageToService(isAlarm);
                            setImageColor(isAlarm, drawable, mainImage);

                        } else {
                            String resource = getResources().getString(R.string.warning_inside);
                            Toast.makeText(MainActivity.this, resource, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        if(seconds >= 1){
                            DatabaseHandler dbHandler = new DatabaseHandler(MainActivity.this);
                            dbHandler.addTimeData((int)seconds);
                            editor.putLong("todayTime", savedTime + seconds);
                            totalTime.setText(makeTimeString(savedTime + seconds));
                        }
                        isAlarm = false;
                        editor.putBoolean("isAlarm", false);
                        editor.commit();

                        setEndService();
                        sendMessageToService(isAlarm);
                        setImageColor(isAlarm, drawable, mainImage);
                    }

                } else {
                    isAlarm = true;
                    editor.putBoolean("isAlarm", true);
                    setServiceStatus();

                    editor.commit();
                    sendMessageToService(isAlarm);
                    setImageColor(isAlarm, drawable, mainImage);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mIsBind){
            unbindService(mConnection);
            mIsBind = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(MyService.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, OptionActivity.class);
            startActivityForResult(intent, CHANGE);
        } else if(id == R.id.action_statistics){
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivityForResult(intent, RESET);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setImageColor(boolean isAlarm, Drawable image, ImageView imageView){

        if(isAlarm){
            image.setColorFilter(null);
            imageView.setImageDrawable(image);

        } else {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

            image.setColorFilter(colorFilter);
            imageView.setImageDrawable(image);
        }
    }


    public void sendMessageToService(boolean isAlarm){
        if(mIsBind){
            try{
                if(isAlarm){
                    Message msg = Message.obtain(null, MyService.MSG_SET_TIME, 0, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } else {
                    Message msg = Message.obtain(null, MyService.MSG_STOP_FOREGROUND, 1, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Service Unbind");
        }
    }

    public void getTotalTime(){
        long savedTime = mSharedPref.getLong("todayTime", 0);
    }

    public String makeTimeString(long seconds){
        int remainSeconds = (int)seconds % 60;

        int minutes = (int)seconds / 60;
        int remainMinutes = minutes % 60;

        int hours = minutes / 60;

        if(hours >= 24){

            int days = hours / 24;
            int remainDays = hours % 24;

            int years = days / 365;
            int remainYears = days % 365;

            String resource = getResources().getString(R.string.time_format_long);
            return String.format(resource, years, remainYears, remainDays, remainMinutes, remainSeconds);

        } else {

            String resource = getResources().getString(R.string.time_format_short);
            return String.format(resource, hours, remainMinutes, remainSeconds);

        }
    }

    public void setServiceStatus(){

        Intent service = new Intent(MainActivity.this, MyService.class);
        if(!MyService.isRunning){
            startService(service);
        }

        if(!mIsBind){
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            mIsBind = true;
        }
    }

    public void setEndService(){

        Intent service = new Intent(MainActivity.this, MyService.class);

        if(mIsBind){
            unbindService(mConnection);
            mIsBind = false;
        }

        if(MyService.isRunning){
            stopService(service);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == CHANGE){
            String resource = getResources().getString(R.string.result_setting);
            Toast.makeText(this, resource, Toast.LENGTH_SHORT).show();
        }else if(resultCode == RESET){
            timeMessage.setText(makeTimeString(0));
            totalTime.setText(makeTimeString(0));
            String resource = getResources().getString(R.string.result_reset);
            Toast.makeText(this, resource, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean compareDistance(){
        boolean result = false;

        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        int distance = mSharedPref.getInt("distance", 500);
        float distanceTo = 0;
        double savedLat = (double)mSharedPref.getFloat("lat", 0);
        double savedLng = (double)mSharedPref.getFloat("lng", 0);

        Location savedLocation = new Location("Here");
        savedLocation.setLatitude(savedLat);
        savedLocation.setLongitude(savedLng);

        Location location1 = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location location2 = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location1 != null){
            distanceTo = savedLocation.distanceTo(location1);
        } else if(location2 != null){
            distanceTo = savedLocation.distanceTo(location2);
        }

        locManager.removeUpdates(locationListener);

        if(distanceTo >= distance){
            return true;
        }
        return false;
    }
}
