package com.powereng.receiving.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;

public class PoProvider extends ContentProvider {
	public static final String AUTHORITY = "com.powereng.receiving";
	public static final String SCHEME = "content://";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		POEntry.addMatcherUris(sURIMatcher);
	}


	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Setup some common parsing and stuff
		final String table;
		final ContentValues values = new ContentValues();
		final ArrayList<String> args = new ArrayList<String>();
		if (selectionArgs != null) {
			for (String arg : selectionArgs) {
				args.add(arg);
			}
		}
		final StringBuilder sb = new StringBuilder();
		if (selection != null && !selection.isEmpty()) {
			sb.append("(").append(selection).append(")");
		}

		// Configure table and args depending on uri
		switch (sURIMatcher.match(uri)) {
		case POEntry.BASEITEMCODE:
			table = POEntry.TABLE_NAME;
			if (selection != null && !selection.isEmpty()) {
				sb.append(" AND ");
			}
			sb.append(POEntry.COL_ID + " IS ?");
			args.add(uri.getLastPathSegment());
			values.put(POEntry.COL_STATUS, 3);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Write to DB
		final SQLiteDatabase db = DatabaseHandler.getInstance(getContext())
				.getWritableDatabase();
		final String[] argArray = new String[args.size()];
		final int result = db.update(table, values, sb.toString(),
				args.toArray(argArray));

		if (result > 0) {
			// Support upload sync
			getContext().getContentResolver().notifyChange(uri, null, true);
		}
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final Uri result;
		final String table;
		final DBItem item; // Just used for getting final URI

		// Configure table and args depending on uri
		switch (sURIMatcher.match(uri)) {
		case POEntry.BASEURICODE:
			table = POEntry.TABLE_NAME;

            //TODO: use this for entry ID on server.
            /*if (!values.containsKey(LogEntry.COL_SHA)) {
				values.put(LogEntry.COL_SHA, LinkIDGenerator.generateID());
			}*/

			item = new POEntry();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Write to DB
		final SQLiteDatabase db = DatabaseHandler.getInstance(getContext())
				.getWritableDatabase();
        values.put(POEntry.COL_STATUS, 1);
		final long id = db.insert(table, null, values);

		if (id > 0) {
			item.setId(id);
			result = item.getUri();
            //ContentObserver observer = new TableWatcher(null);
			// Support upload sync
			getContext().getContentResolver().notifyChange(uri, null, true);
		}
		else {
			result = null;
		}

		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        final String table;
        final ArrayList<String> args = new ArrayList<String>();
        if (selectionArgs != null) {
            for (String arg : selectionArgs) {
                args.add(arg);
            }
        }
        final StringBuilder sb = new StringBuilder();
        if (selection != null && !selection.isEmpty()) {
            sb.append("(").append(selection).append(")");
        }

        // Configure table and args depending on uri
        switch (sURIMatcher.match(uri)) {
            case POEntry.BASEITEMCODE:
                table = POEntry.TABLE_NAME;
                if (selection != null && !selection.isEmpty()) {
                    sb.append(" AND ");
                }
                sb.append(POEntry.COL_ID + " IS ?");
                args.add(uri.getLastPathSegment());
                //TODO: this is keeping the signatures from being uploaded.
                int synced = values.getAsInteger("status");
                if (synced != 4) {
                    values.put(POEntry.COL_STATUS, 2);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Write to DB
        final SQLiteDatabase db = DatabaseHandler.getInstance(getContext())
                .getWritableDatabase();
        final String[] argArray = new String[args.size()];
        final int result = db.update(table, values, sb.toString(),
                args.toArray(argArray));

        if (result > 0) {
            // Support upload sync
            getContext().getContentResolver().notifyChange(uri, null, true);
        }

		//throw new UnsupportedOperationException("Not yet implemented");
        return result;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {

		case POEntry.BASEITEMCODE:
			return POEntry.TYPE_ITEM;
		case POEntry.BASEURICODE:
			return POEntry.TYPE_DIR;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] args, String sortOrder) {
		Cursor result;
		final long id;
		final DatabaseHandler handler = DatabaseHandler
				.getInstance(getContext());

		switch (sURIMatcher.match(uri)) {

		case POEntry.BASEITEMCODE:
			id = Long.parseLong(uri.getLastPathSegment());
			result = handler.getLogEntryCursor(id);
			result.setNotificationUri(getContext().getContentResolver(), uri);
			break;
		case POEntry.BASEURICODE:
            if (projection.length == 2) {
                result = handler.textViewCursor(projection, selection, args, sortOrder);
            } else {
                result = handler.getAllLogEntriesCursor(selection, args, sortOrder);
            }
            result.setNotificationUri(getContext().getContentResolver(), uri);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return result;
	}
}
