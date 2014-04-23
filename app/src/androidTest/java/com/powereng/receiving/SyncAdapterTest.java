package com.powereng.receiving;

import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.test.ServiceTestCase;
import android.util.Log;

import com.powereng.receiving.accounts.GenericAccountService;
import com.powereng.receiving.provider.ReceivingLogContract;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Created by qgallup on 4/22/2014.
 */
public class SyncAdapterTest extends ServiceTestCase<SyncService> {
    private static final String TAG = "SyncAdapterTest: ";
    public static final String SCHEME = "content://";





    public SyncAdapterTest() {
        super(SyncService.class);
    }



    public void testIncomingFeedParsed()
            throws IOException, JSONException, RemoteException,
            OperationApplicationException, ParseException {
        String sampleFeed = "{\"receiving_log\":[{\"date_received\":\"2014-22-04 12:25:45\"," +
                "\"tracking\":\"r75477\"," +
                "\"carrier\":\"UPS\",\"sender\":\"Ford\",\"recipient\":\"Gerald\"," +
                "\"numpackages\":\"1\",\"po_num\":null,\"sig\":null}],\"success\":1}";



        InputStream stream = new ByteArrayInputStream(sampleFeed.getBytes());
        SyncAdapter adapter = new SyncAdapter(getContext(), false);
        try {
            adapter.updateLocalFeedData(stream, new SyncResult());
        } catch (JSONException e) {
            Log.w(TAG, "yo dawg, " + e);
        }
        Context ctx = getContext();
        assert ctx != null;
        ContentResolver cr = ctx.getContentResolver();
        final String[] projection = new String[] {
                ReceivingLogContract.Entry._ID,
                ReceivingLogContract.Entry.COLUMN_NAME_DATE,
                ReceivingLogContract.Entry.COLUMN_NAME_TRACKING,
                ReceivingLogContract.Entry.COLUMN_NAME_CARRIER,
                ReceivingLogContract.Entry.COLUMN_NAME_NUMPACKAGES,
                ReceivingLogContract.Entry.COLUMN_NAME_SENDER,
                ReceivingLogContract.Entry.COLUMN_NAME_RECIPIENT,
                ReceivingLogContract.Entry.COLUMN_NAME_PO_NUM,
                ReceivingLogContract.Entry.COLUMN_NAME_SIG
        };
        Cursor c = cr.query(ReceivingLogContract.Entry.CONTENT_URI, projection, null, null, null);

        assert c != null;
        assertEquals(1, c.getCount());
        c.moveToFirst();
        String tracking = c.getString(0);


        //TODO: create a content resolver and test its functionality.


    }
}
