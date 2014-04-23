package com.powereng.receiving.provider;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.powereng.receiving.accounts.GenericAccountService;

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
        ContentResolver.requestSync(GenericAccountService.GetAccount(),
                ReceivingLogContract.CONTENT_AUTHORITY, extras);
    }
}