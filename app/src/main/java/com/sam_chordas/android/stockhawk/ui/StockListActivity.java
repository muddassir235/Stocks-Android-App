package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.ItemClickListener;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.ui.dummy.DummyContent;

import java.util.List;

/**
 * An activity representing a list of Stocks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StockDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StockListActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String LOG_TAG = StockListActivity.class.getName()+": ";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;

    SwipeRefreshLayout mRefreshLayout;
    ImageView mEmptyListIV;
    TextView mEmptyListTV;

    private boolean mTwoPane;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar!=null){
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final Typeface customTypeFace = Typeface.createFromAsset(getAssets(), "fonts/VCR_OS.ttf");
        TextView titleTV = (TextView) findViewById(R.id.title_text_view);
        titleTV.setTypeface(customTypeFace);

        mContext = this;
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null){
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected){
                startService(mServiceIntent);
            } else{

                networkToast();

            }
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.stock_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mServiceIntent.putExtra("tag", "init");
                if (isConnected){
                    startService(mServiceIntent);
                } else{

                    networkToast();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshLayout.setRefreshing(false);
                        }
                    },100);

                }
            }
        });

        mEmptyListIV = (ImageView) findViewById(R.id.status_image);
        mEmptyListTV = (TextView) findViewById(R.id.status_text);
        mEmptyListTV.setTypeface(customTypeFace);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        SwitchCompat percentageSwitch = (SwitchCompat) findViewById(R.id.percentage_switch);
        final TextView percentageSymbol = (TextView) findViewById(R.id.percentage_symbol);
        percentageSymbol.setTypeface(customTypeFace);

        percentageSwitch.setChecked(Utils.showPercent);
        if(Utils.showPercent){
            percentageSymbol.setTextColor(mContext.getResources().getColor(R.color.white));
        }else{
            percentageSymbol.setTextColor(mContext.getResources().getColor(R.color.transparent_white
            ));
        }

        percentageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Utils.showPercent = isChecked;
                mContext.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
                if(isChecked) {
                    percentageSymbol.setTextColor(mContext.getResources().getColor(R.color.white));
                }else{
                    percentageSymbol.setTextColor(mContext.getResources().getColor(R.color.transparent_white
                    ));
                }
            }
        });

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        mCursorAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {
                mCursor.moveToPosition(position);
                //TODO:
                // do something on item click
                Quote quote = new Quote(
                        mCursor.getString(INDEX_QUOTE_NAME),
                        mCursor.getString(INDEX_QUOTE_SYMBOL),
                        mCursor.getString(INDEX_QUOTE_CURRENCY),
                        mCursor.getString(INDEX_QUOTE_OPEN),
                        mCursor.getString(INDEX_QUOTE_LOW),
                        mCursor.getString(INDEX_QUOTE_HIGH),
                        mCursor.getString(INDEX_QUOTE_MARKET_CAP),
                        mCursor.getString(INDEX_QUOTE_PE_RATIO),
                        mCursor.getString(INDEX_QUOTE_DIV_YIELD),
                        mCursor.getString(INDEX_QUOTE_PERCENT_CHANGE),
                        mCursor.getString(INDEX_QUOTE_CHANGE),
                        mCursor.getString(INDEX_QUOTE_BID_PRICE),
                        mCursor.getString(INDEX_QUOTE_CREATED),
                        mCursor.getString(INDEX_QUOTE_ISUP),
                        mCursor.getString(INDEX_QUOTE_ISCURRENT),
                        mCursor.getDouble(INDEX_QUOTE_PERCENT_CHANGE_REAL)
                );

                if(!mTwoPane) {
                    Intent intent = new Intent(getApplicationContext(), StockDetailActivity.class);
                    intent.putExtra(StockDetailFragment.ARG_QUOTE_PARCABLE,quote);
                    startActivity(intent);
                }else{
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(StockDetailFragment.ARG_QUOTE_PARCABLE,quote);
                    arguments.putString(StockDetailFragment.ARG_PARENT,StockDetailFragment.LIST_ACTIVITY);
                    StockDetailFragment detailFragment = new StockDetailFragment();
                    detailFragment.setArguments(arguments);
                    getSupportFragmentManager().
                            beginTransaction().
                            replace(
                                    R.id.stock_detail_container,
                                    detailFragment
                            ).commit();
                }
            }
        });
        recyclerView.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setColorNormalResId(R.color.primary_color);
        fab.setColorPressedResId(R.color.primary_color_dark);
        fab.setColorRippleResId(R.color.green_accent_200);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isConnected){
                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .titleColorRes(R.color.green_250)
                            .typeface("VCR_OS.ttf","VCR_OS.ttf")
                            .backgroundColorRes(R.color.dark_grey)
                            .positiveColorRes(R.color.green_250)
                            .contentColorRes(R.color.green_250)
                            .iconRes(R.drawable.add_icon_dialog)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                                            new String[] { input.toString() }, null);
                                    if (c.getCount() != 0) {
                                        Toast toast =
                                                Toast.makeText(getApplicationContext(), "This stock is already saved!",
                                                        Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("symbol", input.toString());
                                        startService(mServiceIntent);
                                    }
                                }
                            })
                            .show();
                } else {
                    networkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (isConnected){
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }

        if (findViewById(R.id.stock_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if(mTwoPane){
            TextView selectAStockTV = (TextView) findViewById(R.id.select_stock_tv);
            selectAStockTV.setTypeface(customTypeFace);
        }
    }


    public void networkToast(){
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                projection,
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        mRefreshLayout.setRefreshing(false);
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        if(data.getCount() == 0) {
            if(mEmptyListIV.getVisibility() != View.VISIBLE){
                mEmptyListIV.setVisibility(View.VISIBLE);
                mEmptyListIV.setImageResource(R.drawable.pixel_icon);
            }
            if(mEmptyListTV.getVisibility() != View.VISIBLE){
                mEmptyListTV.setVisibility(View.VISIBLE);
            }
            if (isConnected){
                mEmptyListTV.setText(R.string.empty_list);
                Log.v(LOG_TAG, " empty list no stocks");
            }else {
                mEmptyListTV.setText(R.string.no_internet);
                Log.v(LOG_TAG, " no internet");
            }
        }else {
            if(mEmptyListIV.getVisibility() == View.VISIBLE){
                mEmptyListIV.setVisibility(View.GONE);
            }
            if(mEmptyListTV.getVisibility() == View.VISIBLE){
                mEmptyListTV.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.stock_exists_key))){
            String value = sharedPreferences.getString(getString(R.string.stock_exists_key),getString(R.string.stock_exists));
            if(value.equals(getString(R.string.not_stock__exists))){
                Toast.makeText(mContext," Stock doesn't exit",Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putString(getString(R.string.stock_exists_key),getString(R.string.stock_exists)).commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
