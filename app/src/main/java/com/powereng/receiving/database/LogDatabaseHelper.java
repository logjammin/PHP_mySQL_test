package com.powereng.receiving.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Logjammin on 4/13/14.
 */
public class LogDatabaseHelper extends SQLiteOpenHelper {

    //private static final String CURRENT_MONTH = Calendar.MONTH;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "receiving_log.db";

    public LogDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        ReceivingLogTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ReceivingLogTable.onUpgrade(db, oldVersion, newVersion);
    }

}
