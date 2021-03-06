package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Nicola on 2016-07-01.
 */
public class StockQuotesWidgetRemoteViewsService extends RemoteViewsService{
    public final String LOG_TAG = StockQuotesWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] QUOTE_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };
    // projection indices
    private static final int INDEX_STOCK_ID = 0;
    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_BIDPRICE = 2;
    private static final int INDEX_CHANGE = 3;
    private static final int INDEX_IS_UP = 4;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
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

                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, QUOTE_COLUMNS, QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
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
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                //int stockId = data.getInt(INDEX_STOCK_ID);

                String symbol = data.getString(INDEX_SYMBOL);
                String bidPrice = data.getString(INDEX_BIDPRICE);
                String change = data.getString(INDEX_CHANGE);

                views.setTextColor(R.id.stock_symbol,ContextCompat.getColor(getBaseContext(),R.color.secondary_text_color));
                views.setTextViewText(R.id.stock_symbol, symbol);
                views.setTextColor(R.id.bid_price,ContextCompat.getColor(getBaseContext(),R.color.secondary_text_color));
                views.setTextViewText(R.id.bid_price, bidPrice);
                views.setTextViewText(R.id.change, change);
                views.setTextColor(R.id.change,ContextCompat.getColor(getBaseContext(),R.color.white));
                if (data.getInt(INDEX_IS_UP) == 1){
                    views.setInt(R.id.change,"setBackgroundResource", R.drawable.percent_change_pill_green);
                } else{
                    views.setInt(R.id.change,"setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                // On list item click
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getString(R.string.stock_symbol),symbol);
                views.setOnClickFillInIntent(R.id.stock_container, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_STOCK_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }

}
