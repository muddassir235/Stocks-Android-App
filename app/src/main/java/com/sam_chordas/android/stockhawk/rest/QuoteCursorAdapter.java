package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.sam_chordas.android.stockhawk.ui.StockListActivity;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private static Context mContext;
  private static Typeface customTypeFace;
  private boolean isPercent;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    customTypeFace = Typeface.createFromAsset(mContext.getAssets(), "fonts/VCR_OS.ttf");
    View itemView;
    itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item_quote, parent, false);
    if(viewType == 1) {

    }else{
      itemView.setBackgroundResource(R.drawable.touch_selector_darker);
    }
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public int getItemViewType(int position) {
    if(position%2 == 0){
      return 0;
    }else{
      return 1;
    }
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
    viewHolder.symbol.setText(cursor.getString(StockListActivity.INDEX_QUOTE_SYMBOL));
    viewHolder.name.setText(cursor.getString(StockListActivity.INDEX_QUOTE_NAME));
    viewHolder.bidPrice.setText(cursor.getString(StockListActivity.INDEX_QUOTE_BID_PRICE));
    int sdk = Build.VERSION.SDK_INT;
    String directionString = "down";
    if (cursor.getInt(StockListActivity.INDEX_QUOTE_ISUP) == 1){
      directionString = "up";
    }

    double percentage = cursor.getDouble(StockListActivity.INDEX_QUOTE_PERCENT_CHANGE_REAL);

    viewHolder.change.setTextColor(
            mContext.getResources().getColor(Utils.percToTextColorRes(percentage))
    );

    viewHolder.changeArrow.setImageResource(Utils.percToArrowRes(percentage));

    int stringResId;

    if (Utils.showPercent){
      stringResId = R.string.stock_list_content_description_percent_change;
      viewHolder.change.setText(cursor.getString(StockListActivity.INDEX_QUOTE_PERCENT_CHANGE));
    } else{
      stringResId = R.string.stock_list_content_description_point_change;
      viewHolder.change.setText(cursor.getString(StockListActivity.INDEX_QUOTE_CHANGE));
    }

    viewHolder.itemView.setContentDescription(
            mContext.getString(stringResId,
                    cursor.getString(StockListActivity.INDEX_QUOTE_SYMBOL),
                    cursor.getString(StockListActivity.INDEX_QUOTE_BID_PRICE),
                    cursor.getFloat(StockListActivity.INDEX_QUOTE_CHANGE),
                    directionString)
    );
  }

  @Override public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(StockListActivity.INDEX_QUOTE_SYMBOL);
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    notifyItemRemoved(position);
  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final TextView symbol;
    public final TextView name;
    public final TextView bidPrice;
    public final TextView change;
    public final LinearLayout changeLayout;
    public final ImageView changeArrow;
    public ViewHolder(View itemView){
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(customTypeFace);
      name = (TextView) itemView.findViewById(R.id.company_name);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      bidPrice.setTypeface(customTypeFace);
      changeLayout = (LinearLayout) itemView.findViewById(R.id.change_layout);
      changeArrow = (ImageView) itemView.findViewById(R.id.change_arrow);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {

    }
  }
}
