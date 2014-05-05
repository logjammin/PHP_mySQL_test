package com.powereng.receiving.sync;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.powereng.receiving.accounts.GenericAccountService;
import com.powereng.receiving.database.LogProvider;

/**
 * Created by qgallup on 5/5/2014.
 */
public class TableWatcher extends ContentObserver {


    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public TableWatcher(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(),      // Sync account
                LogProvider.AUTHORITY, // Content authority
                null);
    }
}
