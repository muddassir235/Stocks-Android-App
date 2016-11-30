package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteChartData;
import com.sam_chordas.android.stockhawk.rest.ChartDataWrapper;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.dummy.DummyContent;

/**
 * A fragment representing a single Stock detail screen.
 * This fragment is either contained in a {@link StockListActivity}
 * in two-pane mode (on tablets) or a {@link StockDetailActivity}
 * on handsets.
 */
public class StockDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_QUOTE_PARCABLE = "quote";
    public static final String ARG_PARENT = "parent";
    private static final String LOG_TAG = StockDetailFragment.class.getName()+": ";

    public static final String DETAIL_ACTIVITY = "DetailActivity";
    public static final String LIST_ACTIVITY = "StockListActivity";

    Quote mQuote;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StockDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_QUOTE_PARCABLE)) {
            mQuote = getArguments().getParcelable(ARG_QUOTE_PARCABLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stock_detail_fragment, container, false);

        ImageButton backButton = (ImageButton) rootView.findViewById(R.id.back_button);

        if(getArguments().getString(ARG_PARENT).equals(LIST_ACTIVITY)){
            backButton.setVisibility(View.GONE);
        }else if(getArguments().getString(ARG_PARENT).equals(DETAIL_ACTIVITY)){
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChartDataWrapper.getInstance().startOver();
                    if(getActivity()!=null) {
                        getActivity().finish();
                    }else {
                        Intent intent = new Intent(getContext(),StockListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
        }

        if (mQuote != null) {
            Typeface customTypeFace  = Typeface.createFromAsset(getActivity().getAssets(), "fonts/VCR_OS.ttf");

            final ProgressBar loadingProgress= (ProgressBar) rootView.findViewById(R.id.loading_progress);
            final FrameLayout loadingFilter = (FrameLayout) rootView.findViewById(R.id.loading_filter);
            ImageView statusIV = (ImageView) rootView.findViewById(R.id.status_image);
            TextView statusTV = (TextView) rootView.findViewById(R.id.status_text);
            statusTV.setTypeface(customTypeFace);
            ChartDataWrapper.getInstance().
                    setLoadingView(loadingProgress).
                    setLoadingFilter(loadingFilter).
                    setStatusImage(statusIV).
                    setStatusText(statusTV);

            TextView symbolTV = (TextView) rootView.findViewById(R.id.symbol);
            TextView nameTV = (TextView) rootView.findViewById(R.id.company_name);
            TextView bidPriceTV = (TextView) rootView.findViewById(R.id.bid_price);
            TextView currencyTV = (TextView) rootView.findViewById(R.id.currency_text_view);
            TextView changeTV = (TextView) rootView.findViewById(R.id.change);
            TextView changePercentTV = (TextView) rootView.findViewById(R.id.change_percentage);

            TextView openTV = (TextView) rootView.findViewById(R.id.open_price);
            TextView openLabelTV = (TextView) rootView.findViewById(R.id.open_price_label);
            TextView highTV = (TextView) rootView.findViewById(R.id.high_price);
            TextView highLabelTV = (TextView) rootView.findViewById(R.id.high_price_label);
            TextView lowTV = (TextView) rootView.findViewById(R.id.low_price);
            TextView lowLabelTV = (TextView) rootView.findViewById(R.id.low_price_label);
            TextView mktCapTV = (TextView) rootView.findViewById(R.id.market_cap);
            TextView mktCapLabelTV = (TextView) rootView.findViewById(R.id.market_cap_label);
            TextView peRTV = (TextView) rootView.findViewById(R.id.pe_ratio);
            TextView peRLabelTV = (TextView) rootView.findViewById(R.id.pe_ratio_label);
            TextView divYTV = (TextView) rootView.findViewById(R.id.div_yield);
            TextView divYLabelTV = (TextView) rootView.findViewById(R.id.div_yield_label);
            ImageView arrowIV = (ImageView) rootView.findViewById(R.id.change_arrow);

            symbolTV.setTypeface(customTypeFace);
            nameTV.setTypeface(customTypeFace);
            bidPriceTV.setTypeface(customTypeFace);
            currencyTV.setTypeface(customTypeFace);
            changeTV.setTypeface(customTypeFace);
            changePercentTV.setTypeface(customTypeFace);

            openTV.setTypeface(customTypeFace);
            openLabelTV.setTypeface(customTypeFace);
            highTV.setTypeface(customTypeFace);
            highLabelTV.setTypeface(customTypeFace);
            lowTV.setTypeface(customTypeFace);
            lowLabelTV.setTypeface(customTypeFace);
            mktCapTV.setTypeface(customTypeFace);
            mktCapLabelTV.setTypeface(customTypeFace);
            peRTV.setTypeface(customTypeFace);
            peRLabelTV.setTypeface(customTypeFace);
            divYTV.setTypeface(customTypeFace);
            divYLabelTV.setTypeface(customTypeFace);

            symbolTV.setText(mQuote.mSymbol);
            nameTV.setText("("+mQuote.mName+")");
            bidPriceTV.setText(mQuote.mBidPrice);
            currencyTV.setText(mQuote.mCurrency);
            changeTV.setText(mQuote.mPointsChange);
            changePercentTV.setText("("+mQuote.mPercentChange+")");

            arrowIV.setImageResource(Utils.percToDarkArrowRes(mQuote.mPercentChangeReal));

            if(mQuote.mPercentChangeReal == 0){
                arrowIV.setContentDescription(getString(R.string.constant_value));
            }else if(mQuote.mPercentChangeReal>0){
                arrowIV.setContentDescription(getString(R.string.up_arrow));
            }else {
                arrowIV.setContentDescription(getString(R.string.down_arrow));
            }

            int accentColor = getResources().getColor(Utils.percToTextColorRes(mQuote.mPercentChangeReal));
            changeTV.setTextColor(accentColor);
            changePercentTV.setTextColor(accentColor);


            openTV.setText(mQuote.mOpen);
            highTV.setText(mQuote.mDayHigh);
            lowTV.setText(mQuote.mDayLow);
            mktCapTV.setText(mQuote.mMarketCap);
            peRTV.setText(mQuote.mPERatio);
            divYTV.setText(mQuote.mDivYield);

            final LineChartView lineChartView = (LineChartView) rootView.findViewById(R.id.linechart);
            final TextView prevTV = (TextView) rootView.findViewById(R.id.prev_value);
            TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
            tabLayout.addTab(tabLayout.newTab().setText("1D"));
            tabLayout.addTab(tabLayout.newTab().setText("1W"));
            tabLayout.addTab(tabLayout.newTab().setText("1M"));
            tabLayout.addTab(tabLayout.newTab().setText("1Y"));
            tabLayout.addTab(tabLayout.newTab().setText("5Y"));
            tabLayout.addTab(tabLayout.newTab().setText("MAX"));

            changeTabsFont(tabLayout,customTypeFace);

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tab.getText().toString().equals("1D")){
                        loadingProgress.setVisibility(View.VISIBLE);
                        loadingFilter.setVisibility(View.VISIBLE);
                        ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("1d").setPrevValueTV(prevTV).drawOn(lineChartView);
                    }else if(tab.getText().toString().equals("1W")){
                        loadingProgress.setVisibility(View.VISIBLE);
                        loadingFilter.setVisibility(View.VISIBLE);
                        ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("7d").drawOn(lineChartView);
                    }else if(tab.getText().toString().equals("1M")){
                        loadingProgress.setVisibility(View.VISIBLE);
                        loadingFilter.setVisibility(View.VISIBLE);
                        ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("1m").drawOn(lineChartView);
                    }else if(tab.getText().toString().equals("1Y")){
                        loadingProgress.setVisibility(View.VISIBLE);
                        loadingFilter.setVisibility(View.VISIBLE);
                        ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("1y").drawOn(lineChartView);
                    }else if(tab.getText().toString().equals("5Y")){
                        loadingProgress.setVisibility(View.VISIBLE);
                        loadingFilter.setVisibility(View.VISIBLE);
                        ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("5y").drawOn(lineChartView);
                    }else if(tab.getText().toString().equals("MAX")){
                        loadingProgress.setVisibility(View.VISIBLE);
                        loadingFilter.setVisibility(View.VISIBLE);
                        ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("max").drawOn(lineChartView);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
            Log.v(LOG_TAG, " quote is not null");
            ChartDataWrapper.getInstance().with(getActivity()).stock(mQuote.mSymbol).span("1d").setPrevValueTV(prevTV).drawOn(lineChartView);
        }
        return rootView;
    }

    private void changeTabsFont(TabLayout tabLayout, Typeface typeface) {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeface);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ChartDataWrapper.startOver();
    }
}
