package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */

public class StockTaskService extends GcmTaskService{
  public static final String ACTION_DETAIL_DATA_UPDATED = "com.example.hp.sunshine.ACTION_DETAIL_DATA_UPDATED";

  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }
  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
            .url(url)
            .build();

    Log.v(LOG_TAG, " request: "+request.toString());
    Log.v(LOG_TAG, " url: "+url);
    Response response = client.newCall(request).execute();
    Log.v(LOG_TAG, " response body: "+response.body());
    Log.v(LOG_TAG, " response message: "+response.message());
    return response.body().string();

    //return getDummyString();
  }

  String getDummyString(){
    return "{\n" +
            " \"query\": {\n" +
            "  \"count\": 4,\n" +
            "  \"created\": \"2016-10-24T15:45:16Z\",\n" +
            "  \"lang\": \"en-US\",\n" +
            "  \"diagnostics\": {\n" +
            "   \"url\": [\n" +
            "    {\n" +
            "     \"execution-start-time\": \"1\",\n" +
            "     \"execution-stop-time\": \"2\",\n" +
            "     \"execution-time\": \"1\",\n" +
            "     \"content\": \"http://www.datatables.org/yahoo/finance/yahoo.finance.quotes.xml\"\n" +
            "    },\n" +
            "    {\n" +
            "     \"execution-start-time\": \"42\",\n" +
            "     \"execution-stop-time\": \"56\",\n" +
            "     \"execution-time\": \"14\",\n" +
            "     \"content\": \"http://download.finance.yahoo.com/d/quotes.csv?f=aa2bb2b3b4cc1c3c4c6c8dd1d2ee1e7e8e9ghjkg1g3g4g5g6ii5j1j3j4j5j6k1k2k4k5ll1l2l3mm2m3m4m5m6m7m8nn4opp1p2p5p6qrr1r2r5r6r7ss1s7t1t7t8vv1v7ww1w4xy&s=YHOO,MSFT,GOOG,AAPL\"\n" +
            "    }\n" +
            "   ],\n" +
            "   \"publiclyCallable\": \"true\",\n" +
            "   \"cache\": {\n" +
            "    \"execution-start-time\": \"5\",\n" +
            "    \"execution-stop-time\": \"41\",\n" +
            "    \"execution-time\": \"36\",\n" +
            "    \"method\": \"GET\",\n" +
            "    \"type\": \"MEMCACHED\",\n" +
            "    \"content\": \"5d1e1de680846a307c9874dc3d6878dc\"\n" +
            "   },\n" +
            "   \"query\": {\n" +
            "    \"execution-start-time\": \"41\",\n" +
            "    \"execution-stop-time\": \"57\",\n" +
            "    \"execution-time\": \"16\",\n" +
            "    \"params\": \"{url=[http://download.finance.yahoo.com/d/quotes.csv?f=aa2bb2b3b4cc1c3c4c6c8dd1d2ee1e7e8e9ghjkg1g3g4g5g6ii5j1j3j4j5j6k1k2k4k5ll1l2l3mm2m3m4m5m6m7m8nn4opp1p2p5p6qrr1r2r5r6r7ss1s7t1t7t8vv1v7ww1w4xy&s=YHOO,MSFT,GOOG,AAPL]}\",\n" +
            "    \"content\": \"select * from csv where url=@url and columns='Ask,AverageDailyVolume,Bid,AskRealtime,BidRealtime,BookValue,Change&PercentChange,Change,Commission,Currency,ChangeRealtime,AfterHoursChangeRealtime,DividendShare,LastTradeDate,TradeDate,EarningsShare,ErrorIndicationreturnedforsymbolchangedinvalid,EPSEstimateCurrentYear,EPSEstimateNextYear,EPSEstimateNextQuarter,DaysLow,DaysHigh,YearLow,YearHigh,HoldingsGainPercent,AnnualizedGain,HoldingsGain,HoldingsGainPercentRealtime,HoldingsGainRealtime,MoreInfo,OrderBookRealtime,MarketCapitalization,MarketCapRealtime,EBITDA,ChangeFromYearLow,PercentChangeFromYearLow,LastTradeRealtimeWithTime,ChangePercentRealtime,ChangeFromYearHigh,PercebtChangeFromYearHigh,LastTradeWithTime,LastTradePriceOnly,HighLimit,LowLimit,DaysRange,DaysRangeRealtime,FiftydayMovingAverage,TwoHundreddayMovingAverage,ChangeFromTwoHundreddayMovingAverage,PercentChangeFromTwoHundreddayMovingAverage,ChangeFromFiftydayMovingAverage,PercentChangeFromFiftydayMovingAverage,Name,Notes,Open,PreviousClose,PricePaid,ChangeinPercent,PriceSales,PriceBook,ExDividendDate,PERatio,DividendPayDate,PERatioRealtime,PEGRatio,PriceEPSEstimateCurrentYear,PriceEPSEstimateNextYear,Symbol,SharesOwned,ShortRatio,LastTradeTime,TickerTrend,OneyrTargetPrice,Volume,HoldingsValue,HoldingsValueRealtime,YearRange,DaysValueChange,DaysValueChangeRealtime,StockExchange,DividendYield'\"\n" +
            "   },\n" +
            "   \"javascript\": {\n" +
            "    \"execution-start-time\": \"3\",\n" +
            "    \"execution-stop-time\": \"84\",\n" +
            "    \"execution-time\": \"80\",\n" +
            "    \"instructions-used\": \"260257\",\n" +
            "    \"table-name\": \"yahoo.finance.quotes\"\n" +
            "   },\n" +
            "   \"user-time\": \"86\",\n" +
            "   \"service-time\": \"51\",\n" +
            "   \"build-version\": \"2.0.75\"\n" +
            "  },\n" +
            "  \"results\": {\n" +
            "   \"quote\": [\n" +
            "    {\n" +
            "     \"symbol\": \"YHOO\",\n" +
            "     \"Ask\": \"42.520\",\n" +
            "     \"AverageDailyVolume\": \"11499500\",\n" +
            "     \"Bid\": \"42.510\",\n" +
            "     \"AskRealtime\": null,\n" +
            "     \"BidRealtime\": null,\n" +
            "     \"BookValue\": \"36.390\",\n" +
            "     \"Change_PercentChange\": \"+0.345 - +0.818%\",\n" +
            "     \"Change\": \"+0.345\",\n" +
            "     \"Commission\": null,\n" +
            "     \"Currency\": \"USD\",\n" +
            "     \"ChangeRealtime\": null,\n" +
            "     \"AfterHoursChangeRealtime\": null,\n" +
            "     \"DividendShare\": null,\n" +
            "     \"LastTradeDate\": \"10/24/2016\",\n" +
            "     \"TradeDate\": null,\n" +
            "     \"EarningsShare\": \"-5.107\",\n" +
            "     \"ErrorIndicationreturnedforsymbolchangedinvalid\": null,\n" +
            "     \"EPSEstimateCurrentYear\": \"0.490\",\n" +
            "     \"EPSEstimateNextYear\": \"0.570\",\n" +
            "     \"EPSEstimateNextQuarter\": \"0.170\",\n" +
            "     \"DaysLow\": \"42.380\",\n" +
            "     \"DaysHigh\": \"42.680\",\n" +
            "     \"YearLow\": \"26.150\",\n" +
            "     \"YearHigh\": \"44.920\",\n" +
            "     \"HoldingsGainPercent\": null,\n" +
            "     \"AnnualizedGain\": null,\n" +
            "     \"HoldingsGain\": null,\n" +
            "     \"HoldingsGainPercentRealtime\": null,\n" +
            "     \"HoldingsGainRealtime\": null,\n" +
            "     \"MoreInfo\": null,\n" +
            "     \"OrderBookRealtime\": null,\n" +
            "     \"MarketCapitalization\": \"40.70B\",\n" +
            "     \"MarketCapRealtime\": null,\n" +
            "     \"EBITDA\": \"90.38M\",\n" +
            "     \"ChangeFromYearLow\": \"16.365\",\n" +
            "     \"PercentChangeFromYearLow\": \"+62.581%\",\n" +
            "     \"LastTradeRealtimeWithTime\": null,\n" +
            "     \"ChangePercentRealtime\": null,\n" +
            "     \"ChangeFromYearHigh\": \"-2.405\",\n" +
            "     \"PercebtChangeFromYearHigh\": \"-5.354%\",\n" +
            "     \"LastTradeWithTime\": \"11:30am - <b>42.515</b>\",\n" +
            "     \"LastTradePriceOnly\": \"42.515\",\n" +
            "     \"HighLimit\": null,\n" +
            "     \"LowLimit\": null,\n" +
            "     \"DaysRange\": \"42.380 - 42.680\",\n" +
            "     \"DaysRangeRealtime\": null,\n" +
            "     \"FiftydayMovingAverage\": \"43.110\",\n" +
            "     \"TwoHundreddayMovingAverage\": \"39.363\",\n" +
            "     \"ChangeFromTwoHundreddayMovingAverage\": \"3.152\",\n" +
            "     \"PercentChangeFromTwoHundreddayMovingAverage\": \"+8.009%\",\n" +
            "     \"ChangeFromFiftydayMovingAverage\": \"-0.595\",\n" +
            "     \"PercentChangeFromFiftydayMovingAverage\": \"-1.380%\",\n" +
            "     \"Name\": \"Yahoo! Inc.\",\n" +
            "     \"Notes\": null,\n" +
            "     \"Open\": \"42.500\",\n" +
            "     \"PreviousClose\": \"42.170\",\n" +
            "     \"PricePaid\": null,\n" +
            "     \"ChangeinPercent\": \"+0.818%\",\n" +
            "     \"PriceSales\": \"8.117\",\n" +
            "     \"PriceBook\": \"1.159\",\n" +
            "     \"ExDividendDate\": null,\n" +
            "     \"PERatio\": null,\n" +
            "     \"DividendPayDate\": null,\n" +
            "     \"PERatioRealtime\": null,\n" +
            "     \"PEGRatio\": \"-24.570\",\n" +
            "     \"PriceEPSEstimateCurrentYear\": \"86.765\",\n" +
            "     \"PriceEPSEstimateNextYear\": \"74.588\",\n" +
            "     \"Symbol\": \"YHOO\",\n" +
            "     \"SharesOwned\": null,\n" +
            "     \"ShortRatio\": \"3.780\",\n" +
            "     \"LastTradeTime\": \"11:30am\",\n" +
            "     \"TickerTrend\": null,\n" +
            "     \"OneyrTargetPrice\": \"45.280\",\n" +
            "     \"Volume\": \"1329628\",\n" +
            "     \"HoldingsValue\": null,\n" +
            "     \"HoldingsValueRealtime\": null,\n" +
            "     \"YearRange\": \"26.150 - 44.920\",\n" +
            "     \"DaysValueChange\": null,\n" +
            "     \"DaysValueChangeRealtime\": null,\n" +
            "     \"StockExchange\": \"NMS\",\n" +
            "     \"DividendYield\": null,\n" +
            "     \"PercentChange\": \"+0.818%\"\n" +
            "    },\n" +
            "    {\n" +
            "     \"symbol\": \"MSFT\",\n" +
            "     \"Ask\": \"60.600\",\n" +
            "     \"AverageDailyVolume\": \"24414900\",\n" +
            "     \"Bid\": \"60.590\",\n" +
            "     \"AskRealtime\": null,\n" +
            "     \"BidRealtime\": null,\n" +
            "     \"BookValue\": \"9.041\",\n" +
            "     \"Change_PercentChange\": \"+0.934 - +1.566%\",\n" +
            "     \"Change\": \"+0.934\",\n" +
            "     \"Commission\": null,\n" +
            "     \"Currency\": \"USD\",\n" +
            "     \"ChangeRealtime\": null,\n" +
            "     \"AfterHoursChangeRealtime\": null,\n" +
            "     \"DividendShare\": \"1.560\",\n" +
            "     \"LastTradeDate\": \"10/24/2016\",\n" +
            "     \"TradeDate\": null,\n" +
            "     \"EarningsShare\": \"2.088\",\n" +
            "     \"ErrorIndicationreturnedforsymbolchangedinvalid\": null,\n" +
            "     \"EPSEstimateCurrentYear\": \"2.890\",\n" +
            "     \"EPSEstimateNextYear\": \"3.220\",\n" +
            "     \"EPSEstimateNextQuarter\": \"0.790\",\n" +
            "     \"DaysLow\": \"59.930\",\n" +
            "     \"DaysHigh\": \"60.700\",\n" +
            "     \"YearLow\": \"48.040\",\n" +
            "     \"YearHigh\": \"60.700\",\n" +
            "     \"HoldingsGainPercent\": null,\n" +
            "     \"AnnualizedGain\": null,\n" +
            "     \"HoldingsGain\": null,\n" +
            "     \"HoldingsGainPercentRealtime\": null,\n" +
            "     \"HoldingsGainRealtime\": null,\n" +
            "     \"MoreInfo\": null,\n" +
            "     \"OrderBookRealtime\": null,\n" +
            "     \"MarketCapitalization\": \"471.14B\",\n" +
            "     \"MarketCapRealtime\": null,\n" +
            "     \"EBITDA\": \"26.96B\",\n" +
            "     \"ChangeFromYearLow\": \"12.554\",\n" +
            "     \"PercentChangeFromYearLow\": \"+26.132%\",\n" +
            "     \"LastTradeRealtimeWithTime\": null,\n" +
            "     \"ChangePercentRealtime\": null,\n" +
            "     \"ChangeFromYearHigh\": \"-0.106\",\n" +
            "     \"PercebtChangeFromYearHigh\": \"-0.175%\",\n" +
            "     \"LastTradeWithTime\": \"11:30am - <b>60.594</b>\",\n" +
            "     \"LastTradePriceOnly\": \"60.594\",\n" +
            "     \"HighLimit\": null,\n" +
            "     \"LowLimit\": null,\n" +
            "     \"DaysRange\": \"59.930 - 60.700\",\n" +
            "     \"DaysRangeRealtime\": null,\n" +
            "     \"FiftydayMovingAverage\": \"57.409\",\n" +
            "     \"TwoHundreddayMovingAverage\": \"54.548\",\n" +
            "     \"ChangeFromTwoHundreddayMovingAverage\": \"6.047\",\n" +
            "     \"PercentChangeFromTwoHundreddayMovingAverage\": \"+11.085%\",\n" +
            "     \"ChangeFromFiftydayMovingAverage\": \"3.185\",\n" +
            "     \"PercentChangeFromFiftydayMovingAverage\": \"+5.547%\",\n" +
            "     \"Name\": \"Microsoft Corporation\",\n" +
            "     \"Notes\": null,\n" +
            "     \"Open\": \"59.940\",\n" +
            "     \"PreviousClose\": \"59.660\",\n" +
            "     \"PricePaid\": null,\n" +
            "     \"ChangeinPercent\": \"+1.566%\",\n" +
            "     \"PriceSales\": \"5.432\",\n" +
            "     \"PriceBook\": \"6.599\",\n" +
            "     \"ExDividendDate\": \"8/16/2016\",\n" +
            "     \"PERatio\": \"29.020\",\n" +
            "     \"DividendPayDate\": \"12/8/2016\",\n" +
            "     \"PERatioRealtime\": null,\n" +
            "     \"PEGRatio\": \"2.320\",\n" +
            "     \"PriceEPSEstimateCurrentYear\": \"20.967\",\n" +
            "     \"PriceEPSEstimateNextYear\": \"18.818\",\n" +
            "     \"Symbol\": \"MSFT\",\n" +
            "     \"SharesOwned\": null,\n" +
            "     \"ShortRatio\": \"1.940\",\n" +
            "     \"LastTradeTime\": \"11:30am\",\n" +
            "     \"TickerTrend\": null,\n" +
            "     \"OneyrTargetPrice\": \"59.790\",\n" +
            "     \"Volume\": \"16981252\",\n" +
            "     \"HoldingsValue\": null,\n" +
            "     \"HoldingsValueRealtime\": null,\n" +
            "     \"YearRange\": \"48.040 - 60.700\",\n" +
            "     \"DaysValueChange\": null,\n" +
            "     \"DaysValueChangeRealtime\": null,\n" +
            "     \"StockExchange\": \"NMS\",\n" +
            "     \"DividendYield\": \"2.610\",\n" +
            "     \"PercentChange\": \"+1.566%\"\n" +
            "    },\n" +
            "    {\n" +
            "     \"symbol\": \"GOOG\",\n" +
            "     \"Ask\": \"812.1100\",\n" +
            "     \"AverageDailyVolume\": \"1321270\",\n" +
            "     \"Bid\": \"811.7300\",\n" +
            "     \"AskRealtime\": null,\n" +
            "     \"BidRealtime\": null,\n" +
            "     \"BookValue\": \"186.2010\",\n" +
            "     \"Change_PercentChange\": \"+12.7487 - +1.5948%\",\n" +
            "     \"Change\": \"+12.7487\",\n" +
            "     \"Commission\": null,\n" +
            "     \"Currency\": \"USD\",\n" +
            "     \"ChangeRealtime\": null,\n" +
            "     \"AfterHoursChangeRealtime\": null,\n" +
            "     \"DividendShare\": null,\n" +
            "     \"LastTradeDate\": \"10/24/2016\",\n" +
            "     \"TradeDate\": null,\n" +
            "     \"EarningsShare\": \"25.8090\",\n" +
            "     \"ErrorIndicationreturnedforsymbolchangedinvalid\": null,\n" +
            "     \"EPSEstimateCurrentYear\": \"34.1400\",\n" +
            "     \"EPSEstimateNextYear\": \"40.4800\",\n" +
            "     \"EPSEstimateNextQuarter\": \"9.6700\",\n" +
            "     \"DaysLow\": \"804.8200\",\n" +
            "     \"DaysHigh\": \"812.2900\",\n" +
            "     \"YearLow\": \"663.0600\",\n" +
            "     \"YearHigh\": \"812.2900\",\n" +
            "     \"HoldingsGainPercent\": null,\n" +
            "     \"AnnualizedGain\": null,\n" +
            "     \"HoldingsGain\": null,\n" +
            "     \"HoldingsGainPercentRealtime\": null,\n" +
            "     \"HoldingsGainRealtime\": null,\n" +
            "     \"MoreInfo\": null,\n" +
            "     \"OrderBookRealtime\": null,\n" +
            "     \"MarketCapitalization\": \"558.15B\",\n" +
            "     \"MarketCapRealtime\": null,\n" +
            "     \"EBITDA\": \"26.90B\",\n" +
            "     \"ChangeFromYearLow\": \"149.0587\",\n" +
            "     \"PercentChangeFromYearLow\": \"+22.4804%\",\n" +
            "     \"LastTradeRealtimeWithTime\": null,\n" +
            "     \"ChangePercentRealtime\": null,\n" +
            "     \"ChangeFromYearHigh\": \"-0.1713\",\n" +
            "     \"PercebtChangeFromYearHigh\": \"-0.0211%\",\n" +
            "     \"LastTradeWithTime\": \"11:29am - <b>812.1187</b>\",\n" +
            "     \"LastTradePriceOnly\": \"812.1187\",\n" +
            "     \"HighLimit\": null,\n" +
            "     \"LowLimit\": null,\n" +
            "     \"DaysRange\": \"804.8200 - 812.2900\",\n" +
            "     \"DaysRangeRealtime\": null,\n" +
            "     \"FiftydayMovingAverage\": \"778.2730\",\n" +
            "     \"TwoHundreddayMovingAverage\": \"743.5660\",\n" +
            "     \"ChangeFromTwoHundreddayMovingAverage\": \"68.5527\",\n" +
            "     \"PercentChangeFromTwoHundreddayMovingAverage\": \"+9.2195%\",\n" +
            "     \"ChangeFromFiftydayMovingAverage\": \"33.8457\",\n" +
            "     \"PercentChangeFromFiftydayMovingAverage\": \"+4.3488%\",\n" +
            "     \"Name\": \"Alphabet Inc.\",\n" +
            "     \"Notes\": null,\n" +
            "     \"Open\": \"804.9000\",\n" +
            "     \"PreviousClose\": \"799.3700\",\n" +
            "     \"PricePaid\": null,\n" +
            "     \"ChangeinPercent\": \"+1.5948%\",\n" +
            "     \"PriceSales\": \"6.7194\",\n" +
            "     \"PriceBook\": \"4.2930\",\n" +
            "     \"ExDividendDate\": null,\n" +
            "     \"PERatio\": \"31.4665\",\n" +
            "     \"DividendPayDate\": null,\n" +
            "     \"PERatioRealtime\": null,\n" +
            "     \"PEGRatio\": \"1.2600\",\n" +
            "     \"PriceEPSEstimateCurrentYear\": \"23.7879\",\n" +
            "     \"PriceEPSEstimateNextYear\": \"20.0622\",\n" +
            "     \"Symbol\": \"GOOG\",\n" +
            "     \"SharesOwned\": null,\n" +
            "     \"ShortRatio\": \"1.6500\",\n" +
            "     \"LastTradeTime\": \"11:29am\",\n" +
            "     \"TickerTrend\": null,\n" +
            "     \"OneyrTargetPrice\": \"937.3300\",\n" +
            "     \"Volume\": \"558897\",\n" +
            "     \"HoldingsValue\": null,\n" +
            "     \"HoldingsValueRealtime\": null,\n" +
            "     \"YearRange\": \"663.0600 - 812.2900\",\n" +
            "     \"DaysValueChange\": null,\n" +
            "     \"DaysValueChangeRealtime\": null,\n" +
            "     \"StockExchange\": \"NMS\",\n" +
            "     \"DividendYield\": null,\n" +
            "     \"PercentChange\": \"+1.5948%\"\n" +
            "    },\n" +
            "    {\n" +
            "     \"symbol\": \"AAPL\",\n" +
            "     \"Ask\": \"117.3800\",\n" +
            "     \"AverageDailyVolume\": \"35884500\",\n" +
            "     \"Bid\": \"117.3700\",\n" +
            "     \"AskRealtime\": null,\n" +
            "     \"BidRealtime\": null,\n" +
            "     \"BookValue\": \"23.4630\",\n" +
            "     \"Change_PercentChange\": \"+0.7775 - +0.6668%\",\n" +
            "     \"Change\": \"+0.7775\",\n" +
            "     \"Commission\": null,\n" +
            "     \"Currency\": \"USD\",\n" +
            "     \"ChangeRealtime\": null,\n" +
            "     \"AfterHoursChangeRealtime\": null,\n" +
            "     \"DividendShare\": \"2.2800\",\n" +
            "     \"LastTradeDate\": \"10/24/2016\",\n" +
            "     \"TradeDate\": null,\n" +
            "     \"EarningsShare\": \"8.5760\",\n" +
            "     \"ErrorIndicationreturnedforsymbolchangedinvalid\": null,\n" +
            "     \"EPSEstimateCurrentYear\": \"8.2600\",\n" +
            "     \"EPSEstimateNextYear\": \"8.9600\",\n" +
            "     \"EPSEstimateNextQuarter\": \"3.1700\",\n" +
            "     \"DaysLow\": \"117.0000\",\n" +
            "     \"DaysHigh\": \"117.5900\",\n" +
            "     \"YearLow\": \"89.4700\",\n" +
            "     \"YearHigh\": \"123.8200\",\n" +
            "     \"HoldingsGainPercent\": null,\n" +
            "     \"AnnualizedGain\": null,\n" +
            "     \"HoldingsGain\": null,\n" +
            "     \"HoldingsGainPercentRealtime\": null,\n" +
            "     \"HoldingsGainRealtime\": null,\n" +
            "     \"MoreInfo\": null,\n" +
            "     \"OrderBookRealtime\": null,\n" +
            "     \"MarketCapitalization\": \"632.48B\",\n" +
            "     \"MarketCapRealtime\": null,\n" +
            "     \"EBITDA\": \"73.96B\",\n" +
            "     \"ChangeFromYearLow\": \"27.9075\",\n" +
            "     \"PercentChangeFromYearLow\": \"+31.1920%\",\n" +
            "     \"LastTradeRealtimeWithTime\": null,\n" +
            "     \"ChangePercentRealtime\": null,\n" +
            "     \"ChangeFromYearHigh\": \"-6.4425\",\n" +
            "     \"PercebtChangeFromYearHigh\": \"-5.2031%\",\n" +
            "     \"LastTradeWithTime\": \"11:30am - <b>117.3775</b>\",\n" +
            "     \"LastTradePriceOnly\": \"117.3775\",\n" +
            "     \"HighLimit\": null,\n" +
            "     \"LowLimit\": null,\n" +
            "     \"DaysRange\": \"117.0000 - 117.5900\",\n" +
            "     \"DaysRangeRealtime\": null,\n" +
            "     \"FiftydayMovingAverage\": \"113.0830\",\n" +
            "     \"TwoHundreddayMovingAverage\": \"103.9190\",\n" +
            "     \"ChangeFromTwoHundreddayMovingAverage\": \"13.4585\",\n" +
            "     \"PercentChangeFromTwoHundreddayMovingAverage\": \"+12.9510%\",\n" +
            "     \"ChangeFromFiftydayMovingAverage\": \"4.2945\",\n" +
            "     \"PercentChangeFromFiftydayMovingAverage\": \"+3.7977%\",\n" +
            "     \"Name\": \"Apple Inc.\",\n" +
            "     \"Notes\": null,\n" +
            "     \"Open\": \"117.1000\",\n" +
            "     \"PreviousClose\": \"116.6000\",\n" +
            "     \"PricePaid\": null,\n" +
            "     \"ChangeinPercent\": \"+0.6668%\",\n" +
            "     \"PriceSales\": \"2.8521\",\n" +
            "     \"PriceBook\": \"4.9695\",\n" +
            "     \"ExDividendDate\": \"8/4/2016\",\n" +
            "     \"PERatio\": \"13.6867\",\n" +
            "     \"DividendPayDate\": \"8/11/2016\",\n" +
            "     \"PERatioRealtime\": null,\n" +
            "     \"PEGRatio\": \"1.7500\",\n" +
            "     \"PriceEPSEstimateCurrentYear\": \"14.2104\",\n" +
            "     \"PriceEPSEstimateNextYear\": \"13.1002\",\n" +
            "     \"Symbol\": \"AAPL\",\n" +
            "     \"SharesOwned\": null,\n" +
            "     \"ShortRatio\": \"1.2300\",\n" +
            "     \"LastTradeTime\": \"11:30am\",\n" +
            "     \"TickerTrend\": null,\n" +
            "     \"OneyrTargetPrice\": \"128.3200\",\n" +
            "     \"Volume\": \"9497652\",\n" +
            "     \"HoldingsValue\": null,\n" +
            "     \"HoldingsValueRealtime\": null,\n" +
            "     \"YearRange\": \"89.4700 - 123.8200\",\n" +
            "     \"DaysValueChange\": null,\n" +
            "     \"DaysValueChangeRealtime\": null,\n" +
            "     \"StockExchange\": \"NMS\",\n" +
            "     \"DividendYield\": \"1.9600\",\n" +
            "     \"PercentChange\": \"+0.6668%\"\n" +
            "    }\n" +
            "   ]\n" +
            "  }\n" +
            " }\n" +
            "}";
  }

  @Override
  public int onRunTask(TaskParams params){
    Cursor initQueryCursor;
    Intent intent = new Intent(ACTION_DETAIL_DATA_UPDATED);
    if (mContext == null){
      mContext = this;
    }
    StringBuilder urlStringBuilder = new StringBuilder();
    try{
      // Base URL for the Yahoo query
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
      urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
              + "in (", "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
              new String[] { "Distinct " + QuoteColumns.SYMBOL }, null,
              null, null);
      if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
        // Init task. Populates DB with quotes for the symbols seen below
        try {
          urlStringBuilder.append(
                  URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else if (initQueryCursor != null){
        DatabaseUtils.dumpCursor(initQueryCursor);
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++){
          mStoredSymbols.append("\""+
                  initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"))+"\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
        try {
          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    } else if (params.getTag().equals("add")){
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString("symbol");
      try {
        urlStringBuilder.append(URLEncoder.encode("\""+stockInput+"\")", "UTF-8"));
      } catch (UnsupportedEncodingException e){
        e.printStackTrace();
      }
    }
    // finalize the URL for the API query.
    urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
            + "org%2Falltableswithkeys&callback=");

    String urlString;
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;

    if (urlStringBuilder != null){
      urlString = urlStringBuilder.toString();
      try{
        getResponse = fetchData(urlString);
        result = GcmNetworkManager.RESULT_SUCCESS;
        try {
          ContentValues contentValues = new ContentValues();
          // update ISCURRENT to 0 (false) so new data is current
          if (isUpdate){
            contentValues.put(QuoteColumns.ISCURRENT, 0);
            mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                    null, null);
          }
          JSONObject jsonObject = new JSONObject(getResponse);
          if (jsonObject != null && jsonObject.length() != 0){
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1){
              String name = jsonObject.getJSONObject("results")
                      .getJSONObject("quote").getString("Name");
              if(!name.equals("null")) {
                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                        Utils.quoteJsonToContentVals(getResponse));
                mContext.sendBroadcast(intent);
              }else{
                Log.v(LOG_TAG, " this stock doesn't exist");
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                sharedPreferences.putString(mContext.getString(R.string.stock_exists_key),mContext.getString(R.string.not_stock__exists));
                sharedPreferences.commit();
              }
            }else {
              mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                      Utils.quoteJsonToContentVals(getResponse));
              mContext.sendBroadcast(intent);
            }
          }
        }catch (RemoteException | OperationApplicationException e){
          Log.e(LOG_TAG, "Error applying batch insert", e);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      } catch (IOException e){
        e.printStackTrace();
      }
    }

    return result;
  }

}
