package com.powereng.receiving;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.powereng.receiving.database.LogEntry;

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

        final ContentValues values = new ContentValues();
        values.put(LogEntry.COL_TRACKING, extras.getString("tracking"));
        values.put(LogEntry.COL_CARRIER, extras.getString("carrier"));
        values.put(LogEntry.COL_NUMPACKAGES, extras.getString("numpackages"));
        values.put(LogEntry.COL_SENDER, extras.getString("sender"));
        values.put(LogEntry.COL_RECIPIENT, extras.getString("recipient"));
        values.put(LogEntry.COL_PONUM, extras.getString("ponum"));
        //TODO: this shit needs updated.
        getContentResolver().update(LogEntry.URI(), values, null, null);
    }


    private void addEntry(Bundle extras) {
		if (extras == null) {
			return;
		}

		final ContentValues values = new ContentValues();
		values.put(LogEntry.COL_TRACKING, extras.getString("tracking"));
        values.put(LogEntry.COL_CARRIER, extras.getString("carrier"));
        values.put(LogEntry.COL_NUMPACKAGES, extras.getString("numpackages"));
        values.put(LogEntry.COL_SENDER, extras.getString("sender"));
        values.put(LogEntry.COL_RECIPIENT, extras.getString("recipient"));
        values.put(LogEntry.COL_PONUM, extras.getString("ponum"));

		getContentResolver().insert(LogEntry.URI(), values);
	}
}
