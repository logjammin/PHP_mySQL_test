package com.powereng.receiving;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.powereng.receiving.database.LogEntry;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AddLogEntryService extends IntentService {

	private static final String ACTION_ADD = "com.powereng.receiving.action.ADD";
	private static final String EXTRA_PACKAGE = "com.powereng.receiving.extra.PACKAGE";
    private static final String ACTION_UPDATE = "com.powereng.receiving.action.UPDATE";

    /**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void addEntry(Context context, Bundle extras) {
		Intent intent = new Intent(context, AddLogEntryService.class);


		intent.setAction(ACTION_ADD).putExtra(EXTRA_PACKAGE, extras);
		context.startService(intent);
	}

    public static void updateEntry(Context context, Bundle extras) {
        Intent intent = new Intent(context, AddLogEntryService.class);
        intent.setAction(ACTION_UPDATE).putExtra(EXTRA_PACKAGE, extras);
        context.startService(intent);
    }
	public AddLogEntryService() {
		super("AddLogEntryService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_ADD.equals(action)) {
                addEntry(intent.getBundleExtra(EXTRA_PACKAGE));
			} else if (ACTION_UPDATE.equals(action)) {
                updateEntry(intent.getBundleExtra(EXTRA_PACKAGE));
            }
		}
	}

    private void updateEntry(Bundle extras) {
        if (extras == null) {
            return;
        }
        Uri uri = Uri.parse(extras.getString("uri"));
        ArrayList<String> list = extras.getStringArrayList("values");

        final ContentValues values = new ContentValues();

        values.put(LogEntry.COL_TRACKING, list.get(0));
        values.put(LogEntry.COL_CARRIER, list.get(1));
        values.put(LogEntry.COL_NUMPACKAGES, list.get(2));
        values.put(LogEntry.COL_SENDER, list.get(3));
        values.put(LogEntry.COL_RECIPIENT, list.get(4));
        values.put(LogEntry.COL_PONUM, list.get(5));
        values.put(LogEntry.COL_SYNC_STATUS, 2);

        getContentResolver().update(uri, values, null, null);
    }


    private void addEntry(Bundle extras) {
		if (extras == null) {
			return;
		}

        ArrayList<String> list = extras.getStringArrayList("values");

        final ContentValues values = new ContentValues();

        values.put(LogEntry.COL_TRACKING, list.get(0));
        values.put(LogEntry.COL_CARRIER, list.get(1));
        values.put(LogEntry.COL_NUMPACKAGES, list.get(2));
        values.put(LogEntry.COL_SENDER, list.get(3));
        values.put(LogEntry.COL_RECIPIENT, list.get(4));
        values.put(LogEntry.COL_PONUM, list.get(5));
        values.put(LogEntry.COL_SYNC_STATUS, 1);

		getContentResolver().insert(LogEntry.URI(), values);
	}
}
