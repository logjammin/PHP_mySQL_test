package com.powereng.receiving.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.test.ProviderTestCase2;

import com.powereng.receiving.accounts.GenericAccountService;

/**
 * Created by qgallup on 4/22/2014.
 */
public class ReceivingLogProviderTest extends ProviderTestCase2<ReceivingLogProvider>{
    public ReceivingLogProviderTest() {
        super(ReceivingLogProvider.class, ReceivingLogContract.CONTENT_AUTHORITY);
    }
    public void testEntryContentUriIsSane() {
        assertEquals(Uri.parse("content://com.powereng.receiving/entries"),
                ReceivingLogContract.LogEntry.CONTENT_URI);
    }

    public class TableObserver extends ContentObserver {


        public TableObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Bundle extras = new Bundle();
            extras.putString("uri", uri.toString());
            ContentResolver.requestSync(GenericAccountService.GetAccount(), ReceivingLogContract.CONTENT_AUTHORITY, extras);
        }
    }

    public void testCreateAndRetrieve() {
        // Create
        ContentValues newValues = new ContentValues();
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING, "1Z8465684A");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER, "UPS");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES, "5");
        Uri newUri = getMockContentResolver().insert(
                ReceivingLogContract.LogEntry.CONTENT_URI,
                newValues);

        // Retrieve
        String[] projection = {
                ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING,      // 0
                ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER,       // 1
                ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES};  // 2
        Cursor c = getMockContentResolver().query(newUri, projection, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals("1Z8465684A", c.getString(0));
        assertEquals("UPS", c.getString(1));
        assertEquals("5", c.getString(2));
    }

    public void testCreateAndQuery() {
        // Create
        ContentValues newValues = new ContentValues();
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING, "Alpha-1Z8465684A");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER, "PAPERCLIPS-Alpha");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES, "Alpha-1");
        getMockContentResolver().insert(
                ReceivingLogContract.LogEntry.CONTENT_URI,
                newValues);

        newValues = new ContentValues();
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING, "Beta-1Z8465684A");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER, "FEDEX-Beta");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES, "Beta-2");
        getMockContentResolver().insert(
                ReceivingLogContract.LogEntry.CONTENT_URI,
                newValues);

        // Retrieve
        String[] projection = {
                ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING,      // 0
                ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER,       // 1
                ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES};  // 2
        String where = ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING + " LIKE ?";
        Cursor c = getMockContentResolver().query(ReceivingLogContract.LogEntry.CONTENT_URI, projection,
                where, new String[] {"Alpha%"}, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();


    }

    public void testUpdate() {
        // Create
        ContentValues newValues = new ContentValues();
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING, "Alpha-MyTitle");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER, "http://alpha.example.com");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES, "Alpha-MyEntryID");
        Uri alpha = getMockContentResolver().insert(
                ReceivingLogContract.LogEntry.CONTENT_URI,
                newValues);

        newValues = new ContentValues();
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING, "Beta-MyTitle");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER, "http://beta.example.com");
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES, "Beta-MyEntryID");
        Uri beta = getMockContentResolver().insert(
                ReceivingLogContract.LogEntry.CONTENT_URI,
                newValues);

        // Update
        newValues = new ContentValues();
        newValues.put(ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER, "http://replaced.example.com");
        getMockContentResolver().update(alpha, newValues, null, null);

        // Retrieve
        String[] projection = {
                ReceivingLogContract.LogEntry.COLUMN_NAME_TRACKING,      // 0
                ReceivingLogContract.LogEntry.COLUMN_NAME_CARRIER,       // 1
                ReceivingLogContract.LogEntry.COLUMN_NAME_NUMPACKAGES};  // 2
        // Check that alpha was updated
        Cursor c = getMockContentResolver().query(alpha, projection, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals("Alpha-MyTitle", c.getString(0));
        assertEquals("http://replaced.example.com", c.getString(1));
        assertEquals("Alpha-MyEntryID", c.getString(2));

        // ...and that beta was not
        c = getMockContentResolver().query(beta, projection, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals("Beta-MyTitle", c.getString(0));
        assertEquals("http://beta.example.com", c.getString(1));
        assertEquals("Beta-MyEntryID", c.getString(2));
    }
}
