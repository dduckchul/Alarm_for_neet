package com.dduckchul.codelabapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DDuckchul on 2015-02-27.
 */
public class StatisticsActivity extends ActionBarActivity{

    private static final String TAG = "StatisticsActivity";

    private static final int IS_LAST_DAY = 0;
    private static final int IS_DAILY = 1;
    private static final int IS_WEEKLY = 2;
    private static final int IS_MONTHLY = 3;

    private LineChart linechart;
    private DatabaseHandler dbHandler;
    private SharedPreferences mSharedPrefence;

    private Button button_lastday;
    private Button button_daily;
    private Button button_weekly;
    private Button button_monthly;

    private boolean isLastDay;
    private boolean isDaily;
    private boolean isWeekly;
    private boolean isMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_statistics);

        mSharedPrefence = getSharedPreferences("setting", MODE_PRIVATE);
        double savedLat = (double)mSharedPrefence.getFloat("lat", 0);
        double savedLng = (double)mSharedPrefence.getFloat("lng", 0);

        Location savedLocation = new Location("Here");
        savedLocation.setLatitude(savedLat);
        savedLocation.setLongitude(savedLng);

        AdView adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().setLocation(savedLocation).build();
        adView.loadAd(adRequest);

        dbHandler = new DatabaseHandler(this);
        linechart = (LineChart)findViewById(R.id.line_chart);
        button_lastday = (Button)findViewById(R.id.button_lastday);
        button_daily = (Button)findViewById(R.id.button_daily);
        button_weekly = (Button)findViewById(R.id.button_weekly);
        button_monthly = (Button)findViewById(R.id.button_monthly);

        setLineChartOption();
        setYaxisOption();
        setXaxisOption();
        setChartDataToLastDay();

        button_lastday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChartDataToLastDay();
            }
        });

        button_daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChartDataToDaily();
            }
        });

        button_weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChartDataToWeekly();
            }
        });

        button_monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChartDataToMonthly();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_close) {
            finish();
        } else if(id == R.id.action_reset){
            AlertDialog alertDialog = new AlertDialog.Builder(StatisticsActivity.this)
                    .setTitle(R.string.title_reset)
                    .setMessage(R.string.alert_message)
                    .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHandler.removeAllData();
                            mSharedPrefence.edit().remove("todayTime").commit();
                            setResult(MainActivity.RESET);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.alert_cancel, null).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setLineChartOption() {
        linechart.setNoDataText(getResources().getString(R.string.statistics_text_nodata));
        linechart.setValueTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));

        Paint paint = linechart.getPaint(LineChart.PAINT_DESCRIPTION);
        paint.setColor(getResources().getColor(R.color.abc_primary_text_material_dark));

        linechart.setDrawGridBackground(false);
        linechart.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float second) {
                float minute = second / 60;
                float hour = second / (60 * 60);
                float day = second / (60 * 60 * 24);

                if(second <= 59){
                    String resource = getResources().getString(R.string.text_second);
                    float round = Float.parseFloat(String.format("%.2f", second));
                    return Float.toString(round) + resource;
                } else if(minute < 60){
                    String resource = getResources().getString(R.string.text_minute);
                    float round = Float.parseFloat(String.format("%.2f", minute));
                    return Float.toString(round) + resource;
                } else if(hour < 24){
                    String resource = getResources().getString(R.string.text_hour);
                    float round = Float.parseFloat(String.format("%.2f", hour));
                    return Float.toString(round) + resource;
                } else {
                    String resource = getResources().getString(R.string.text_day);
                    float round = Float.parseFloat(String.format("%.2f", day));
                    return Float.toString(round) + resource;
                }
            }
        });
    }

    private void setYaxisOption() {
        YAxis yAxisLeft = linechart.getAxisLeft();
        YAxis yAxisRight = linechart.getAxisRight();

        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        yAxisLeft.setEnabled(false);

        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(true);
        yAxisRight.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        yAxisRight.setEnabled(false);
    }

    private void setXaxisOption() {
        XAxis xAxis = linechart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
    }

    public void setChartDataToLastDay(){
        if(!isLastDay){
            List<TimeData> timeDataListLastDay = dbHandler.getTimeDataByDay(7);

            ArrayList<String> xVals = new ArrayList<String>();
            ArrayList<Entry> yVals = new ArrayList<Entry>();

            for(TimeData timedata : timeDataListLastDay){
                int i = timeDataListLastDay.indexOf(timedata);
                yVals.add(new Entry(timedata.getElapsedTime(),i));
                xVals.add(timedata.getDate());
            }

            setChartDatas(xVals, yVals, IS_LAST_DAY);
            setWhichChart(IS_LAST_DAY);
        }
    }

    public void setChartDataToDaily(){
        if(!isDaily){
            List<TimeData> timeDataListDaily = dbHandler.getTimeDataAll();

            ArrayList<String> xVals = new ArrayList<String>();
            ArrayList<Entry> yVals = new ArrayList<Entry>();

            for(TimeData timedata : timeDataListDaily){
                int i = timeDataListDaily.indexOf(timedata);
                yVals.add(new Entry(timedata.getElapsedTime(),i));
                xVals.add(timedata.getDate());
            }

            setChartDatas(xVals, yVals, IS_DAILY);
            setWhichChart(IS_DAILY);
        }
    }

    public void setChartDataToWeekly(){
        if(!isWeekly) {
            List<TimeData> timeDataListWeekly = dbHandler.getTimeDataByWeek();

            ArrayList<String> xVals = new ArrayList<String>();
            ArrayList<Entry> yValsAvg = new ArrayList<Entry>();
            ArrayList<Entry> yValsSum = new ArrayList<Entry>();

            for(TimeData timedata : timeDataListWeekly){
                int i = timeDataListWeekly.indexOf(timedata);
                yValsAvg.add(new Entry(timedata.getAvg(), i));
                yValsSum.add(new Entry(timedata.getSum(), i));
                xVals.add(timedata.getDate());
            }

            setChartDatas(xVals, yValsAvg, yValsSum, IS_WEEKLY);
            setWhichChart(IS_WEEKLY);
        }
    }

    public void setChartDataToMonthly(){
        if(!isMonthly){
            List<TimeData> timeDataListMonthly = dbHandler.getTimeDataByMonth();

            ArrayList<String> xVals = new ArrayList<String>();
            ArrayList<Entry> yValsAvg = new ArrayList<Entry>();
            ArrayList<Entry> yValsSum = new ArrayList<Entry>();

            for(TimeData timedata : timeDataListMonthly){
                int i = timeDataListMonthly.indexOf(timedata);
                yValsAvg.add(new Entry(timedata.getAvg(), i));
                yValsSum.add(new Entry(timedata.getSum(), i));
                xVals.add(timedata.getDate());
            }

            setChartDatas(xVals, yValsAvg, yValsSum, IS_MONTHLY);
            setWhichChart(IS_MONTHLY);
        }
    }

    public void setWhichChart(int which){
        switch(which){
            case IS_LAST_DAY:
                isLastDay = true;
                isDaily = false;
                isWeekly = false;
                isMonthly = false;
                break;
            case IS_DAILY :
                isLastDay = false;
                isDaily = true;
                isWeekly = false;
                isMonthly = false;
                break;
            case IS_WEEKLY :
                isLastDay = false;
                isDaily = false;
                isWeekly = true;
                isMonthly = false;
                break;
            case IS_MONTHLY :
                isLastDay = false;
                isDaily = false;
                isWeekly = false;
                isMonthly = true;
        }
    }

    public void setChartDatas(ArrayList<String> xVals, ArrayList<Entry> yVals, int which){
        if(xVals.size() != 0){
            LineDataSet lineDataSet = new LineDataSet(yVals, null);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawCubic(true);
            lineDataSet.setDrawFilled(true);
            switch(which){
                case IS_LAST_DAY :
                    lineDataSet.setColor(getResources().getColor(R.color.blue_daum));
                    lineDataSet.setFillColor(getResources().getColor(R.color.blue_daum));
                    break;
                case IS_DAILY :
                    lineDataSet.setColor(getResources().getColor(R.color.green_daum));
                    lineDataSet.setFillColor(getResources().getColor(R.color.green_daum));
            }
            LineData lineData = new LineData(xVals, lineDataSet);
            linechart.setData(lineData);
            linechart.setDescription(null);
            linechart.setDrawLegend(false);
            linechart.setMaxVisibleValueCount(10);
            linechart.animateY(1000);
        } else {
            linechart.setData(null);
            linechart.setDrawLegend(false);
            linechart.invalidate();
        }
    }

    public void setChartDatas(ArrayList<String> xVals, ArrayList<Entry> yValsAvg, ArrayList<Entry> yValsSum, int which){
        if(xVals.size() != 0){
            String resource1 = getResources().getString(R.string.chart_legend_sum);
            String resource2 = getResources().getString(R.string.chart_legend_avg);
            LineDataSet lineDataSet = new LineDataSet(yValsSum, resource1);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawCubic(true);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setColor(getResources().getColor(R.color.orange_daum));
            lineDataSet.setFillColor(getResources().getColor(R.color.orange_daum));

            LineDataSet lineDataSet2 = new LineDataSet(yValsAvg, resource2);
            lineDataSet2.setDrawCircles(false);
            lineDataSet2.setDrawCubic(true);
            lineDataSet2.setDrawFilled(true);
            lineDataSet2.setColor(getResources().getColor(R.color.red_daum));
            lineDataSet2.setFillColor(getResources().getColor(R.color.red_daum));

            ArrayList<LineDataSet> lines = new ArrayList<LineDataSet>();
            lines.add(lineDataSet);
            lines.add(lineDataSet2);

            LineData lineData = new LineData(xVals,lines);
            linechart.setData(lineData);
            linechart.setDrawLegend(true);
            linechart.setMaxVisibleValueCount(20);

            Legend legend = linechart.getLegend();
            legend.setTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
            legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            linechart.animateY(1000);

            switch(which){
                case IS_WEEKLY:
                    String resource = getResources().getString(R.string.text_week_description);
                    linechart.setDescription(resource);
                    break;
                case IS_MONTHLY:
                    linechart.setDescription(null);
            }
        } else {
            linechart.setData(null);
            linechart.setDrawLegend(false);
            linechart.invalidate();
        }
    }

    public void makeSampleData(DatabaseHandler dbHandler){
        dbHandler.addSampleTimeData("2015-02-01", 20);
        dbHandler.addSampleTimeData("2015-02-02", 30);
        dbHandler.addSampleTimeData("2015-02-03", 40);
        dbHandler.addSampleTimeData("2015-02-04", 50);
        dbHandler.addSampleTimeData("2015-02-05", 60);
        dbHandler.addSampleTimeData("2015-01-30", 32);
        dbHandler.addSampleTimeData("2015-01-31", 54);
        dbHandler.addSampleTimeData("2015-02-07", 28);
        dbHandler.addSampleTimeData("2015-02-08", 35);
        dbHandler.addSampleTimeData("2015-02-09", 43);
        dbHandler.addSampleTimeData("2015-02-10", 45);
        dbHandler.addSampleTimeData("2015-02-15", 40);
        dbHandler.addSampleTimeData("2015-02-21", 30);
        dbHandler.addSampleTimeData("2015-02-22", 40);
        dbHandler.addSampleTimeData("2015-02-23", 50);
        dbHandler.addSampleTimeData("2015-02-24", 80);
        dbHandler.addSampleTimeData("2015-02-27", 12);
        dbHandler.addSampleTimeData("2015-02-28", 17);
        dbHandler.addSampleTimeData("2015-03-01", 15);
    }
}
