package com.powereng.receiving.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.powereng.receiving.database.LogDatabaseHelper;
import com.powereng.receiving.database.ReceivingLogTable;

import java.util.Arrays;
import java.util.HashSet;

public class ReceivingLogContentProvider extends ContentProvider {
    public ReceivingLogContentProvider() {
    }
    // database
    private LogDatabaseHelper database;

    // used for the UriMacher
    private static final int ENTRIES = 10;
    private static final int ENTRY_ID = 20;

    private static final String AUTHORITY = ReceivingLogContract.CONTENT_AUTHORITY;

    private static final String BASE_PATH = "entries";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/entries";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/entry";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRY_ID);
    }

    @Override
    public boolean onCreate() {
        database = new LogDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(ReceivingLogTable.TABLE_LOG);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ENTRIES:
                break;
            case ENTRY_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(ReceivingLogTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case ENTRIES:
                id = sqlDB.insert(ReceivingLogTable.TABLE_LOG, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case ENTRIES:
                rowsDeleted = sqlDB.delete(ReceivingLogTable.TABLE_LOG, selection,
                        selectionArgs);
                break;
            case ENTRY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ReceivingLogTable.TABLE_LOG,
                            ReceivingLogTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ReceivingLogTable.TABLE_LOG,
                            ReceivingLogTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case ENTRIES:
                rowsUpdated = sqlDB.update(ReceivingLogTable.TABLE_LOG,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ENTRY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ReceivingLogTable.TABLE_LOG,
                            values,
                            ReceivingLogTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ReceivingLogTable.TABLE_LOG,
                            values,
                            ReceivingLogTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { ReceivingLogTable.COLUMN_ID, ReceivingLogTable.COLUMN_DATE,
                ReceivingLogTable.COLUMN_TRACKING, ReceivingLogTable.COLUMN_CARRIER,
                ReceivingLogTable.COLUMN_PCS, ReceivingLogTable.COLUMN_SENDER,
                ReceivingLogTable.COLUMN_RECIPIENT, ReceivingLogTable.COLUMN_PCS,
                ReceivingLogTable.COLUMN_PO, ReceivingLogTable.COLUMN_SIG };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }


}