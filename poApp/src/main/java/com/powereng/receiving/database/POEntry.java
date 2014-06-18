package com.powereng.receiving.database;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Represents Link in the database.
 *
 */
public class POEntry extends DBItem {
    public static final String TABLE_NAME = "requisitions";

    public static Uri URI() {
        return Uri.withAppendedPath(
            Uri.parse(PoProvider.SCHEME
                      + PoProvider.AUTHORITY), TABLE_NAME);
    }

    // Column names
    public static final String COL_ID = "id";
    public static final String COL_VALID_FROM = "valid_from";
    public static final String COL_PONUM = "ponum";
    public static final String COL_PROJECTNUM = "projectnum";
    public static final String COL_VENDOR = "vendor";
    public static final String COL_DESCRIPTION = "item_desc";
    public static final String COL_UNIT_TYPE = "unit_type";
    public static final String COL_TOTAL = "total";
    public static final String COL_PRICE = "unit_price";
    public static final String COL_RECEIVED = "receieved";
    public static final String COL_REMAINING = "remaining";
    public static final String COL_PACKSLIP = "packslip";
    public static final String COL_VALID_UNTIL = "valid_until";
    public static final String COL_STATUS = "status";

    // For database projection so order is consistent
    public static final String[] FIELDS = { COL_ID, COL_PONUM,
            COL_PROJECTNUM, COL_VENDOR, COL_DESCRIPTION, COL_UNIT_TYPE, COL_TOTAL,
             COL_PRICE, COL_RECEIVED, COL_REMAINING, COL_PACKSLIP, COL_VALID_FROM, COL_STATUS};

    public long poID = -1;
    public String projectnum;
    public String ponum;
    public String carrier;
    public String vendor;
    public String description;
    public String unitType;
    public int total;
    public String unitPrice;
    public int received;
    public int remaining;
    public String packslip;
    public String validUntil;
    public String validFrom;
    public int status = 0;

    public static final int BASEURICODE = 0x3b109c7;
    public static final int BASEITEMCODE = 0x87a22b7;

    public static void addMatcherUris(UriMatcher sURIMatcher) {
        sURIMatcher.addURI(PoProvider.AUTHORITY, TABLE_NAME, BASEURICODE);
        sURIMatcher.addURI(PoProvider.AUTHORITY, TABLE_NAME + "/#", BASEITEMCODE);
    }

    public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.powereng." + TABLE_NAME;
    public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.powereng." + TABLE_NAME;

    public POEntry() {
        super();
    }

    public POEntry(final Cursor cursor) {
        super();
        // Projection expected to match FIELDS array
        this.poID = cursor.getLong(0);
        this.ponum = cursor.getString(1);
        this.projectnum = cursor.getString(2);
        this.vendor = cursor.getString(3);
        this.description = cursor.getString(4);
        this.unitType = cursor.getString(5);
        this.total = cursor.getInt(6);
        this.unitPrice = cursor.getString(7);
        this.received = cursor.getInt(8);
        this.remaining = cursor.getInt(9);
        this.packslip = cursor.getString(10);
        this.validFrom = cursor.getString(11);
        this.validUntil = cursor.getString(12);
        this.status = cursor.getInt(13);
    }

    public ContentValues getContent() {
        ContentValues values = new ContentValues();
        if (ponum != null) {
            values.put(COL_ID, poID);
            values.put(COL_PONUM, ponum);
            values.put(COL_PROJECTNUM, projectnum);
            values.put(COL_VENDOR, vendor);
            values.put(COL_DESCRIPTION, description);
            values.put(COL_UNIT_TYPE, unitType);
            values.put(COL_TOTAL, total);
            values.put(COL_PRICE, unitPrice);
            values.put(COL_RECEIVED, received);
            values.put(COL_REMAINING, received);
            values.put(COL_PACKSLIP, packslip);
            values.put(COL_VALID_FROM, validFrom);
            values.put(COL_VALID_UNTIL, validUntil);
            values.put(COL_STATUS, status);
        }
        return values;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public String[] getFields() {
        return FIELDS;
    }

    public long getId() {
        return poID;
    }

    public void setId(final long id) {
        poID = id;
    }

    public static final String CREATE_TABLE =
        "CREATE TABLE " + TABLE_NAME
        +"  (_id INTEGER NOT NULL,"
        +"  ponum TEXT NOT NULL,"
        +"  projectnum TEXT NOT NULL,"
        +"  vendor TEXT NULL,"
        +"  item_desc TEXT NULL,"
        +"  unit_type TEXT NULL,"
        +"  total INTEGER NULL,"
        +"  unit_price TEXT NULL,"
        +"  received INTEGER NULL,"
        +"  remaining INTEGER NULL,"
        +"  packslip TEXT NULL,"
        +"  valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
        +"  valid_until TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',"
        +"  status INTEGER NOT NULL DEFAULT 0,"
        +""
        +"  PRIMARY KEY (_id, ponum, valid_from) ON CONFLICT IGNORE);";
}
