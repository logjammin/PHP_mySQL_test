package com.powereng.receiving.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.powereng.receiving.database.DatabaseHandler;
import com.powereng.receiving.database.POEntry;

import java.io.File;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String TAG = "LogSyncAdapter";
	private static final String KEY_LASTSYNC = "key_lastsync";
    private static final int SYNCED = 0;
    private static final int INSERT = 1;
    private static final int UPDATE = 2;
    private static final int DELETE = 3;
    private static final int SIGNED = 4;

    Context mContext;
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
        mContext = context;
	}

	public SyncAdapter(Context context, boolean autoInitialize,
                       boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
        try {
			// Need to get an access token first
			final String token = "d3f2ece245bb00d9706234a9f5f0753e";

			if (token == null) {
				Log.e(TAG, "Token was null. Aborting sync");
				// Sync is rescheduled by SyncHelper
				return;
			}


			final POServer server = SyncUtils.getRESTAdapter();
			DatabaseHandler db = DatabaseHandler.getInstance(getContext());

			// Upload stuff
			for (POEntry entry : db.getAllLogEntries(POEntry.COL_STATUS
					+ " IS NOT " + SYNCED, null, null)) {

                switch(entry.status) {

                    case DELETE:
                        server.deletePOEntry(token, entry.ponum);
                        db.deleteEntry(entry);
                        break;

                    case INSERT:
                        server.addPOEntry(token, new POServer.POMsg(entry));
                        //TODO: needs some kind of error checking
                        syncResult.stats.numInserts++;
                        //flag entry as "synced" in local db
                        entry.status = SYNCED;
                        db.putEntry(entry);
                        break;

                    case UPDATE:
                        server.getPOEntry(token, entry.ponum, new POServer.POMsg(entry));
                        syncResult.stats.numUpdates++;
                        //flag entry as "synced" in local db
                        entry.status = SYNCED;
                        db.putEntry(entry);
                        break;

                    case SIGNED:
                        String fileName = entry.ponum + "_" + entry.unitPrice + ".png";
                        File file = new File(mContext.getFilesDir(), fileName);
                        TypedFile outFile = new TypedFile("image/png", file);
                        server.addSignature(token, outFile);
                        server.getPOEntry(token, entry.ponum, new POServer.POMsg(entry));
                        syncResult.stats.numUpdates++;
                        //flag entry as "synced" in local db
                        entry.status = SYNCED;
                        db.putEntry(entry);
                        break;
                }
			}



			// Download stuff - but only if this is not an upload-only sync
			if (!extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false)) {
				// Check if we synced before
				String lastSync = PreferenceManager
						.getDefaultSharedPreferences(getContext()).getString(
								KEY_LASTSYNC, null);
                //final String lastSync = "2014-04-30 12:24:40.0";

				final POServer.POEntries entries;
				if (lastSync != null && !lastSync.isEmpty()) {

					entries = server.listEntries(token, lastSync);
                    //entries = server.listEntries(token);
                }

				else {
                    lastSync = "2014-04-30 12:24:40.0";
                    entries = server.listEntries(token, lastSync);
					//entries = server.listEntries(token);
				}

				if (entries != null && entries.entries != null) {
					for (POServer.POMsg msg : entries.entries) {
						Log.d(TAG, "got ponum:" + msg.tracking);
						final POEntry entry = msg.toDBItem();
						//if (msg.deleted) {
						//	Log.d(TAG, "Deleting:" + msg.id);
						//	db.deletePOEntry(entry);
						//}
						//else {
							Log.d(TAG, "Adding ponum:" + entry.ponum);
							entry.status = SYNCED;
							db.putEntry(entry);
						//}
					}
				}

				// Save sync validFrom
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				PreferenceManager.getDefaultSharedPreferences(getContext())
						.edit().putString(KEY_LASTSYNC, entries.latestTimestamp)
						.commit();
			}
		}
		catch (RetrofitError e) {
			Log.d(TAG, "" + e);
			final int status;
			if (e.getResponse() != null) {
				Log.e(TAG, "" + e.getResponse().getStatus() + "; "
						+ e.getResponse().getReason());
				status = e.getResponse().getStatus();
			}
			else {
				status = 999;
			}
			// An HTTP error was encountered.
			switch (status) {
			case 401: // Unauthorized
				syncResult.stats.numAuthExceptions++;
				break;
			case 404: // No such item, should never happen, programming error
			case 415: // Not proper body, programming error
			case 400: // Didn't specify url, programming error
				syncResult.databaseError = true;
				break;
			default: // Default is to consider it a networking problem
				syncResult.stats.numIoExceptions++;
				break;
			}
		}
	}
}
