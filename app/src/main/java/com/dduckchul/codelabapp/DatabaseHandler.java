package com.dduckchul.codelabapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DDuckchul on 2015-02-27.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    //db version
    private static final int DATABASE_VERSION = 1;

    //db name
    private static final String DATABASE_NAME = "CodeLabDB";

    //table name
    private static final String TABLE_TIMEDATA = "timedata";

    //timedata's column name
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_ELAPSEDTIME = "elapsedTime";

    private static final String TAG = "DatabaseHandler";

    private Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TIMEDATA + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " DATE DEFAULT (date('now', 'localtime')) UNIQUE," + KEY_ELAPSEDTIME + " INTEGER NOT NULL" + ")";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMEDATA);

        onCreate(db);
    }

    public void addSampleTimeData(String date, int timeData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_ELAPSEDTIME, timeData);
        db.insert(TABLE_TIMEDATA, null, values);
        db.close();
    }

    public void addTimeData(int timeData){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ELAPSEDTIME, timeData);
        long column = db.insertWithOnConflict(TABLE_TIMEDATA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if(column == -1){
            db.execSQL("UPDATE "+TABLE_TIMEDATA+" SET elapsedTime = elapsedTime + "+timeData+" WHERE date = date('now','localtime')");
        }

        db.close();
    }

    public TimeData getTimeData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TIMEDATA + " WHERE id=" + id;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        TimeData timedata = new TimeData(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));

        db.close();

        return timedata;
    }

    public List<TimeData> getTimeDataAll() {
        List<TimeData> timeDataList = new ArrayList<TimeData>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TIMEDATA + " ORDER BY date ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                TimeData timeData = new TimeData();
                timeData.setId(cursor.getInt(0));
                timeData.setDate(cursor.getString(1));
                timeData.setElapsedTime(cursor.getInt(2));
                timeDataList.add(timeData);
                cursor.moveToNext();
            }
        }

        db.close();

        return timeDataList;
    }

    public List<TimeData> getTimeDataByDay(int days) {
        List<TimeData> timeDataList = new ArrayList<TimeData>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TIMEDATA + " WHERE (julianday('now', 'localtime') - julianday(date, 'localtime')) <= " + days + " ORDER BY date ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                TimeData timeData = new TimeData();
                timeData.setId(cursor.getInt(0));
                timeData.setDate(cursor.getString(1));
                timeData.setElapsedTime(cursor.getInt(2));
                timeDataList.add(timeData);
                cursor.moveToNext();
            }
        }

        db.close();

        return timeDataList;
    }

    public List<TimeData> getTimeDataByWeek() {
        List<TimeData> timeDataList = new ArrayList<TimeData>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id, strftime('%Y-%W', date) as week, SUM(elapsedTime) as sum, AVG(elapsedTime) as avg FROM "
                + TABLE_TIMEDATA + " GROUP BY week ORDER BY week ASC;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
            String resource = context.getResources().getString(R.string.text_week);
            for (int i = 0; i < cursor.getCount(); i++) {
                TimeData timeData = new TimeData();
                timeData.setId(cursor.getInt(0));
                timeData.setDate(cursor.getString(1));
                timeData.setSum(cursor.getInt(2));
                timeData.setAvg(cursor.getInt(3));
                timeDataList.add(timeData);
                cursor.moveToNext();
            }
        }

        db.close();

        return timeDataList;
    }

    public List<TimeData> getTimeDataByMonth(){
        List<TimeData> timeDataList = new ArrayList<TimeData>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT id, strftime('%Y-%m', date) as month, SUM(elapsedTime) as sum, AVG(elapsedTime) as avg FROM "
                + TABLE_TIMEDATA + " GROUP BY month ORDER BY month ASC;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                TimeData timeData = new TimeData();
                timeData.setId(cursor.getInt(0));
                timeData.setDate(cursor.getString(1));
                timeData.setSum(cursor.getInt(2));
                timeData.setAvg(cursor.getInt(3));
                timeDataList.add(timeData);
                cursor.moveToNext();
            }
        }

        db.close();

        return timeDataList;
    }

    public void removeAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TIMEDATA);
        db.close();
    }

    public void executeExampleQuery(Context context){
        SQLiteDatabase db = this.getReadableDatabase();
//        String query = "SELECT date('now') - date(date) As time FROM " + TABLE_TIMEDATA;
        String query = "SELECT (julianday('now') - julianday(date)) As time FROM " + TABLE_TIMEDATA;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor != null){
            cursor.moveToFirst();
            String cal = cursor.getString(0);
            Toast.makeText(context, "DAY:" + cal, Toast.LENGTH_SHORT).show();
        }
    }
}
