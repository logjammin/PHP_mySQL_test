package com.powereng.receiving.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.powereng.receiving.database.DatabaseHandler;
import com.powereng.receiving.database.LogEntry;
import com.powereng.receiving.sync.SyncHelper;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GCMIntentService extends IntentService {
	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

			// If it's a regular GCM message, do some work.
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// Write link to database
				final LogEntry entry = new LogEntry();
                entry.timestamp = extras.getString("timestamp");
                entry.tracking = extras.getString("tracking");
                entry.numpackages = extras.getString("numpackages");
				entry.carrier = extras.getString("carrier");
                entry.sender = extras.getString("sender");
                entry.recipient = extras.getString("recipient");
                entry.ponum = extras.getString("ponum");
                //TODO: sig shouldn't be a string.
                entry.sig = extras.getString("sig");
				entry.synced = 1;
				if (Boolean.parseBoolean(extras.getString("deleted", "false"))) {
					entry.deleted = 1;
				}

				if (entry.deleted == 0) {
					DatabaseHandler.getInstance(this).putEntry(entry);
				}
				else {
					DatabaseHandler.getInstance(this).deleteEntry(entry);
				}

				Log.i("linksgcm", "Received: " + extras.toString()
						+ ", deleted: " + entry.deleted);
			}
			else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				// We reached the limit of 100 queued messages. Request a full
				// sync
				SyncHelper.requestSync(this);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GCMReceiver.completeWakefulIntent(intent);
	}
}
