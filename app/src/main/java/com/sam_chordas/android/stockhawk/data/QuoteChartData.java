package com.sam_chordas.android.stockhawk.data;

import java.util.List;

/**
 * Created by hp on 10/31/2016.
 */

public class QuoteChartData {
    public static final int TYPE_MAX = 0;
    public static final int TYPE_FIVE_YEAR = 1;
    public static final int TYPE_ONE_YEAR = 2;
    public static final int TYPE_ONE_MONTH = 3;
    public static final int TYPE_ONE_WEEK = 4;
    public static final int TYPE_ONE_DAY = 5;

    public static final int ERROR_FAILED_TO_LOAD_DATA = 6;
    public static final int ERROR_ERROR_IN_LOADED_DATA = 7;

    public static final int TYPE_TIME_STAMPS = 0;
    public static final int TYPE_DATE_STAMPS = 1;

    public List<String> stamps;
    public List<Double> quotes;

    public String minStamp;
    public String maxStamp;
    public String timeZone;
    public String currency;
    public Double previousClose;

    public int type;
    public int stampsType;

    public QuoteChartData(List<String> stamps, List<Double> quotes, String minStamp, String maxStamp, String timeZone, String currency, Double previousClose, int type, int stampsType) {
        this.stamps = stamps;
        this.quotes = quotes;
        this.minStamp = minStamp;
        this.maxStamp = maxStamp;
        this.timeZone = timeZone;
        this.currency = currency;
        this.previousClose = previousClose;
        this.type = type;
        this.stampsType = stampsType;
    }
}
