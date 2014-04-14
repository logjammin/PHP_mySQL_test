package com.powereng.receiving;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Logjammin on 4/13/14.
 */
public class LogDataSource {

    private SQLiteDatabase database;
    private LogOpenHelper logOpenHelper;
    private String[] allColumns = { LogOpenHelper.COLUMN_ID, LogOpenHelper.COLUMN_TRACKING,
            LogOpenHelper.COLUMN_DATE, LogOpenHelper.COLUMN_CARRIER, LogOpenHelper.COLUMN_SENDER,
            LogOpenHelper.COLUMN_RECIPIENT, LogOpenHelper.COLUMN_PCS,
            LogOpenHelper.COLUMN_PO, LogOpenHelper.COLUMN_SIG };

    public LogDataSource(Context context) {
        logOpenHelper = new LogOpenHelper(context);
    }

    public void open() throws SQLException {
        database = logOpenHelper.getWritableDatabase();
    }

    public void close() {
        logOpenHelper.close();
    }


    public ReceivingLog createEntry(String[] params) {
        ContentValues values = new ContentValues();

        for (int i = 0; i < allColumns.length; i++) {
            values.put(allColumns[i], params[i]);
        }

        long insertId = database.insert(LogOpenHelper.TABLE_LOG, null,
                values);
        Cursor cursor = database.query(LogOpenHelper.TABLE_LOG,
                allColumns, LogOpenHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ReceivingLog newEntry = cursorToLog(cursor);
        cursor.close();
        return newEntry;
    }

    public void deleteEntry(ReceivingLog entry) {
        long id = entry.getId();
        System.out.println("Entry deleted with id: " + id);
        database.delete(LogOpenHelper.TABLE_LOG, LogOpenHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<ReceivingLog> getAllEntries() {
        List<ReceivingLog> entries = new ArrayList<ReceivingLog>();

        Cursor cursor = database.query(LogOpenHelper.TABLE_LOG,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ReceivingLog receivingLog = cursorToLog(cursor);
            entries.add(receivingLog);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return entries;
    }

    private ReceivingLog cursorToLog(Cursor cursor) {
        ReceivingLog log = new ReceivingLog();
        log.setId(cursor.getLong(0));
        log.setTracking(cursor.getString(1));
        log.setDate(cursor.getString(2));
        log.setCarrier(cursor.getString(3));
        log.setSender(cursor.getString(4));
        log.setRecipient(cursor.getString(5));
        log.setPcs(cursor.getString(6));
        log.setPO(cursor.getString(7));
        log.setSig(cursor.getString(8));
        return log;
    }
}
