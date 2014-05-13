package com.powereng.receiving.database;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Represents Link in the database.
 *
 */
public class LogEntry extends DBItem {
    public static final String TABLE_NAME = "Entry";

    public static Uri URI() {
        return Uri.withAppendedPath(
            Uri.parse(LogProvider.SCHEME
                      + LogProvider.AUTHORITY), TABLE_NAME);
    }

    // Column names
    public static final String COL__ID = "_id";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_TRACKING = "tracking";
    public static final String COL_CARRIER = "carrier";
    public static final String COL_NUMPACKAGES = "numpackages";
    public static final String COL_SENDER = "sender";
    public static final String COL_RECIPIENT = "recipient";
    public static final String COL_PONUM = "ponum";
    public static final String COL_SIG = "signature";
    public static final String COL_SYNC_STATUS = "sync_status";

    // For database projection so order is consistent
    public static final String[] FIELDS = { COL__ID, COL_TRACKING,
            COL_CARRIER, COL_NUMPACKAGES, COL_SENDER, COL_RECIPIENT,
            COL_PONUM, COL_SIG, COL_TIMESTAMP, COL_SYNC_STATUS};

    public long _id = -1;
    public String tracking;
    public String carrier;
    public String numpackages;
    public String sender;
    public String recipient;
    public String ponum;
    public String sig;
    public String timestamp = null;
    public int sync_status = 0;

    public static final int BASEURICODE = 0x3b109c7;
    public static final int BASEITEMCODE = 0x87a22b7;

    public static void addMatcherUris(UriMatcher sURIMatcher) {
        sURIMatcher.addURI(LogProvider.AUTHORITY, TABLE_NAME, BASEURICODE);
        sURIMatcher.addURI(LogProvider.AUTHORITY, TABLE_NAME + "/#", BASEITEMCODE);
    }

    public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.powereng." + TABLE_NAME;
    public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.powereng." + TABLE_NAME;

    public LogEntry() {
        super();
    }

    public LogEntry(final Cursor cursor) {
        super();
        // Projection expected to match FIELDS array
        this._id = cursor.getLong(0);
        this.tracking = cursor.getString(1);
        this.carrier = cursor.getString(2);
        this.numpackages = cursor.getString(3);
        this.sender = cursor.getString(4);
        this.recipient = cursor.getString(5);
        this.ponum = cursor.getString(6);
        this.sig = cursor.getString(7);
        this.timestamp = cursor.getString(8);
        this.sync_status = cursor.getInt(9);
    }

    public ContentValues getContent() {
        ContentValues values = new ContentValues();
        if (tracking != null)
        values.put(COL_TRACKING, tracking);
        values.put(COL_CARRIER, carrier);
        values.put(COL_NUMPACKAGES, numpackages);
        values.put(COL_SENDER, sender);
        values.put(COL_RECIPIENT, recipient);
        values.put(COL_PONUM, ponum);
        values.put(COL_SIG, sig);
        values.put(COL_TIMESTAMP, timestamp);
        values.put(COL_SYNC_STATUS, sync_status);
        return values;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public String[] getFields() {
        return FIELDS;
    }

    public long getId() {
        return _id;
    }

    public void setId(final long id) {
        _id = id;
    }

    public static final String CREATE_TABLE =
"CREATE TABLE " + TABLE_NAME
+"  (_id INTEGER NOT NULL PRIMARY KEY,"
+"  tracking TEXT NOT NULL,"
+"  carrier TEXT NULL,"
+"  numpackages TEXT NULL,"
+"  sender TEXT NULL,"
+"  recipient TEXT NULL,"
+"  ponum TEXT NULL,"
+"  signature TEXT NULL,"
+"  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
+"  sync_status INTEGER NOT NULL DEFAULT 1,"
+""
+"  UNIQUE (tracking) ON CONFLICT IGNORE,"
+"  UNIQUE (timestamp) ON CONFLICT IGNORE);";
}
