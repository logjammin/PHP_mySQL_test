package com.powereng.receiving.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Database handler, SQLite wrapper and ORM layer.
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "Receiving";
	private final Context context;

	private static DatabaseHandler instance = null;

	public synchronized static DatabaseHandler getInstance(Context context) {
		if (instance == null)
			instance = new DatabaseHandler(context.getApplicationContext());
		return instance;
	}

	public DatabaseHandler(Context context) {
		super(context.getApplicationContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		this.context = context.getApplicationContext();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			// This line requires android16
			// db.setForeignKeyConstraintsEnabled(true);
			// This line works everywhere though
			db.execSQL("PRAGMA foreign_keys=ON;");
			// Create temporary triggers
			DatabaseTriggers.createTemp(db);
		}
	}

	@Override
	public synchronized void onCreate(SQLiteDatabase db) {

		db.execSQL("DROP TABLE IF EXISTS " + POEntry.TABLE_NAME);
		db.execSQL(POEntry.CREATE_TABLE);

		// Create Triggers
		DatabaseTriggers.create(db);
	}

	// Upgrading database
	@Override
	public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		// Try to drop and recreate. You should do something clever here
		onCreate(db);
	}

	// Convenience methods
	public synchronized boolean putEntry(final POEntry entry) {
		boolean success = false;
		int result = 0;
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues values = entry.getContent();

		if (entry.getId() > -1) {
			result += db.update(entry.getTableName(), values, DBItem.COL_ID
					+ " IS ?", new String[] { String.valueOf(entry.getId()) });
		} else {
			final long id = db.insert(entry.getTableName(), null, values);
			if (id > 0) {
				entry.setId(id);
				result++;
			}
		}

		if (result > 0) {
			success = true;
		}
		if (success) {
			entry.notifyProvider(context);
		}
		return success;
	}

	public synchronized int deleteEntry(POEntry item) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final int result = db.delete(item.getTableName(), POEntry.COL_ID
				+ " IS ? OR " + POEntry.COL_PONUM + " IS ?",
				new String[] { Long.toString(item.poID), item.ponum});

		if (result > 0) {
			item.notifyProvider(context);
		}

		return result;
	}

	public synchronized Cursor getLogEntryCursor(final long id) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.query(POEntry.TABLE_NAME, POEntry.FIELDS,
				POEntry.COL_ID + " IS ?", new String[] { String.valueOf(id) },
				null, null, null, null);
		return cursor;
	}

	public synchronized POEntry getLogEntry(final long id) {
		final Cursor cursor = getLogEntryCursor(id);
		final POEntry result;
		if (cursor.moveToFirst()) {
			result = new POEntry(cursor);
		} else {
			result = null;
		}
		cursor.close();
		return result;
	}

	public synchronized Cursor getAllLogEntriesCursor(final String selection,
                  final String[] args, final String sortOrder) {

        final SQLiteDatabase db = this.getWritableDatabase();
        if (db == null) {

        }
        final Cursor cursor = db.query(POEntry.TABLE_NAME, POEntry.FIELDS,
                    selection, args, null, null, sortOrder, null);

        return cursor;
	}

	public synchronized List<POEntry> getAllLogEntries(final String selection,
                    final String[] args, final String sortOrder) {
		final List<POEntry> result = new ArrayList<POEntry>();

		final Cursor cursor = getAllLogEntriesCursor(selection, args, sortOrder);

		while (cursor.moveToNext()) {
			POEntry q = new POEntry(cursor);
			result.add(q);
		}

		cursor.close();
		return result;
	}

    public synchronized Cursor textViewCursor(String[] projection, String selection, String[] args, String sortOrder) {
        String colname= projection[1];
        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.query(POEntry.TABLE_NAME, projection, selection, args, colname, null, sortOrder);
        return cursor;
    }
}
