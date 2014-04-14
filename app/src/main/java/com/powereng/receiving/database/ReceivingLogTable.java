package com.powereng.receiving.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Logjammin on 4/13/14.
 */
public class ReceivingLogTable {

    //private static final String CURRENT_MONTH = Calendar.MONTH;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "receiving_log.db";
    //TABLE_LOG should = CURRENT_MONTH + " log"
    public static final String TABLE_LOG = "log";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date_received";
    public static final String COLUMN_TRACKING = "tracking";
    public static final String COLUMN_CARRIER = "carrier";
    public static final String COLUMN_PCS = "numpackages";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECIPIENT = "recipient";
    public static final String COLUMN_PO = "po_num";
    public static final String COLUMN_SIG = "sig";


    private static final String DATABASE_CREATE = "create table "
            + TABLE_LOG + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TRACKING + " int null, "
            + COLUMN_DATE + " text null, "
            + COLUMN_CARRIER + " text null, "
            + COLUMN_SENDER + " text null, "
            + COLUMN_RECIPIENT + " text null, "
            + COLUMN_PCS + " int null, "
            + COLUMN_PO + " int null, "
            + COLUMN_SIG + " text null);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(ReceivingLogTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
    }

}
