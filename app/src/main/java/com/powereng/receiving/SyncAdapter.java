package com.powereng.receiving;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.powereng.receiving.net.JSONParser;
import com.powereng.receiving.provider.ReceivingLogContract;

import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qgallup on 4/14/14.
 */

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL to fetch content from during a sync.
     *
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */
    private static final String FEED_URL = "http://boi40310ll.powereng.com/android_sync.php";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    private static final int QUERY =0;
    private static final int INSERT =1;
    private static final int UPDATE =2;
    private static final int DELETE =3;

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[] {
            ReceivingLogContract.Entry._ID,
            ReceivingLogContract.Entry.COLUMN_NAME_DATE,
            ReceivingLogContract.Entry.COLUMN_NAME_TRACKING,
            ReceivingLogContract.Entry.COLUMN_NAME_CARRIER,
            ReceivingLogContract.Entry.COLUMN_NAME_PCS,
            ReceivingLogContract.Entry.COLUMN_NAME_SENDER,
            ReceivingLogContract.Entry.COLUMN_NAME_RECIPIENT,
            ReceivingLogContract.Entry.COLUMN_NAME_PO,
            ReceivingLogContract.Entry.COLUMN_NAME_SIG
    };

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_DATE = 1;
    public static final int COLUMN_TRACKING = 2;
    public static final int COLUMN_CARRIER = 3;
    public static final int COLUMN_PCS = 4;
    public static final int COLUMN_SENDER = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_PO = 7;
    public static final int COLUMN_SIG = 8;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        //TODO: add output stream method using the extras param.

        int method = extras.getInt("method");
        switch(method) {


            case QUERY:

            try {
                final URL location = new URL(FEED_URL);
                InputStream stream = null;

                try {
                    Log.i(TAG, "Streaming data from network: " + location);
                    stream = downloadUrl(location);
                    updateLocalFeedData(stream, syncResult);
                    // Makes sure that the InputStream is closed after the app is
                    // finished using it.
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Feed URL is malformed", e);
                syncResult.stats.numParseExceptions++;
                return;
            } catch (IOException e) {
                Log.e(TAG, "Error reading from network: " + e.toString());
                syncResult.stats.numIoExceptions++;
                return;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing feed: " + e.toString());
                syncResult.stats.numParseExceptions++;
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "Error updating database: " + e.toString());
                syncResult.databaseError = true;
                return;
            } catch (OperationApplicationException e) {
                Log.e(TAG, "Error updating database: " + e.toString());
                syncResult.databaseError = true;
                return;
            }
            Log.i(TAG, "Network synchronization complete");
            break;

            //TODO: get the info'z for this shit.
            case INSERT:

            try {
                final URL location = new URL(FEED_URL);
                OutputStream stream = null;

                try {
                    Log.i(TAG, "Streaming data to network: " + location);
                    stream = uploadUrl(location);
                    updateServerData(stream, syncResult);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Feed URL is malformed", e);
                syncResult.stats.numParseExceptions++;
                return;
            } catch (IOException e) {
                Log.e(TAG, "Error reading from network: " + e.toString());
                syncResult.stats.numIoExceptions++;
                return;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing feed: " + e.toString());
                syncResult.stats.numParseExceptions++;
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "Error updating database: " + e.toString());
                syncResult.databaseError = true;
                return;
            } catch (OperationApplicationException e) {
                Log.e(TAG, "Error updating database: " + e.toString());
                syncResult.databaseError = true;
                return;
            }
            Log.i(TAG, "Network synchronization complete");
            break;

            //TODO: figure this shit out too.
            case UPDATE:
                break;
            case DELETE:
                break;
        }
    }

    /**
     * Read XML from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
    public void updateLocalFeedData(final InputStream stream, final SyncResult syncResult)
            throws IOException, JSONException, RemoteException,
            OperationApplicationException {
        final JSONParser jParser = new JSONParser();
        final ContentResolver contentResolver = getContext().getContentResolver();

        final List<JSONParser.Entry> entries = jParser.loadAllEntries();
        Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, JSONParser.Entry> entryMap = new HashMap<String, JSONParser.Entry>();
        for (JSONParser.Entry e : entries) {
            entryMap.put(e.date, e);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = ReceivingLogContract.Entry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        //TODO: use date and tracking to compare entries. Remove all other junk.
        int id;
        String date;
        String tracking;
        String carrier;
        String pcs;
        String sender;
        String recipient;
        String ponum;
        String sig;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            date = c.getString(COLUMN_DATE);
            tracking = c.getString(COLUMN_TRACKING);
            carrier = c.getString(COLUMN_CARRIER);
            pcs = c.getString(COLUMN_PCS);
            sender = c.getString(COLUMN_SENDER);
            recipient = c.getString(COLUMN_RECIPIENT);
            ponum = c.getString(COLUMN_PO);
            sig = c.getString(COLUMN_SIG);
            JSONParser.Entry match = entryMap.get(tracking);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(tracking);
                // Check to see if the entry needs to be updated
                Uri existingUri = ReceivingLogContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.tracking != null && !match.tracking.equals(tracking)) ||
                        (match.carrier != null && !match.carrier.equals(carrier)) ||
                        (match.pcs != null && !match.pcs.equals(pcs)) ||
                        (match.sender != null && !match.sender.equals(sender)) ||
                        (match.recipient != null && !match.recipient.equals(recipient)) ||
                        (match.ponum != null && !match.ponum.equals(ponum)) ||
                        (match.sig != null && !match.sig.equals(sig))) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_TRACKING, tracking)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_CARRIER, carrier)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_PCS, pcs)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_SENDER, sender)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_RECIPIENT, recipient)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_PO, ponum)
                            .withValue(ReceivingLogContract.Entry.COLUMN_NAME_SIG, sig)
                            .build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = ReceivingLogContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (JSONParser.Entry e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: date=" + e.date);
            batch.add(ContentProviderOperation.newInsert(ReceivingLogContract.Entry.CONTENT_URI)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_DATE, e.date)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_TRACKING, e.tracking)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_CARRIER, e.carrier)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_PCS, e.pcs)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_SENDER, e.sender)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_RECIPIENT, e.recipient)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_PO, e.ponum)
                    .withValue(ReceivingLogContract.Entry.COLUMN_NAME_SIG, e.sig)
                    .build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(ReceivingLogContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                ReceivingLogContract.Entry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */

    public void updateServerData(final OutputStream stream, final SyncResult syncResult)
            throws IOException,JSONException, RemoteException,
            OperationApplicationException {

    }

    private InputStream downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private OutputStream uploadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0);

        OutputStream out = new BufferedOutputStream(conn.getOutputStream());

        return out;
    }
}