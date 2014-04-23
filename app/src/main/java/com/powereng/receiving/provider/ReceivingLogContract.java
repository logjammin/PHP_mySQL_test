package com.powereng.receiving.provider;
/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class ReceivingLogContract {
    private ReceivingLogContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.powereng.receiving";

    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "entry"-type resources..
     */
    private static final String PATH_ENTRIES = "entries";

    /**
     * Columns supported by "entries" records.
     */
    public static class Entry implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.receiving.entries";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.receiving.entry";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY
                + "/entries");

        /**
         * Table name where records are stored for "entry" resources.
         */
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_DATE = "date_received";
        public static final String COLUMN_NAME_TRACKING = "tracking";
        public static final String COLUMN_NAME_CARRIER = "carrier";
        public static final String COLUMN_NAME_NUMPACKAGES = "numpackages";
        public static final String COLUMN_NAME_SENDER = "sender";
        public static final String COLUMN_NAME_RECIPIENT = "recipient";
        public static final String COLUMN_NAME_PO_NUM = "po_num";
        public static final String COLUMN_NAME_SIG = "sig";
    }
}