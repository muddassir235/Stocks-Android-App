package com.sam_chordas.android.stockhawk.ui;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.concurrent.ExecutionException;

/**
 * Implementation of App Widget functionality.
 */
public class DetailWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.detail_widget_provider);



        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.detail_widget_provider);

            views.setImageViewBitmap(R.id.app_name_view,buildText(context,context.getString(R.string.app_name)));

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, StockListActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_heading_layout, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            boolean useDetailActivity = context.getResources()
                    .getBoolean(R.bool.use_detail_activity);
            Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, StockDetailActivity.class)
                    : new Intent(context, StockListActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.weekly_forecast_list_view, clickPendingIntentTemplate);
            views.setEmptyView(R.id.weekly_forecast_list_view, R.id.empty_view);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (StockTaskService.ACTION_DETAIL_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.weekly_forecast_list_view);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.weekly_forecast_list_view,
                new Intent(context, DetailWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.weekly_forecast_list_view,
                new Intent(context, DetailWidgetRemoteViewsService.class));
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static class DetailWidgetRemoteViewsService extends RemoteViewsService{

        public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
        private static String[] projection = new String[]{
                QuoteColumns.NAME,
                QuoteColumns.SYMBOL,
                QuoteColumns.CURRENCY,
                QuoteColumns.OPEN,
                QuoteColumns.LOW,
                QuoteColumns.HIGH,
                QuoteColumns.MARKET_CAP,
                QuoteColumns.PE_RATIO,
                QuoteColumns.DIV_YIELD,
                QuoteColumns.PERCENT_CHANGE,
                QuoteColumns.CHANGE,
                QuoteColumns.BIDPRICE,
                QuoteColumns.CREATED,
                QuoteColumns.ISUP,
                QuoteColumns.ISCURRENT,
                QuoteColumns._ID,
                QuoteColumns.PERCENT_CHANGE_REAL
        };

        public static final int INDEX_QUOTE_NAME = 0;
        public static final int INDEX_QUOTE_SYMBOL = 1;
        public static final int INDEX_QUOTE_CURRENCY = 2;
        private static final int INDEX_QUOTE_OPEN = 3;
        private static final int INDEX_QUOTE_LOW = 4;
        private static final int INDEX_QUOTE_HIGH = 5;
        private static final int INDEX_QUOTE_MARKET_CAP = 6;
        private static final int INDEX_QUOTE_PE_RATIO = 7;
        private static final int INDEX_QUOTE_DIV_YIELD = 8;
        public static final int INDEX_QUOTE_PERCENT_CHANGE = 9;
        public static final int INDEX_QUOTE_CHANGE = 10;
        public static final int INDEX_QUOTE_BID_PRICE = 11;
        private static final int INDEX_QUOTE_CREATED = 12;
        public static final int INDEX_QUOTE_ISUP = 13;
        public static final int INDEX_QUOTE_ISCURRENT = 14;
        private static final int INDEX_QUOTE_ID = 15;
        public static final int INDEX_QUOTE_PERCENT_CHANGE_REAL = 16;


        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new RemoteViewsFactory() {
                private Cursor data = null;

                @Override
                public void onCreate() {

                }

                @Override
                public void onDataSetChanged() {
                    if (data != null) {
                        data.close();
                    }
                    // This method is called by the app hosting the widget (e.g., the launcher)
                    // However, our ContentProvider is not exported so it doesn't have access to the
                    // data. Therefore we need to clear (and finally restore) the calling identity so
                    // that calls use our process and permission
                    final long identityToken = Binder.clearCallingIdentity();
                    data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                            projection,
                            QuoteColumns.ISCURRENT + " = ?",
                            new String[]{"1"},
                            null);
                    Binder.restoreCallingIdentity(identityToken);
                }

                @Override
                public void onDestroy() {
                    if (data != null) {
                        data.close();
                        data = null;
                    }
                }

                @Override
                public int getCount() {
                    if(data!=null){
                        return data.getCount();
                    }else{
                        return 0;
                    }
                }

                @Override
                public RemoteViews getViewAt(int position) {
                    if (position == AdapterView.INVALID_POSITION ||
                            data == null || !data.moveToPosition(position)) {
                        return null;
                    }
                    RemoteViews views = new RemoteViews(getPackageName(),
                            R.layout.detail_widget_item_layout);
                    int quoteId = data.getInt(INDEX_QUOTE_ID);

                    String symbol = data.getString(INDEX_QUOTE_SYMBOL);
                    String bidPrice = data.getString(INDEX_QUOTE_BID_PRICE);

                    views.setTextViewText(R.id.stock_symbol,symbol);
                    views.setTextViewText(R.id.bid_price,bidPrice);
                    int sdk = Build.VERSION.SDK_INT;
                    String directionString = "down";
                    if (data.getInt(INDEX_QUOTE_ISUP) == 1){
                        directionString = "up";
                    }

                    double percentage = data.getDouble(INDEX_QUOTE_PERCENT_CHANGE_REAL);

                    views.setTextColor(R.id.change,getResources().getColor(Utils.percToTextColorRes(percentage)));
                    views.setImageViewResource(R.id.change_arrow,Utils.percToArrowRes(percentage));

                    int stringResId;

                    if (Utils.showPercent){
                        stringResId = R.string.stock_list_content_description_percent_change;
                        String percentageChange = data.getString(INDEX_QUOTE_PERCENT_CHANGE);
                        views.setTextViewText(R.id.change,percentageChange);
                    } else{
                        stringResId = R.string.stock_list_content_description_point_change;
                        String change = data.getString(INDEX_QUOTE_CHANGE);
                        views.setTextViewText(R.id.change,change);
                    }

                    String contentDescription = getString(stringResId,
                            data.getString(INDEX_QUOTE_SYMBOL),
                            data.getString(INDEX_QUOTE_BID_PRICE),
                            data.getFloat(INDEX_QUOTE_CHANGE),
                            directionString);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        setRemoteContentDescription(views, contentDescription);
                    }else {
                        views.setContentDescription(R.id.detail_widget_list_item,contentDescription);
                    }

                    final Intent fillInIntent = new Intent();

                    Quote quote = new Quote(
                            data.getString(INDEX_QUOTE_NAME),
                            data.getString(INDEX_QUOTE_SYMBOL),
                            data.getString(INDEX_QUOTE_CURRENCY),
                            data.getString(INDEX_QUOTE_OPEN),
                            data.getString(INDEX_QUOTE_LOW),
                            data.getString(INDEX_QUOTE_HIGH),
                            data.getString(INDEX_QUOTE_MARKET_CAP),
                            data.getString(INDEX_QUOTE_PE_RATIO),
                            data.getString(INDEX_QUOTE_DIV_YIELD),
                            data.getString(INDEX_QUOTE_PERCENT_CHANGE),
                            data.getString(INDEX_QUOTE_CHANGE),
                            data.getString(INDEX_QUOTE_BID_PRICE),
                            data.getString(INDEX_QUOTE_CREATED),
                            data.getString(INDEX_QUOTE_ISUP),
                            data.getString(INDEX_QUOTE_ISCURRENT),
                            data.getDouble(INDEX_QUOTE_PERCENT_CHANGE_REAL)
                    );

                    fillInIntent.putExtra(StockDetailFragment.ARG_QUOTE_PARCABLE,quote);
                    views.setOnClickFillInIntent(R.id.detail_widget_list_item, fillInIntent);
                    return views;
                }


                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                private void setRemoteContentDescription(RemoteViews views, String description) {
                    views.setContentDescription(R.id.detail_widget_list_item, description);
                }

                @Override
                public RemoteViews getLoadingView() {
                    return new RemoteViews(getPackageName(), R.layout.detail_widget_item_layout);
                }

                @Override
                public int getViewTypeCount() {
                    return 1;
                }

                @Override
                public long getItemId(int position) {
                    if (data.moveToPosition(position))
                        return data.getLong(INDEX_QUOTE_ID);
                    return position;
                }

                @Override
                public boolean hasStableIds() {
                    return true;
                }
            };
        }
    }

    static int getPXfromDP(Context context,float dps){
        return (int)(context.getResources().getDisplayMetrics().density*dps);
    }

    public static Bitmap buildText(Context context, String string)
    {
        Bitmap myBitmap = Bitmap.createBitmap(getPXfromDP(context,120f), getPXfromDP(context,22f), Bitmap.Config.ARGB_4444);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface clock = Typeface.createFromAsset(context.getAssets(),"fonts/VCR_OS.ttf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(clock);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(getPXfromDP(context,22f));
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(string, getPXfromDP(context,22f), getPXfromDP(context,22f), paint);
        return myBitmap;
    }

}

