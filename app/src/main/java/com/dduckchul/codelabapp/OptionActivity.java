package com.dduckchul.codelabapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class OptionActivity extends ActionBarActivity implements OnMapReadyCallback{

    public static final String TAG = "OptionActivity";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private TextView alarm_time;
    private Switch toggle_alarm;
    private Switch toggle_range;
    private EditText location_range;
    private LinearLayout alarm_layout;
    private MapFragment mapFragment;

    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor editor;

    private Marker marker;
    private Circle circle;

    private MarkerOptions markerOptions;
    private CircleOptions circleOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        mSharedPreference = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = mSharedPreference.edit();

        alarm_time = (TextView) findViewById(R.id.alarm_time);
        toggle_alarm = (Switch) findViewById(R.id.toggle_alarm);
        toggle_range = (Switch) findViewById(R.id.toggle_range);
        location_range = (EditText) findViewById(R.id.location_range);
        alarm_layout = (LinearLayout) findViewById(R.id.alarm_layout);

        getSavedSettings();

        alarm_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp_time[] = alarm_time.getText().toString().split(":");
                int temp_hour = Integer.parseInt(temp_time[0]);
                int temp_minute = Integer.parseInt(temp_time[1].substring(0,2));
                new TimePickerDialog(OptionActivity.this, timeSetListener, temp_hour, temp_minute, true).show();
            }
        });

        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close) {
            finish();
        } else if(id == R.id.action_save){

            String temp_time[] = alarm_time.getText().toString().split(":");
            int mod_hour = Integer.parseInt(temp_time[0]);
            int mod_minute = Integer.parseInt(temp_time[1].substring(0,2));
            int mod_distance = Integer.parseInt(location_range.getText().toString());

            boolean mod_isAlarmSet = toggle_alarm.isChecked();
            boolean mod_isRangeAlarmSet = toggle_range.isChecked();

            if(checkValidateData(mod_distance, mod_isRangeAlarmSet)){

                if(mod_isAlarmSet){
                    settingAlarm(mod_hour, mod_minute);
                } else {
                    disableAlarm();
                }
                saveSettings(mod_isAlarmSet, mod_isRangeAlarmSet, mod_hour, mod_minute, mod_distance);
                setResult(MainActivity.CHANGE);

                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void settingAlarm(int hour, int minute){
        Context context = getApplicationContext();

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

    public void disableAlarm(){
        Context context = getApplicationContext();

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getService(context, 0, new Intent(context, MyService.class), 0);
        alarmMgr.cancel(alarmIntent);
    }

    public void saveSettings(boolean isAlarmSet, boolean isRangeAlarmSet, int hour, int minute, int distance){
        if(marker != null && circle != null){
            LatLng latLng = marker.getPosition();
            editor.putFloat("lat", (float)latLng.latitude);
            editor.putFloat("lng", (float)latLng.longitude);
        } else {
            editor.remove("lat");
            editor.remove("lng");
        }

        editor.putBoolean("isAlarmSet", isAlarmSet);
        editor.putBoolean("isRangeAlarmSet", isRangeAlarmSet);
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.putInt("distance", distance);

        editor.commit();
    }

    public void getSavedSettings(){
        int hour = mSharedPreference.getInt("hour", 8);
        int minute = mSharedPreference.getInt("minute", 00);
        int distance = mSharedPreference.getInt("distance", 500);
        boolean isAlarmSet = mSharedPreference.getBoolean("isAlarmSet", false);
        boolean isRangeAlarmSet = mSharedPreference.getBoolean("isRangeAlarmSet", false);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        alarm_time.setText(simpleDateFormat.format(calendar.getTime()));
        toggle_alarm.setChecked(isAlarmSet);
        toggle_range.setChecked(isRangeAlarmSet);
        location_range.setText(Integer.toString(distance));
    }

    public boolean checkValidateData(int distance, boolean isRangeAlarmSet){
        boolean checklist = false;

        if(distance < 50 || distance > 1000){
            String resource = getResources().getString(R.string.warning_distance);
            Toast.makeText(OptionActivity.this, resource, Toast.LENGTH_SHORT).show();
            location_range.requestFocus();
            return checklist;
        }

        if(isRangeAlarmSet){
            if(marker == null || circle == null){
                String resource = getResources().getString(R.string.warning_range_alarm);
                Toast.makeText(OptionActivity.this, resource, Toast.LENGTH_SHORT).show();
                toggle_range.setChecked(false);
                return checklist;
            }
        }

        checklist = true;
        return checklist;
    }

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm a");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            alarm_time.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        GoogleMapOptions options = new GoogleMapOptions();
        googleMap.setIndoorEnabled(false);
        googleMap.setMyLocationEnabled(true);

        markerOptions = new MarkerOptions().alpha(0.6f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.siren_small))
                .title(getResources().getString(R.string.marker_title))
                .snippet(getResources().getString(R.string.marker_description));

        circleOptions = new CircleOptions().strokeWidth(1)
                .strokeColor(Color.parseColor("#44E85F0B"))
                .fillColor(Color.parseColor("#44FF8F19"));

        initMapCamera(googleMap);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(marker != null){
                    marker.remove();
                }

                if(circle != null){
                    circle.remove();
                }

                marker = googleMap.addMarker(markerOptions.position(latLng));
                marker.showInfoWindow();
                circle = googleMap.addCircle(circleOptions.center(latLng).radius(Integer.parseInt(location_range.getText().toString())));
                moveCamera(latLng);
            }

            public void moveCamera(LatLng latLng){
                float currZoom = googleMap.getCameraPosition().zoom;
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(currZoom).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker != null){
                    OptionActivity.this.marker.remove();
                    OptionActivity.this.marker = null;
                    circle.remove();
                    circle = null;
                }
                return true;
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });
    }

    public void initMapCamera(GoogleMap googleMap){
        float lat;
        float lng;

        if(mSharedPreference.contains("lat") && mSharedPreference.contains("lng")){
            lat = mSharedPreference.getFloat("lat", 0);
            lng = mSharedPreference.getFloat("lng", 0);
            LatLng savedLatLng = new LatLng(lat, lng);

            marker = googleMap.addMarker(markerOptions.position(savedLatLng));
            marker.showInfoWindow();
            circle = googleMap.addCircle(circleOptions.center(savedLatLng).radius(Integer.parseInt(location_range.getText().toString())));
        } else {
            LocationManager locationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location;
            if(locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
                location = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lat = (float)location.getLatitude();
                lng = (float)location.getLongitude();
            } else if(locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
                location = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lat = (float)location.getLatitude();
                lng = (float)location.getLongitude();
            } else {
                lat = 0;
                lng = 0;
            }
        }

        LatLng savedLatLng = new LatLng(lat, lng);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(savedLatLng).zoom(14).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
