package com.sam_chordas.android.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hp on 10/26/2016.
 */

public class Quote implements Parcelable{

    public String mName;
    public String mSymbol;
    public String mCurrency;
    public String mOpen;
    public String mDayLow;
    public String mDayHigh;
    public String mMarketCap;
    public String mPERatio;
    public String mDivYield;
    public String mPercentChange;
    public String mPointsChange;
    public String mBidPrice;
    public String mCreated;
    public String mIsUp;
    public String mIsCurrent;
    public double mPercentChangeReal;

    public Quote(
            String mName,
            String mSymbol,
            String mCurrency,
            String mOpen,
            String mDayLow,
            String mDayHigh,
            String mMarketCap,
            String mPERatio,
            String mDivYield,
            String mPercentChange,
            String mPointsChange,
            String mBidPrice,
            String mCreated,
            String mIsUp,
            String mIsCurrent,
            double mPercentChangeReal
    ) {
        this.mName = mName;
        this.mSymbol = mSymbol;
        this.mCurrency = mCurrency;
        this.mOpen = mOpen;
        this.mDayLow = mDayLow;
        this.mDayHigh = mDayHigh;
        this.mMarketCap = mMarketCap;
        this.mPERatio = mPERatio;
        this.mDivYield = mDivYield;
        this.mPercentChange = mPercentChange;
        this.mPointsChange = mPointsChange;
        this.mBidPrice = mBidPrice;
        this.mCreated = mCreated;
        this.mIsUp = mIsUp;
        this.mIsCurrent = mIsCurrent;
        this.mPercentChangeReal = mPercentChangeReal;
    }

    public Quote(Parcel in) {
        String[] data = new String[16];

        in.readStringArray(data);
        this.mName = data[0];
        this.mSymbol = data[1];
        this.mCurrency = data[2];
        this.mOpen = data[3];
        this.mDayLow = data[4];
        this.mDayHigh = data[5];
        this.mMarketCap = data[6];
        this.mPERatio = data[7];
        this.mDivYield = data[8];
        this.mPercentChange = data[9];
        this.mPointsChange = data[10];
        this.mBidPrice = data[11];
        this.mCreated = data[12];
        this.mIsUp = data[13];
        this.mIsCurrent = data[14];
        this.mPercentChangeReal = Double.valueOf(data[15]);
    }

    public static final Creator<Quote> CREATOR = new Creator<Quote>() {
        @Override
        public Quote createFromParcel(Parcel in) {
            return new Quote(in);
        }

        @Override
        public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.mName,
                this.mSymbol,
                this.mCurrency,
                this.mOpen,
                this.mDayLow,
                this.mDayHigh,
                this.mMarketCap,
                this.mPERatio,
                this.mDivYield,
                this.mPercentChange,
                this.mPointsChange,
                this.mBidPrice,
                this.mCreated,
                this.mIsUp,
                this.mIsCurrent,
                String.valueOf(this.mPercentChangeReal)
        });
    }
}
