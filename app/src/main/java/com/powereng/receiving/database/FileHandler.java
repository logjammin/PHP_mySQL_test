package com.powereng.receiving.database;

import android.net.Uri;

/**
 * Created by qgallup on 5/9/2014.
 */
public class FileHandler {
    public static String FILES = ".files/";
    public static Uri URI() {
        return Uri.withAppendedPath(
                Uri.parse(LogProvider.SCHEME
                        + LogProvider.AUTHORITY), FILES);
    }


}
