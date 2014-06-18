package com.powereng.receiving;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import com.powereng.receiving.database.POEntry;

/**
 * Created by Logjammin on 5/12/14.
 */
public class RecipientTextView extends AutoCompleteTextView{

    Context mContext;

    public RecipientTextView(Context context) {
        super(context);
        this.mContext = context;
        initializeTextView();
    }

    public RecipientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initializeTextView();
    }

    public RecipientTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initializeTextView();
    }

    void initializeTextView(){
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(mContext, android.R.layout.simple_list_item_1,
                null,new String[] {POEntry.COL_TOTAL},new int[]{android.R.id.text1}, 0);
        this.setAdapter(mAdapter);

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursor(str);
            }
        });

        mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(POEntry.COL_TOTAL);
                return cur.getString(index);
            }
        });
    }

    public Cursor getCursor(CharSequence str) {
        String select = "(" + POEntry.COL_TOTAL + " LIKE ?)";
        String[]  selectArgs = { "%" + str + "%"};
        String[] projection = new String[] {
                POEntry.COL_ID,
                POEntry.COL_TOTAL};

        return mContext.getContentResolver().query(POEntry.URI(), projection, select, selectArgs, null);
    }

}
