package com.dduckchul.codelabapp;

import java.util.Date;

/**
 * Created by DDuckchul on 2015-02-27.
 */
public class TimeData {

    private int id;
    private String date;
    private int elapsedTime;
    private int sum;
    private int avg;

    public TimeData(){};

    public TimeData(int id, String date, int elapsedTime){
        this.id = id;
        this.date = date;
        this.elapsedTime = elapsedTime;
    }

    public TimeData(int id, String date, int sum, int avg){
        this.id = id;
        this.date = date;
        this.sum = sum;
        this.avg = avg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeData{");
        sb.append("id=").append(id);
        sb.append(", date='").append(date).append('\'');
        sb.append(", elapsedTime=").append(elapsedTime);
        sb.append(", sum=").append(sum);
        sb.append(", avg=").append(avg);
        sb.append('}');
        return sb.toString();
    }
}
