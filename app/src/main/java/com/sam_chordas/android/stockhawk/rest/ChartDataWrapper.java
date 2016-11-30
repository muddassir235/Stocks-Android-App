package com.sam_chordas.android.stockhawk.rest;

import android.animation.PropertyValuesHolder;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteChartData;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by hp on 10/31/2016.
 */

public class ChartDataWrapper {
    Context mContext;
    String mSymbol;
    String mSpan;
    LineChartView mLineChartView;
    YahooChartDataLoader mYahooChartLoader;
    ProgressBar mLoadingProgress;
    FrameLayout mLoadingFilter;

    ImageView mStatusImage;
    TextView mStatusText;

    Typeface customTypeFace;
    TextView mPrevValueTV;

    private static ChartDataWrapper chartDataWrapper;

    public static ChartDataWrapper getInstance(){
        if(chartDataWrapper == null){
            chartDataWrapper = new ChartDataWrapper();
        }
        return chartDataWrapper;
    }

    public static void startOver(){
        chartDataWrapper = null;
    }

    public ChartDataWrapper with(Context context){
        this.mContext = context;
        customTypeFace  = Typeface.createFromAsset(mContext.getAssets(), "fonts/VCR_OS.ttf");
        return this;
    }

    public ChartDataWrapper stock(String symbol){
        this.mSymbol = symbol;
        return this;
    }

    /**
     * @param span: can be W number of days e.g. (1d,2d,3d, ...)
     *            Y number of months e.g. (1m,2m,3m, ...)
     *            Z number of years e.g.(1y,2y,3y, ...)
     *            or "max" to show a graph of all previous stocks
     *
     * @return
     */
    public ChartDataWrapper span(String span){
        this.mSpan = span;
        return this;
    }

    public  ChartDataWrapper drawOn(LineChartView lineChartView){
        if(mSymbol!=null && mSpan!=null){
            this.mLineChartView = lineChartView;
            if(mYahooChartLoader==null) {
                mYahooChartLoader = new YahooChartDataLoader(mContext, mContext.getString(R.string.yahoo_chart_url, mSymbol, mSpan), mLineChartView, mSpan,mPrevValueTV,mLoadingProgress,mLoadingFilter,mStatusImage,mStatusText);
            }else {
                mYahooChartLoader.changeParams(mLineChartView,mContext.getString(R.string.yahoo_chart_url, mSymbol, mSpan),mSpan);
            }
            mYahooChartLoader.startLoading();
            return this;
        }else{
            throw new NullPointerException("Please call both stock() and span() before calling drawOn()");
        }
    }

    public ChartDataWrapper setPrevValueTV(TextView textView){
        this.mPrevValueTV = textView;
        return this;
    }

    public ChartDataWrapper setLoadingView(ProgressBar progressBar){
        this.mLoadingProgress = progressBar;
        return this;
    }

    public ChartDataWrapper setLoadingFilter(FrameLayout filter){
        this.mLoadingFilter = filter;
        return this;
    }

    public ChartDataWrapper setStatusImage(ImageView image){
        this.mStatusImage = image;
        return this;
    }

    public ChartDataWrapper setStatusText(TextView text){
        this.mStatusText = text;
        return this;
    }

    ChartDataWrapper() {
    }

    class YahooChartDataLoader extends AsyncTaskLoader<QuoteChartData> {
        private final String LOG_TAG = YahooChartDataLoader.class.getName()+": ";
        String mUrl;
        String mSpan;
        private OkHttpClient mClient;
        LineChartView mLineChart;
        TextView mPrevTV;
        ProgressBar mLoadingProgress;
        FrameLayout mLoadingFilter;

        ImageView mStatusImage;
        TextView mStatusText;

        Tooltip mTip;
        QuoteChartData mQuoteChartDataOneDay;
        QuoteChartData mQuoteChartDataOneWeek;
        QuoteChartData mQuoteChartDataOneMonth;
        QuoteChartData mQuoteChartDataOneYear;
        QuoteChartData mQuoteChartDataFiveYears;
        QuoteChartData mQuoteChartDataMax;

        int numberOfTries;

        public YahooChartDataLoader(Context context,String url, LineChartView lineChart, String span,TextView prevTV,ProgressBar progressBar,FrameLayout filter,ImageView statusImage,TextView statusText) {
            super(context);
            this.mUrl = url;
            this.mClient = new OkHttpClient();
            this.mLineChart = lineChart;
            this.mSpan = span;
            this.mPrevTV = prevTV;
            this.mLoadingProgress = progressBar;
            this.mLoadingFilter = filter;
            this.mStatusImage = statusImage;
            this.mStatusText = statusText;
            this.numberOfTries = 0;
        }

        public void changeParams(LineChartView lineView,String url, String span){
            this.mLineChart = lineView;
            this.mUrl = url;
            this.mSpan = span;
        }

        @Override
        public QuoteChartData loadInBackground() {
            Request request = new Request.Builder()
                    .url(mUrl)
                    .build();

            Log.v(LOG_TAG, " request: "+request.toString());
            Log.v(LOG_TAG, " url: "+mUrl);
            Response response = null;
            try {
                response = mClient.newCall(request).execute();

                Log.v(LOG_TAG, " response body: "+response.body());
                Log.v(LOG_TAG, " response message: "+response.message());

                String responseString = response.body().string();
                responseString = responseString.substring(30,responseString.length()-1);
                JSONObject respJson = new JSONObject(responseString);
                List<String> stamps = new ArrayList<>();
                List<Double> quotes = new ArrayList<>();

                String STAMPS_KEY;
                if(mSpan.equals("1d") || mSpan.equals("7d")){
                    STAMPS_KEY = "Timestamp";
                }else{
                    STAMPS_KEY = "Date";
                }

                JSONArray seriesArray = respJson.getJSONArray("series");
                for(int i=0;i<seriesArray.length();i++){
                    JSONObject tuple = seriesArray.getJSONObject(i);
                    stamps.add(tuple.getString(STAMPS_KEY));
                    quotes.add(tuple.getDouble("close"));
                }

                switch (mSpan) {
                    case "1d":
                        mQuoteChartDataOneDay = new QuoteChartData(
                                stamps,
                                quotes,
                                respJson.getJSONObject(STAMPS_KEY).getString("min"),
                                respJson.getJSONObject(STAMPS_KEY).getString("max"),
                                respJson.getJSONObject("meta").getString("timezone"),
                                respJson.getJSONObject("meta").getString("currency"),
                                respJson.getJSONObject("meta").getDouble("previous_close"),
                                QuoteChartData.TYPE_ONE_DAY,
                                QuoteChartData.TYPE_TIME_STAMPS
                        );
                        return mQuoteChartDataOneDay;
                    case "7d":
                        Log.v(LOG_TAG, " one week");
                        mQuoteChartDataOneWeek = new QuoteChartData(
                                stamps,
                                quotes,
                                respJson.getJSONObject(STAMPS_KEY).getString("min"),
                                respJson.getJSONObject(STAMPS_KEY).getString("max"),
                                respJson.getJSONObject("meta").getString("timezone"),
                                respJson.getJSONObject("meta").getString("currency"),
                                null,
                                QuoteChartData.TYPE_ONE_WEEK,
                                QuoteChartData.TYPE_TIME_STAMPS
                        );
                        return mQuoteChartDataOneWeek;
                    case "1m":
                        mQuoteChartDataOneMonth = new QuoteChartData(
                                stamps,
                                quotes,
                                respJson.getJSONObject(STAMPS_KEY).getString("min"),
                                respJson.getJSONObject(STAMPS_KEY).getString("max"),
                                null,
                                respJson.getJSONObject("meta").getString("currency"),
                                null,
                                QuoteChartData.TYPE_ONE_MONTH,
                                QuoteChartData.TYPE_DATE_STAMPS
                        );
                        return mQuoteChartDataOneMonth;
                    case "1y":
                        mQuoteChartDataOneYear = new QuoteChartData(
                                stamps,
                                quotes,
                                respJson.getJSONObject(STAMPS_KEY).getString("min"),
                                respJson.getJSONObject(STAMPS_KEY).getString("max"),
                                null,
                                respJson.getJSONObject("meta").getString("currency"),
                                null,
                                QuoteChartData.TYPE_ONE_YEAR,
                                QuoteChartData.TYPE_DATE_STAMPS
                        );
                        return mQuoteChartDataOneYear;
                    case "5y":
                        mQuoteChartDataFiveYears = new QuoteChartData(
                                stamps,
                                quotes,
                                respJson.getJSONObject(STAMPS_KEY).getString("min"),
                                respJson.getJSONObject(STAMPS_KEY).getString("max"),
                                null,
                                respJson.getJSONObject("meta").getString("currency"),
                                null,
                                QuoteChartData.TYPE_FIVE_YEAR,
                                QuoteChartData.TYPE_DATE_STAMPS
                        );
                        return mQuoteChartDataFiveYears;
                    default:
                        mQuoteChartDataMax = new QuoteChartData(
                                stamps,
                                quotes,
                                respJson.getJSONObject(STAMPS_KEY).getString("min"),
                                respJson.getJSONObject(STAMPS_KEY).getString("max"),
                                null,
                                respJson.getJSONObject("meta").getString("currency"),
                                null,
                                QuoteChartData.TYPE_MAX,
                                QuoteChartData.TYPE_DATE_STAMPS
                        );
                        return mQuoteChartDataMax;
                }
            } catch (IOException e) {
                // TODO: show appropriate message in chart view
                e.printStackTrace();
                Log.v(LOG_TAG, " io exception");
                return new QuoteChartData(null,null,null,null,null,null,null,QuoteChartData.ERROR_FAILED_TO_LOAD_DATA,-1);
            } catch (JSONException e) {
                // TODO: show appropriate response in chart view
                e.printStackTrace();
                Log.v(LOG_TAG, " json exception");
                return new QuoteChartData(null,null,null,null,null,null,null,QuoteChartData.ERROR_ERROR_IN_LOADED_DATA,-1);

            }
        }

        @Override
        public void deliverResult(final QuoteChartData data) {
            // Data
            mLineChart.dismiss();
            mLineChart.dismissAllTooltips();

            if(mPrevTV!=null){
                mPrevTV.setVisibility(View.GONE);
            }
            mTip = new Tooltip(mContext, R.layout.tool_tip_layout);
            mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

                mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

                mTip.setPivotX(Tools.fromDpToPx(65) / 2);
                mTip.setPivotY(Tools.fromDpToPx(25));
            }

            final TextView valueTV = (TextView) mTip.findViewById(R.id.value);
            final TextView stampTV = (TextView) mTip.findViewById(R.id.stamp);
            valueTV.setTypeface(customTypeFace);
            stampTV.setTypeface(customTypeFace);
            mLineChart.setTypeface(customTypeFace);
            mLineChart.removeLabelThreshold();
            mLineChart.removeValueThreshold();

            final Paint labelThresholdPaint = new Paint();
            labelThresholdPaint.setColor(mContext.getResources().getColor(R.color.purple_400));
            labelThresholdPaint.setStrokeWidth(5.0f);


            if (data != null) {
                doneLoading();
                this.numberOfTries = 0;
                if(mStatusImage!=null && mStatusImage.getVisibility() == View.VISIBLE){
                    mStatusImage.setVisibility(View.GONE);
                }
                if(mStatusText!=null && mStatusText.getVisibility() == View.VISIBLE){
                    mStatusText.setVisibility(View.GONE);
                }

                if (data.type == QuoteChartData.TYPE_ONE_DAY) {
                    if(mPrevTV!=null){
                        mPrevTV.setVisibility(View.VISIBLE);
                    }

                    mTip.setDimensions((int) Tools.fromDpToPx(145), (int) Tools.fromDpToPx(25));

                    Long currTimeStamp = System.currentTimeMillis();

                    int currIndex = 0;

                    final int endIndex = data.stamps.size() - 1-(data.stamps.size() / 4);

                    for (int i = 0; i <= endIndex; i++) {
                        if (currTimeStamp <= (Long.valueOf(data.stamps.get(i)) * 1000)) {
                            currIndex = i;
                            break;
                        } else if (i == endIndex) {
                            currIndex = i;
                        }
                    }

                    int stepSize = data.quotes.size() / 3;
                    if(stepSize == 0){
                        stepSize = 1;
                    }
                    int numberOfIndexs = 0;

                    final float[] values = new float[data.quotes.size()];
                    for (int i = 0; i < data.quotes.size(); i++) {
                        values[i] = Float.valueOf(String.valueOf(data.quotes.get(i)));
                    }

                    Double minimunValue = Collections.min(data.quotes);
                    Double maximunValue = Collections.max(data.quotes);

                    int maxYAxisIndex = (int) (maximunValue + (maximunValue - minimunValue) / 5);
                    int minYAxisIndex = (int) (minimunValue - (maximunValue - minimunValue) / 5);

                    final String[] stamps = new String[data.stamps.size()];
                    for (int i = 0; i < data.stamps.size(); i++) {
                        if (i == 0) {
                            stamps[i] = "";
                        } else if (numberOfIndexs >= 2) {
                            stamps[i] = "";
                        } else if (i % stepSize == 0) {
                            stamps[i] = getReadableTime(data.stamps.get(i));
                            Log.v(LOG_TAG, " time: " + data.stamps.get(i));
                            numberOfIndexs++;
                        } else {
                            stamps[i] = "";
                        }
                    }

                    Log.v(LOG_TAG, " curr index: " + currIndex + ", size: " + values.length);

                    Paint thresholdPaint = new Paint();
                    thresholdPaint.setStyle(Paint.Style.STROKE);
                    thresholdPaint.setStrokeWidth(5.0f);
                    thresholdPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
                    thresholdPaint.setColor(mContext.getResources().getColor(R.color.teal_accent));

                    mLineChart.setTooltips(mTip);
                    mLineChart.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            valueTV.setText(""+round(values[entryIndex]));
                            stampTV.setText(getReadableTime(data.stamps.get(entryIndex)));
                            mLineChartView.setLabelThreshold(entryIndex,entryIndex,labelThresholdPaint);
                        }
                    });

                    LineSet prevDataSet = new LineSet(stamps, values);
                    prevDataSet.setSmooth(true);
                    prevDataSet.setColor(mContext.getResources().getColor(R.color.green_500))
                            .endAt(currIndex);
                    mLineChart.addData(prevDataSet);

                    LineSet currDataSet = new LineSet(stamps, values);
                    if(currIndex!=0) {
                        currDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(currIndex - 1).endAt(currIndex);
                    }else {
                        currDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(currIndex).endAt(currIndex);
                    }
                    mLineChart.addData(currDataSet);

                    LineSet laterDataSet = new LineSet(stamps, values);
                    laterDataSet.setColor(Color.parseColor("#AAAAAA")).beginAt(currIndex).endAt(endIndex);
                    mLineChart.addData(laterDataSet);

                    float marginTop = valueToPixels(
                            mLineChart.getHeight(),
                            maxYAxisIndex-minYAxisIndex+2,
                            Float.valueOf(String.valueOf(data.previousClose))-minYAxisIndex+1
                    );

                    FrameLayout.LayoutParams prevTVLayoutParams = (FrameLayout.LayoutParams) mPrevTV.getLayoutParams();

                    if(marginTop>Tools.fromDpToPx(30)) {
                        prevTVLayoutParams.topMargin = (int) (marginTop - Tools.fromDpToPx(30));
                    }else {
                        prevTVLayoutParams.topMargin = (int) (marginTop + Tools.fromDpToPx(30));
                    }
                    mPrevTV.setText("Prev Close: "+data.previousClose);
                    mPrevTV.setTypeface(customTypeFace);

                    mLineChart.setValueThreshold(
                            Float.valueOf(String.valueOf(data.previousClose-0.01)),
                            Float.valueOf(String.valueOf(data.previousClose)),
                            thresholdPaint
                    );

                    Paint gridPaint = new Paint();
                    gridPaint.setStrokeWidth(1.0f);
                    mLineChart.setYAxis(false);

                    gridPaint.setColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChart.setStep((maxYAxisIndex-minYAxisIndex+2)/getNumberOfGridRows(minYAxisIndex-1,maxYAxisIndex+1));
                    mLineChart.setAxisBorderValues(minYAxisIndex - 1, maxYAxisIndex + 1);
                    mLineChart.setAxisLabelsSpacing(30.0f);
                    mLineChart.setGrid(ChartView.GridType.HORIZONTAL, getNumberOfGridRows(minYAxisIndex - 1, maxYAxisIndex + 1), 1, gridPaint);
                    mLineChart.setAxisColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChartView.setLabelsColor(mContext.getResources().getColor(R.color.green_text_50));
                } else if (data.type == QuoteChartData.TYPE_ONE_WEEK) {
                    mTip.setDimensions((int) Tools.fromDpToPx(210), (int) Tools.fromDpToPx(25));

                    int endIndex = data.stamps.size() - (data.stamps.size() / 25);

                    int stepSize = data.quotes.size() / 3;
                    if(stepSize == 0){
                        stepSize = 1;
                    }
                    int numberOfIndexs = 0;

                    final float[] values = new float[data.quotes.size()];
                    for (int i = 0; i < data.quotes.size(); i++) {
                        values[i] = Float.valueOf(String.valueOf(data.quotes.get(i)));
                    }

                    Double minimunValue = Collections.min(data.quotes);
                    Double maximunValue = Collections.max(data.quotes);

                    int maxYAxisIndex = (int) (maximunValue + (maximunValue - minimunValue) / 5);
                    int minYAxisIndex = (int) (minimunValue - (maximunValue - minimunValue) / 5);

                    String[] stamps = new String[data.stamps.size()];
                    for (int i = 0; i < data.stamps.size(); i++) {
                        if (i == 0) {
                            stamps[i] = "";
                        } else if (numberOfIndexs >= 2) {
                            stamps[i] = "";
                        } else if (i % stepSize == 0) {
                            stamps[i] = getReadableDateFromTimeStamp(data.stamps.get(i));
                            Log.v(LOG_TAG, " time: " + data.stamps.get(i));
                            numberOfIndexs++;
                        } else {
                            stamps[i] = "";
                        }
                    }

                    mLineChart.setTooltips(mTip);
                    mLineChart.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            valueTV.setText(""+round(values[entryIndex]));
                            stampTV.setText(getReadableDateFromTimeStamp(data.stamps.get(entryIndex))+" "+getReadableTime(data.stamps.get(entryIndex)));
                            mLineChartView.setLabelThreshold(entryIndex,entryIndex,labelThresholdPaint);
                        }
                    });

                    LineSet prevDataSet = new LineSet(stamps, values);
                    prevDataSet.setSmooth(true);
                    prevDataSet.setColor(mContext.getResources().getColor(R.color.green_500))
                            .endAt(endIndex);
                    mLineChart.addData(prevDataSet);

                    LineSet endPointDataSet = new LineSet(stamps, values);
                    endPointDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(endIndex - 1).endAt(endIndex);
                    mLineChart.addData(endPointDataSet);


                    Paint gridPaint = new Paint();
                    gridPaint.setStrokeWidth(1.0f);
                    mLineChart.setYAxis(false);
                    gridPaint.setColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChart.setStep((maxYAxisIndex-minYAxisIndex+2)/getNumberOfGridRows(minYAxisIndex-1,maxYAxisIndex+1));
                    mLineChart.setAxisBorderValues(minYAxisIndex - 1, maxYAxisIndex + 1);
                    mLineChart.setAxisLabelsSpacing(30.0f);
                    mLineChart.setGrid(ChartView.GridType.HORIZONTAL, getNumberOfGridRows(minYAxisIndex - 1, maxYAxisIndex + 1), 1, gridPaint);
                    mLineChart.setAxisColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChartView.setLabelsColor(mContext.getResources().getColor(R.color.green_text_50));
                } else if (data.type == QuoteChartData.TYPE_ONE_MONTH) {
                    mTip.setDimensions((int) Tools.fromDpToPx(128), (int) Tools.fromDpToPx(25));

                    int endIndex = data.stamps.size() - (data.stamps.size() / 25);
                    if(endIndex==data.stamps.size()){
                        endIndex = data.stamps.size() - (data.stamps.size()/10);
                    }

                    int stepSize = data.quotes.size() / 3;
                    if(stepSize == 0){
                        stepSize = 1;
                    }

                    int numberOfIndexs = 0;

                    final float[] values = new float[data.quotes.size()];
                    for (int i = 0; i < data.quotes.size(); i++) {
                        values[i] = Float.valueOf(String.valueOf(data.quotes.get(i)));
                    }

                    Double minimunValue = Collections.min(data.quotes);
                    Double maximunValue = Collections.max(data.quotes);

                    int maxYAxisIndex = (int) (maximunValue + (maximunValue - minimunValue) / 5);
                    int minYAxisIndex = (int) (minimunValue - (maximunValue - minimunValue) / 5);

                    String[] stamps = new String[data.stamps.size()];
                    for (int i = 0; i < data.stamps.size(); i++) {
                        if (i == 0) {
                            stamps[i] = "";
                        } else if (numberOfIndexs >= 2) {
                            stamps[i] = "";
                        } else if (i % stepSize == 0) {
                            stamps[i] = getReadableDate(data.stamps.get(i));
                            Log.v(LOG_TAG, " time: " + data.stamps.get(i));
                            numberOfIndexs++;
                        } else {
                            stamps[i] = "";
                        }
                    }


                    mLineChart.setTooltips(mTip);
                    mLineChart.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            valueTV.setText(""+round(values[entryIndex]));
                            stampTV.setText(getReadableDate(data.stamps.get(entryIndex)));
                            mLineChartView.setLabelThreshold(entryIndex,entryIndex,labelThresholdPaint);
                        }
                    });

                    LineSet prevDataSet = new LineSet(stamps, values);
                    prevDataSet.setSmooth(true);
                    prevDataSet.setColor(mContext.getResources().getColor(R.color.green_500))
                            .endAt(endIndex);
                    mLineChart.addData(prevDataSet);

                    LineSet endPointDataSet = new LineSet(stamps, values);
                    endPointDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(endIndex - 1).endAt(endIndex);
                    mLineChart.addData(endPointDataSet);

                    Paint gridPaint = new Paint();
                    gridPaint.setStrokeWidth(1.0f);
                    mLineChart.setYAxis(false);
                    gridPaint.setColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChart.setStep((maxYAxisIndex-minYAxisIndex+2)/getNumberOfGridRows(minYAxisIndex-1,maxYAxisIndex+1));
                    mLineChart.setAxisBorderValues(minYAxisIndex - 1, maxYAxisIndex + 1);
                    mLineChart.setAxisLabelsSpacing(30.0f);
                    mLineChart.setGrid(ChartView.GridType.HORIZONTAL, getNumberOfGridRows(minYAxisIndex - 1, maxYAxisIndex + 1), 1, gridPaint);
                    mLineChart.setAxisColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChartView.setLabelsColor(mContext.getResources().getColor(R.color.green_text_50));
                } else if (data.type == QuoteChartData.TYPE_ONE_YEAR) {
                    mTip.setDimensions((int) Tools.fromDpToPx(182), (int) Tools.fromDpToPx(25));

                    final int endIndex = data.stamps.size() - (data.stamps.size() / 25);

                    int stepSize = data.quotes.size() / 3;
                    if(stepSize == 0){
                        stepSize = 1;
                    }

                    int numberOfIndexs = 0;

                    final float[] values = new float[data.quotes.size()];
                    for (int i = 0; i < data.quotes.size(); i++) {
                        values[i] = Float.valueOf(String.valueOf(data.quotes.get(i)));
                    }

                    Double minimunValue = Collections.min(data.quotes);
                    Double maximunValue = Collections.max(data.quotes);

                    int maxYAxisIndex = (int) (maximunValue + (maximunValue - minimunValue) / 5);
                    int minYAxisIndex = (int) (minimunValue - (maximunValue - minimunValue) / 5);

                    String[] stamps = new String[data.stamps.size()];
                    for (int i = 0; i < data.stamps.size(); i++) {
                        if (i == 0) {
                            stamps[i] = "";
                        } else if (numberOfIndexs >= 2) {
                            stamps[i] = "";
                        } else if (i % stepSize == 0) {
                            stamps[i] = getReadableDateYear(data.stamps.get(i));
                            Log.v(LOG_TAG, " time: " + data.stamps.get(i));
                            numberOfIndexs++;
                        } else {
                            stamps[i] = "";
                        }
                    }

                    mLineChart.setTooltips(mTip);
                    mLineChart.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            valueTV.setText(""+round(values[entryIndex]));
                            stampTV.setText(getReadableDate(data.stamps.get(entryIndex))+", "+data.stamps.get(entryIndex).substring(0,4));
                            mLineChartView.setLabelThreshold(entryIndex,entryIndex,labelThresholdPaint);
                        }
                    });

                    LineSet prevDataSet = new LineSet(stamps, values);
                    prevDataSet.setSmooth(true);
                    prevDataSet.setColor(mContext.getResources().getColor(R.color.green_500))
                            .endAt(endIndex);
                    mLineChart.addData(prevDataSet);

                    LineSet endPointDataSet = new LineSet(stamps, values);
                    endPointDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(endIndex - 1).endAt(endIndex);
                    mLineChart.addData(endPointDataSet);

                    Paint gridPaint = new Paint();
                    gridPaint.setStrokeWidth(1.0f);
                    mLineChart.setYAxis(false);
                    gridPaint.setColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChart.setStep((maxYAxisIndex-minYAxisIndex+2)/getNumberOfGridRows(minYAxisIndex-1,maxYAxisIndex+1));
                    mLineChart.setAxisBorderValues(minYAxisIndex - 1, maxYAxisIndex + 1);
                    mLineChart.setAxisLabelsSpacing(30.0f);
                    mLineChart.setGrid(ChartView.GridType.HORIZONTAL, getNumberOfGridRows(minYAxisIndex - 1, maxYAxisIndex + 1), 1, gridPaint);
                    mLineChart.setAxisColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChartView.setLabelsColor(mContext.getResources().getColor(R.color.green_text_50));
                } else if (data.type == QuoteChartData.TYPE_FIVE_YEAR) {
                    mTip.setDimensions((int) Tools.fromDpToPx(182), (int) Tools.fromDpToPx(25));

                    final int endIndex = data.stamps.size() - (data.stamps.size() / 25);

                    int stepSize = data.quotes.size() / 3;
                    if(stepSize == 0){
                        stepSize = 1;
                    }

                    int numberOfIndexs = 0;

                    final float[] values = new float[data.quotes.size()];
                    for (int i = 0; i < data.quotes.size(); i++) {
                        values[i] = Float.valueOf(String.valueOf(data.quotes.get(i)));
                    }

                    Double minimunValue = Collections.min(data.quotes);
                    Double maximunValue = Collections.max(data.quotes);

                    int maxYAxisIndex = (int) (maximunValue + (maximunValue - minimunValue) / 5);
                    int minYAxisIndex = (int) (minimunValue - (maximunValue - minimunValue) / 5);

                    if(minYAxisIndex<1){
                        minYAxisIndex = 1;
                    }

                    String[] stamps = new String[data.stamps.size()];
                    for (int i = 0; i < data.stamps.size(); i++) {
                        if (i == 0) {
                            stamps[i] = "";
                        } else if (numberOfIndexs >= 2) {
                            stamps[i] = "";
                        } else if (i % stepSize == 0) {
                            stamps[i] = getReadableDataYears(data.stamps.get(i));
                            Log.v(LOG_TAG, " time: " + data.stamps.get(i));
                            numberOfIndexs++;
                        } else {
                            stamps[i] = "";
                        }
                    }

                    mLineChart.setTooltips(mTip);
                    mLineChart.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            valueTV.setText(""+round(values[entryIndex]));
                            stampTV.setText(getReadableDate(data.stamps.get(entryIndex))+", "+data.stamps.get(entryIndex).substring(0,4));
                            mLineChartView.setLabelThreshold(entryIndex,entryIndex,labelThresholdPaint);
                        }
                    });

                    LineSet prevDataSet = new LineSet(stamps, values);
                    prevDataSet.setSmooth(true);
                    prevDataSet.setColor(mContext.getResources().getColor(R.color.green_500))
                            .endAt(endIndex);
                    mLineChart.addData(prevDataSet);

                    LineSet endPointDataSet = new LineSet(stamps, values);
                    endPointDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(endIndex - 1).endAt(endIndex);
                    mLineChart.addData(endPointDataSet);

                    Paint gridPaint = new Paint();
                    gridPaint.setStrokeWidth(1.0f);
                    mLineChart.setYAxis(false);
                    gridPaint.setColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChart.setStep((maxYAxisIndex-minYAxisIndex+2)/getNumberOfGridRows(minYAxisIndex-1,maxYAxisIndex+1));
                    mLineChart.setAxisBorderValues(minYAxisIndex - 1, maxYAxisIndex + 1);
                    mLineChart.setAxisLabelsSpacing(30.0f);
                    mLineChart.setGrid(ChartView.GridType.HORIZONTAL, getNumberOfGridRows(minYAxisIndex - 1, maxYAxisIndex + 1), 1, gridPaint);
                    mLineChart.setAxisColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChartView.setLabelsColor(mContext.getResources().getColor(R.color.green_text_50));
                } else if (data.type == QuoteChartData.TYPE_MAX) {
                    mTip.setDimensions((int) Tools.fromDpToPx(182), (int) Tools.fromDpToPx(25));

                    final int endIndex = data.stamps.size() - (data.stamps.size() / 25);

                    int stepSize = data.quotes.size() / 3;
                    if(stepSize == 0){
                        stepSize = 1;
                    }

                    int numberOfIndexs = 0;

                    final float[] values = new float[data.quotes.size()];
                    for (int i = 0; i < data.quotes.size(); i++) {
                        values[i] = Float.valueOf(String.valueOf(data.quotes.get(i)));
                    }

                    Double minimunValue = Collections.min(data.quotes);
                    Double maximunValue = Collections.max(data.quotes);

                    int maxYAxisIndex = (int) (maximunValue + (maximunValue - minimunValue) / 5);
                    int minYAxisIndex = (int) (minimunValue - (maximunValue - minimunValue) / 5);
                    if(minYAxisIndex<1){
                        minYAxisIndex = 1;
                    }

                    String[] stamps = new String[data.stamps.size()];
                    for (int i = 0; i < data.stamps.size(); i++) {
                        if (i == 0) {
                            stamps[i] = "";
                        } else if (numberOfIndexs >= 2) {
                            stamps[i] = "";
                        } else if (i % stepSize == 0) {
                            stamps[i] = getReadableDataYears(data.stamps.get(i));
                            Log.v(LOG_TAG, " time: " + data.stamps.get(i));
                            numberOfIndexs++;
                        } else {
                            stamps[i] = "";
                        }
                    }

                    mLineChart.setTooltips(mTip);
                    mLineChart.setOnEntryClickListener(new OnEntryClickListener() {
                        @Override
                        public void onClick(int setIndex, int entryIndex, Rect rect) {
                            valueTV.setText(""+round(values[entryIndex]));
                            stampTV.setText(getReadableDate(data.stamps.get(entryIndex))+", "+data.stamps.get(entryIndex).substring(0,4));
                            mLineChartView.setLabelThreshold(entryIndex,entryIndex,labelThresholdPaint);
                        }
                    });

                    LineSet prevDataSet = new LineSet(stamps, values);
                    prevDataSet.setSmooth(true);
                    prevDataSet.setColor(mContext.getResources().getColor(R.color.green_500))
                            .endAt(endIndex);
                    mLineChart.addData(prevDataSet);

                    LineSet endPointDataSet = new LineSet(stamps, values);
                    endPointDataSet.setDotsColor(mContext.getResources().getColor(R.color.green_500)).setDotsRadius(14.0f).beginAt(endIndex - 1).endAt(endIndex);
                    mLineChart.addData(endPointDataSet);

                    Paint gridPaint = new Paint();
                    gridPaint.setStrokeWidth(1.0f);
                    mLineChart.setYAxis(false);
                    gridPaint.setColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChart.setStep((maxYAxisIndex-minYAxisIndex+2)/getNumberOfGridRows(minYAxisIndex-1,maxYAxisIndex+1));
                    mLineChart.setAxisBorderValues(minYAxisIndex - 1, maxYAxisIndex + 1);
                    mLineChart.setAxisLabelsSpacing(30.0f);
                    mLineChart.setGrid(ChartView.GridType.HORIZONTAL, getNumberOfGridRows(minYAxisIndex - 1, maxYAxisIndex + 1), 1, gridPaint);
                    mLineChart.setAxisColor(mContext.getResources().getColor(R.color.green_text_50));
                    mLineChartView.setLabelsColor(mContext.getResources().getColor(R.color.green_text_50));
                } else if(data.type == QuoteChartData.ERROR_FAILED_TO_LOAD_DATA){
                    if(!isConnected()){
                        doneLoading();
                        if(mStatusImage!=null){
                            mStatusImage.setVisibility(View.VISIBLE);
                            mStatusImage.setImageResource(R.drawable.pixel_icon);
                        }
                        if(mStatusText!=null){
                            mStatusText.setVisibility(View.VISIBLE);
                            mStatusText.setText(R.string.no_internet);
                        }
                        startLoading();
                    }else {
                        if(this.numberOfTries<3) {
                            this.numberOfTries++;
                            startLoading();
                        }else {
                            doneLoading();
                            if(mStatusImage!=null){
                                mStatusImage.setVisibility(View.VISIBLE);
                                mStatusImage.setImageResource(R.drawable.pixel_icon);
                            }
                            if(mStatusText!=null){
                                mStatusText.setVisibility(View.VISIBLE);
                                mStatusText.setText(R.string.loading_failed);
                            }
                            startLoading();
                        }
                    }
                } else if(data.type == QuoteChartData.ERROR_ERROR_IN_LOADED_DATA){
                    if(this.numberOfTries<3) {
                        this.numberOfTries++;
                        startLoading();
                    }else {
                        doneLoading();
                        if(mStatusImage!=null){
                            mStatusImage.setVisibility(View.VISIBLE);
                            mStatusImage.setImageResource(R.drawable.pixel_icon);
                        }
                        if(mStatusText!=null){
                            mStatusText.setVisibility(View.VISIBLE);
                            mStatusText.setText(R.string.error_string);
                        }
                        startLoading();
                    }
                }

                if(data.type!=QuoteChartData.ERROR_ERROR_IN_LOADED_DATA && data.type!=QuoteChartData.ERROR_FAILED_TO_LOAD_DATA) {
                    mLineChart.show();
                }
            }

            super.deliverResult(data);
        }

        boolean isConnected(){
            ConnectivityManager cm =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        void doneLoading(){
            if(mLoadingProgress!=null) {
                mLoadingProgress.setVisibility(View.GONE);
            }
            if(mLoadingFilter!=null){
                mLoadingFilter.setVisibility(View.GONE);
            }
        }

        float valueToPixels(float deltaPixel, float deltaValue,float prevValue){
            return (1 - prevValue/deltaValue)*deltaPixel;
        }

        float round(float f){
            if(f<100f){
                return Math.round(f*100f)/100f;
            }else if(f<1000f) {
                return Math.round(f*10f)/10f;
            }else {
                return Math.round(f);
            }
        }
        String getReadableDate(String stamp){
            int month = Integer.valueOf(stamp.substring(4,6));
            String dayString = stamp.substring(6,8);
            return getMonthName(month)+" "+dayString;
        }

        String getReadableDateYear(String stamp){
            int month = Integer.valueOf(stamp.substring(4,6));
            return getMonthName(month)+" "+stamp.substring(0,4);
        }

        String getReadableDateFromTimeStamp(String stamp){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(stamp)*1000);
            return getMonthName(calendar.get(Calendar.MONTH)+1)+" "+appendZero(calendar.get(Calendar.DAY_OF_MONTH));
        }

        String getReadableDataYears(String stamp){
            return stamp.substring(0,4);
        }

        String getMonthName(int month){
            switch (month){
                case 1:return "Jan";
                case 2:return "Feb";
                case 3:return "Mar";
                case 4:return "Apr";
                case 5:return "May";
                case 6:return "Jun";
                case 7:return "Jul";
                case 8:return "Aug";
                case 9:return "Sep";
                case 10:return "Oct";
                case 11:return "Nov";
                case 12:return "Dec";
                default:return "Jan";
            }
        }

        int getNumberOfGridRows(int yaxisMin,int yaxiMax){
            int delta = yaxiMax - yaxisMin;

            for (int i=2;i<=delta;i++){
                if(delta%i ==0){
                    if(i>10){
                        return 1;
                    }else {
                        return i;
                    }
                }
            }
            return 1;
        }

//        boolean isPrime(int n) {
//            //check if n is a multiple of 2
//            if (n%2==0) return false;
//            //if not, then just check the odds
//            for(int i=3;i*i<=n;i+=2) {
//                if(n%i==0)
//                    return false;
//            }
//            return true;
//        }
//
        String getReadableTime(String timestamp){
            Time time = new Time(Long.valueOf(timestamp)*1000);
            Log.v(LOG_TAG," time: long conversion: "+Long.valueOf(timestamp));
            //calendar.setTimeInMillis(Long.valueOf(timestamp));
            int hour = time.getHours();
            Log.v(LOG_TAG," time: hour: "+hour);
            int minute = time.getMinutes();
            Log.v(LOG_TAG," time: minute: "+minute);

            return getHourString(hour)+":"+appendZero(minute)+" "+getAMorPM(hour);
        }

        String getHourString(int hour){
            if(hour == 0){
                return "12";
            }else{
                if(hour > 12){
                    if((hour-12)>=1 && (hour-12)<=9){
                        return "0"+(hour-12);
                    }else {
                        return ""+(hour-12);
                    }
                }else {
                    if(hour>=1 && hour<=9){
                        return "0"+hour;
                    }else {
                        return ""+hour;
                    }
                }
            }
        }

        String getAMorPM(int hour){
            if(hour<12){
                return "AM";
            }else {
                return "PM";
            }
        }

        String appendZero(int i){
            if(i>=0 && i<=9){
                return "0"+i;
            }else {
                return ""+i;
            }
        }

        @Override
        protected void onStartLoading() {
            if(mSpan.equals("1d")){
                if (mQuoteChartDataOneDay != null) {
                    deliverResult(mQuoteChartDataOneDay);
                }else {
                    forceLoad();
                }
            }else if(mSpan.equals("7d")){
                if (mQuoteChartDataOneWeek != null) {
                    deliverResult(mQuoteChartDataOneWeek);
                }else {
                    forceLoad();
                }
            }else if(mSpan.equals("1m")){
                if (mQuoteChartDataOneMonth != null) {
                    deliverResult(mQuoteChartDataOneMonth);
                }else {
                    forceLoad();
                }
            }else if(mSpan.equals("1y")){
                if (mQuoteChartDataOneYear != null) {
                    deliverResult(mQuoteChartDataOneYear);
                }else {
                    forceLoad();
                }
            }else if(mSpan.equals("5y")){
                if (mQuoteChartDataFiveYears != null) {
                    deliverResult(mQuoteChartDataFiveYears);
                }else {
                    forceLoad();
                }
            }else if(mSpan.equals("max")){
                if (mQuoteChartDataMax != null) {
                    deliverResult(mQuoteChartDataMax);
                }else {
                    forceLoad();
                }
            }
        }
    }
}
