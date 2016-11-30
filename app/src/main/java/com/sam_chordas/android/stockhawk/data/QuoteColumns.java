package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by sam_chordas on 10/5/15.
 */
public class QuoteColumns {
  @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
  public static final String _ID = "_id";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String NAME = "name";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String SYMBOL = "symbol";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CURRENCY = "currency";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String OPEN = "open";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String LOW = "low";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String HIGH = "high";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String MARKET_CAP = "market_cap";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PE_RATIO = "pe_ratio";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String DIV_YIELD = "div_yield";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String PERCENT_CHANGE = "percent_change";

  @DataType(DataType.Type.REAL) @NotNull
  public static final String PERCENT_CHANGE_REAL = "percent_change_real";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CHANGE = "change";

  @DataType(DataType.Type.TEXT) @NotNull
  public static final String BIDPRICE = "bid_price";

  @DataType(DataType.Type.TEXT)
  public static final String CREATED = "created";

  @DataType(DataType.Type.INTEGER) @NotNull
  public static final String ISUP = "is_up";

  @DataType(DataType.Type.INTEGER) @NotNull
  public static final String ISCURRENT = "is_current";
}
