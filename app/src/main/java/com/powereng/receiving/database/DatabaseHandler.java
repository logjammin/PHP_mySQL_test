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
	private static final String DATABASE_NAME = "SampleDB";
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

		db.execSQL("DROP TABLE IF EXISTS " + LogEntry.TABLE_NAME);
		db.execSQL(LogEntry.CREATE_TABLE);

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
	public synchronized boolean putEntry(final LogEntry item) {
		boolean success = false;
		int result = 0;
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues values = item.getContent();

		if (item.getId() > -1) {
			result += db.update(item.getTableName(), values, DBItem.COL_ID
					+ " IS ?", new String[] { String.valueOf(item.getId()) });
		}
		// Update failed or wasn't possible, insert instead
		else {
			final long id = db.insert(item.getTableName(), null, values);
			if (id > 0) {
				item.setId(id);
				result++;
			}
		}

		if (result > 0) {
			success = true;
		}
		if (success) {
			item.notifyProvider(context);
		}
		return success;
	}

	public synchronized int deleteEntry(LogEntry item) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final int result = db.delete(item.getTableName(), LogEntry.COL_ID
				+ " IS ? OR " + LogEntry.COL_TRACKING + " IS ?",
				new String[] { Long.toString(item._id), item.tracking});

		if (result > 0) {
			item.notifyProvider(context);
		}

		return result;
	}

	public synchronized Cursor getLogEntryCursor(final long id) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor cursor = db.query(LogEntry.TABLE_NAME, LogEntry.FIELDS,
				LogEntry.COL_ID + " IS ?", new String[] { String.valueOf(id) },
				null, null, null, null);
		return cursor;
	}

	public synchronized LogEntry getLogEntry(final long id) {
		final Cursor cursor = getLogEntryCursor(id);
		final LogEntry result;
		if (cursor.moveToFirst()) {
			result = new LogEntry(cursor);
		}
		else {
			result = null;
		}

		cursor.close();
		return result;
	}

	public synchronized Cursor getAllLogEntriesCursor(final String selection,
                  final String[] args, final String sortOrder) {
		final SQLiteDatabase db = this.getReadableDatabase();

		final Cursor cursor = db.query(LogEntry.TABLE_NAME, LogEntry.FIELDS,
				selection, args, null, null, sortOrder, null);

		return cursor;
	}

	public synchronized List<LogEntry> getAllLogEntries(final String selection,
                    final String[] args, final String sortOrder) {
		final List<LogEntry> result = new ArrayList<LogEntry>();

		final Cursor cursor = getAllLogEntriesCursor(selection, args, sortOrder);

		while (cursor.moveToNext()) {
			LogEntry q = new LogEntry(cursor);
			result.add(q);
		}

		cursor.close();
		return result;
	}

}
