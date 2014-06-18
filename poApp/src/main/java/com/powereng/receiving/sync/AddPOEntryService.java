package com.powereng.receiving.sync;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.powereng.receiving.database.POEntry;

import java.util.ArrayList;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AddPOEntryService extends IntentService {

	private static final String ACTION_ADD_PO = "com.powereng.receiving.action.ADDPO";
	private static final String EXTRA_PO = "com.powereng.receiving.extra.PACKAGE";
    private static final String EXTRA_PACKSLIP = "com.powereng.receiving.extra.PACKSLIP";
    private static final String ACTION_PACKSLIP = "com.powereng.receiving.action.PACKSLIP";
    private static final String ACTION_UPDATE_PO = "com.powereng.receiving.action.UPDATEPO";

    /**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 *
	 * @see android.app.IntentService
	 */
	public static void addPOEntry(Context context, Bundle extras) {
		Intent intent = new Intent(context, AddPOEntryService.class);


		intent.setAction(ACTION_ADD_PO).putExtra(EXTRA_PO, extras);
		context.startService(intent);
	}

    public static void addPackslip(Context context, Bundle extras) {
        Intent intent = new Intent(context, AddPOEntryService.class);

        intent.setAction(ACTION_PACKSLIP).putExtra(EXTRA_PACKSLIP, extras);
        context.startService(intent);
    }

    public static void updatePOEntry(Context context, Bundle extras) {
        Intent intent = new Intent(context, AddPOEntryService.class);

        intent.setAction(ACTION_UPDATE_PO).putExtra(EXTRA_PO, extras);
        context.startService(intent);
    }

	public AddPOEntryService() {
		super("AddLogEntryService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_ADD_PO.equals(action)) {
                addEntry(intent.getBundleExtra(EXTRA_PO));
			} else if (ACTION_UPDATE_PO.equals(action)) {
                updateEntry(intent.getBundleExtra(EXTRA_PO));
            } else {
                updateEntry(intent.getBundleExtra(EXTRA_PACKSLIP));
            }
		}
	}

    private void updateEntry(Bundle extras) {
        if (extras == null) {
            return;
        }
        Uri uri = Uri.parse(extras.getString("uri"));

        Boolean signed = extras.getBoolean("signed");
        final ContentValues values = new ContentValues();
        if (!signed) {
            ArrayList<String> list = extras.getStringArrayList("values");
            values.put(POEntry.COL_ID, list.get(0));
            values.put(POEntry.COL_PONUM, list.get(1));
            values.put(POEntry.COL_PROJECTNUM, list.get(2));
            values.put(POEntry.COL_VENDOR, list.get(3));
            values.put(POEntry.COL_DESCRIPTION, list.get(4));
            values.put(POEntry.COL_UNIT_TYPE, list.get(5));
            values.put(POEntry.COL_TOTAL, list.get(6));
            values.put(POEntry.COL_PRICE, list.get(7));
            values.put(POEntry.COL_RECEIVED, list.get(8));
            values.put(POEntry.COL_REMAINING, list.get(9));
            values.put(POEntry.COL_VALID_FROM, list.get(10));
            values.put(POEntry.COL_STATUS, 2);
        } else {
            values.put(POEntry.COL_PACKSLIP, extras.getString("signee"));
            values.put(POEntry.COL_STATUS, 4);
        }
        getContentResolver().update(uri, values, null, null);
    }


    private void addEntry(Bundle extras) {
		if (extras == null) {
			return;
		}

        ArrayList<String> list = extras.getStringArrayList("values");

        final ContentValues values = new ContentValues();

        values.put(POEntry.COL_ID, list.get(0));
        values.put(POEntry.COL_PONUM, list.get(1));
        values.put(POEntry.COL_PROJECTNUM, list.get(2));
        values.put(POEntry.COL_VENDOR, list.get(3));
        values.put(POEntry.COL_DESCRIPTION, list.get(4));
        values.put(POEntry.COL_UNIT_TYPE, list.get(5));
        values.put(POEntry.COL_TOTAL, list.get(6));
        values.put(POEntry.COL_PRICE, list.get(7));
        values.put(POEntry.COL_RECEIVED, list.get(8));
        values.put(POEntry.COL_REMAINING, list.get(9));
        values.put(POEntry.COL_VALID_FROM, list.get(10));
        values.put(POEntry.COL_STATUS, 2);

		getContentResolver().insert(POEntry.URI(), values);
	}
}
