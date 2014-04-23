package com.powereng.receiving.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.powereng.receiving.database.ReceivingLogTable;
import com.powereng.receiving.util.SelectionBuilder;

public class ReceivingLogProvider extends ContentProvider {
    private LogDatabase mDatabaseHelper;

	private static final String AUTHORITY = ReceivingLogContract.CONTENT_AUTHORITY;
	
    private static final int ROUTE_ENTRIES = 1;
    private static final int ROUTE_ENTRIES_ID = 2;

    private static final int QUERY =0;
    private static final int INSERT =1;
    private static final int UPDATE =2;
    private static final int DELETE =3;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }


    @Override
    public boolean onCreate() {
        mDatabaseHelper = new LogDatabase(getContext());
        return true;
    }
	
	@Override
    public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return ReceivingLogContract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return ReceivingLogContract.Entry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
	
	/**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
						String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
		int uriMatch = sUriMatcher.match(uri);
        
        switch (uriMatch) {
            case ROUTE_ENTRIES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(ReceivingLogContract.Entry._ID + "=?", id);
            case ROUTE_ENTRIES:
            // Return all known entries.
                builder.table(ReceivingLogContract.Entry.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
	}


	
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;		
		final int match = sUriMatcher.match(uri);
		Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long id = db.insertOrThrow(ReceivingLogContract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(ReceivingLogContract.Entry.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
				throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
			default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(ReceivingLogContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(ReceivingLogContract.Entry.TABLE_NAME)
                       .where(ReceivingLogContract.Entry._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
		SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(ReceivingLogContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(ReceivingLogContract.Entry.TABLE_NAME)
                        .where(ReceivingLogContract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);

        return count;
    }

	
	static class LogDatabase extends SQLiteOpenHelper {

        //private static final String CURRENT_MONTH = Calendar.MONTH;
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "receiving_log.db";
	
	    private static final String DATABASE_CREATE = "create table "
            + ReceivingLogContract.Entry.TABLE_NAME + "("
            + ReceivingLogContract.Entry._ID + " integer primary key autoincrement, "
            + ReceivingLogContract.Entry.COLUMN_NAME_DATE + " text null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_TRACKING + " int null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_CARRIER + " text null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_SENDER + " text null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_RECIPIENT + " text null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_NUMPACKAGES + " int null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_PO_NUM + " int null, "
            + ReceivingLogContract.Entry.COLUMN_NAME_SIG + " text null);";
			
        public LogDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    Log.w(ReceivingLogTable.class.getName(),
                            "Upgrading database from version " + oldVersion + " to "
                                    + newVersion + ", which will destroy all old data"
                    );

            db.execSQL("DROP TABLE IF EXISTS " + ReceivingLogContract.Entry.TABLE_NAME);
        }

    }

}