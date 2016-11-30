package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.FloatProperty;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import java.util.jar.Pack200;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    if(bidPrice!=null) {
      if(!bidPrice.equals("null")) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
      }else{
        return "n/a";
      }
    }else {
      return "n/a";
    }
  }

  public static String truncateChange(String change, boolean isPercentChange){
    if(!change.equals("null")) {
      String weight = change.substring(0, 1);
      String ampersand = "";
      if (isPercentChange) {
        ampersand = change.substring(change.length() - 1, change.length());
        change = change.substring(0, change.length() - 1);
      }
      change = change.substring(1, change.length());
      double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
      change = String.format("%.2f", round);
      StringBuffer changeBuffer = new StringBuffer(change);
      changeBuffer.insert(0, weight);
      changeBuffer.append(ampersand);
      change = changeBuffer.toString();
      return change;
    }else {
      return "n/a";
    }
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String name = jsonObject.getString("Name");
      if(name.equals("null")){
        Log.v(LOG_TAG, "stock doesn't exist");
      }else {

        String change = jsonObject.getString("Change");
        builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
        builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                jsonObject.getString("ChangeinPercent"), true));
        String changeInPercent = jsonObject.getString("ChangeinPercent");
        if(changeInPercent.equals("null")) {
          builder.withValue(QuoteColumns.PERCENT_CHANGE_REAL, 0);
        }else {
          builder.withValue(QuoteColumns.PERCENT_CHANGE_REAL, Double.valueOf(changeInPercent.substring(0,changeInPercent.length()-1)));
        }
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-') {
          builder.withValue(QuoteColumns.ISUP, 0);
        } else {
          builder.withValue(QuoteColumns.ISUP, 1);
        }
        builder.withValue(QuoteColumns.NAME, name);
        builder.withValue(QuoteColumns.CURRENCY, jsonObject.getString("Currency"));
        builder.withValue(QuoteColumns.OPEN, truncateBidPrice(jsonObject.getString("Open")));
        builder.withValue(QuoteColumns.LOW, truncateBidPrice(jsonObject.getString("DaysLow")));
        builder.withValue(QuoteColumns.HIGH, truncateBidPrice(jsonObject.getString("DaysHigh")));

        String divYield = jsonObject.getString("DividendYield");
        String marketCap = jsonObject.getString("MarketCapitalization");
        String peRatio = jsonObject.getString("PERatio");

        if(divYield.equals("null")){
          builder.withValue(QuoteColumns.DIV_YIELD,"n/a");
        }else {
          builder.withValue(QuoteColumns.DIV_YIELD,divYield);
        }

        if(marketCap.equals("null")){
          builder.withValue(QuoteColumns.MARKET_CAP, "n/a");
        }else {
          builder.withValue(QuoteColumns.MARKET_CAP, marketCap);
        }

        if(peRatio.equals("null")){
          builder.withValue(QuoteColumns.PE_RATIO,"n/a");
        }else {
          builder.withValue(QuoteColumns.PE_RATIO,peRatio);
        }
      }
    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static int percToCol(double percentage){
    boolean positive = (percentage>0.0f);
    percentage = Math.abs(percentage);

    if(percentage == 0.0f){
      return 0;
    }

    if (percentage > 0f && percentage < 0.0625f) {
      if(positive) {
        return 50;
      }else {
        return -50;
      }
    } else if (percentage >= 0.0625f && percentage < 0.125f) {
      if(positive) {
        return 100;
      }else {
        return -100;
      }
    } else if (percentage >= 0.125f && percentage < 0.25f) {
      if(positive) {
        return 150;
      }else {
        return -150;
      }
    } else if (percentage >= 0.25f && percentage < 0.5f) {
      if(positive) {
        return 200;
      }else {
        return -200;
      }
    } else if (percentage >= 0.5f && percentage < 0.7f) {
      if(positive) {
        return 250;
      }else {
        return -250;
      }
    } else if (percentage >= 0.7f && percentage < 1.0f) {
      if(positive) {
        return 300;
      }else {
        return -300;
      }
    } else if (percentage >= 1.0f && percentage < 2.0f) {
      if(positive) {
        return 350;
      }else {
        return -350;
      }
    } else if (percentage >= 2.0f && percentage < 5.0f) {
      if(positive) {
        return 400;
      }else {
        return -400;
      }
    } else if (percentage >= 5.0f && percentage < 7.0f) {
      if(positive) {
        return 450;
      }else {
        return -450;
      }
    } else if (percentage >= 7.0f) {
      if(positive) {
        return 500;
      }else {
        return -500;
      }
    }else {
      return 0;
    }
  }

  public static int percToArrowRes(double percentage){
    int icon_col = percToCol(percentage);
    switch (icon_col){
      case -500: return R.drawable.down_color_500;
      case -450: return R.drawable.down_color_450;
      case -400: return R.drawable.down_color_400;
      case -350: return R.drawable.down_color_350;
      case -300: return R.drawable.down_color_300;
      case -250: return R.drawable.down_color_250;
      case -200: return R.drawable.down_color_200;
      case -150: return R.drawable.down_color_150;
      case -100: return R.drawable.down_color_100;
      case -50: return R.drawable.down_color_50;
      case -0: return R.drawable.dash_icon;
      case 50: return R.drawable.up_color_50;
      case 100: return R.drawable.up_color_100;
      case 150: return R.drawable.up_color_150;
      case 200: return R.drawable.up_color_200;
      case 250: return R.drawable.up_color_250;
      case 300: return R.drawable.up_color_300;
      case 350: return R.drawable.up_color_350;
      case 400: return R.drawable.up_color_400;
      case 450: return R.drawable.up_color_450;
      case 500: return R.drawable.up_color_500;
      default: return R.drawable.dash_icon;
    }
  }

  public static int percToDarkArrowRes(double percentage){
    int icon_col = percToCol(percentage);
    switch (icon_col){
      case -500: return R.drawable.down_color_500_dark;
      case -450: return R.drawable.down_color_450_dark;
      case -400: return R.drawable.down_color_400_dark;
      case -350: return R.drawable.down_color_350_dark;
      case -300: return R.drawable.down_color_300_dark;
      case -250: return R.drawable.down_color_250_dark;
      case -200: return R.drawable.down_color_200_dark;
      case -150: return R.drawable.down_color_150_dark;
      case -100: return R.drawable.down_color_100_dark;
      case -50: return R.drawable.down_color_50_dark;
      case -0: return R.drawable.dash_icon_dark;
      case 50: return R.drawable.up_color_50_dark;
      case 100: return R.drawable.up_color_100_dark;
      case 150: return R.drawable.up_color_150_dark;
      case 200: return R.drawable.up_color_200_dark;
      case 250: return R.drawable.up_color_250_dark;
      case 300: return R.drawable.up_color_300_dark;
      case 350: return R.drawable.up_color_350_dark;
      case 400: return R.drawable.up_color_400_dark;
      case 450: return R.drawable.up_color_450_dark;
      case 500: return R.drawable.up_color_500_dark;
      default: return R.drawable.dash_icon_dark;
    }
  }

  public static int percToTextColorRes(double percentage){
    int icon_col = percToCol(percentage);
    switch (icon_col){
      case -500: return R.color.red_text_500;
      case -450: return R.color.red_text_450;
      case -400: return R.color.red_text_400;
      case -350: return R.color.red_text_350;
      case -300: return R.color.red_text_300;
      case -250: return R.color.red_text_250;
      case -200: return R.color.red_text_200;
      case -150: return R.color.red_text_150;
      case -100: return R.color.red_text_100;
      case -50: return R.color.red_text_50;
      case -0: return R.color.text_normal;
      case 50: return R.color.green_text_50;
      case 100: return R.color.green_text_100;
      case 150: return R.color.green_text_150;
      case 200: return R.color.green_text_200;
      case 250: return R.color.green_text_250;
      case 300: return R.color.green_text_300;
      case 350: return R.color.green_text_350;
      case 400: return R.color.green_text_400;
      case 450: return R.color.green_text_450;
      case 500: return R.color.green_text_500;
      default: return R.color.text_normal;
    }
  }
}
