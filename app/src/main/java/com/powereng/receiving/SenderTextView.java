package com.powereng.receiving;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import com.powereng.receiving.database.LogEntry;

/**
 * Created by Logjammin on 5/12/14.
 */
public class SenderTextView extends AutoCompleteTextView{

    Context mContext;

    public SenderTextView(Context context) {
        super(context);
        this.mContext = context;
        initializeTextView();
    }

    public SenderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initializeTextView();
    }

    public SenderTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initializeTextView();
    }

    void initializeTextView(){
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(mContext, android.R.layout.simple_list_item_1,
                null,new String[] {LogEntry.COL_SENDER},new int[]{android.R.id.text1}, 0);
        this.setAdapter(mAdapter);

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursor(str);
            }
        });

        mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(LogEntry.COL_SENDER);
                return cur.getString(index);
            }
        });
    }

    public Cursor getCursor(CharSequence str) {
        String select = "(" + LogEntry.COL_SENDER + " LIKE ?)";
        String[]  selectArgs = { "%" + str + "%"};
        String[] projection = new String[] {
                LogEntry.COL_ID,
                LogEntry.COL_SENDER};

        return mContext.getContentResolver().query(LogEntry.URI(), projection, select, selectArgs, null);
    }

}
