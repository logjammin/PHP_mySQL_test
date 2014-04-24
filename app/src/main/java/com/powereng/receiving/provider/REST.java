package com.powereng.receiving.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.powereng.receiving.NotificationUtil;

/**
 * Created by Logjammin on 4/23/14.
 */
public class REST{

    public class InsertTask implements Callable<boolean>{

        private static final String TAG = "InsertTask";

        private String mTitle;
        private String mArtist;

        public InsertTask( String title, String artist )
        {
            mTitle = title;
            mArtist = artist;
        }

        /**
         * Insert a row into the table with data entered by the user.
         * Set the status to "POST" and transacting flag to "pending".
         */
        @Override
        public Boolean call()
        {
            ContentResolver cr = RestfulApplication.getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            Uri uri;

            values.put( TableConstants.COL_TITLE, mTitle );
            values.put( TableConstants.COL_ARTIST, mArtist );

            uri = cr.insert( RestfulProvider.CONTENT_URI_SONGS_PENDING, values );
            if ( uri == null ) {
                Log.e(TAG, "Error setting insert request to PENDING status.");

                NotificationUtil.errorNotify(MethodEnum.POST);

                return false;
            }

            return true;
        }

    }

    public class UpdateTask implements Callable<boolean>{

        private static final String TAG = "UpdateTask";

        private long mUpdateId;
        private String mTitle;
        private String mArtist;

        public UpdateTask( long updateId, String title, String artist )
        {
            mUpdateId = updateId;
            mTitle = title;
            mArtist = artist;
        }

        /**
         * Update the table with data entered by the user.
         * Set the status to "PUT" and transacting flag to "pending".
         */
        @Override
        public Boolean call()
        {
            ContentResolver cr = RestfulApplication.getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            Uri uri;
            int updateCount;

            uri = ContentUris.withAppendedId(
                    RestfulProvider.CONTENT_URI_SONGS_PENDING,
                    mUpdateId);

            values.put( TableConstants.COL_TITLE, mTitle );
            values.put( TableConstants.COL_ARTIST, mArtist );

            updateCount = cr.update( uri, values, null, null );

            if ( updateCount == 0 ) {
                Log.e( TAG, "Error setting update request to PENDING status." );

                NotificationUtil.errorNotify( MethodEnum.PUT );

                return false;
            }

            return true;
        }

    }

    public class DeleteTask  implements Callable<boolean>{

        private static final String TAG = "DeleteTask";

        private long mDeleteId;

        public DeleteTask( long deleteId )
        {
            mDeleteId = deleteId;
        }

        /**
         * Set the status to "DELETE" and transacting flag to "pending".
         */
        @Override
        public Boolean call()
        {
            ContentResolver cr = RestfulApplication.getAppContext().getContentResolver();
            Uri uri;
            int deleteCount;

            uri = ContentUris.withAppendedId(
                    RestfulProvider.CONTENT_URI_SONGS_PENDING,
                    mDeleteId );

            deleteCount = cr.delete( uri, null, null );

            if ( deleteCount == 0 ) {
                Log.e( TAG, "Error setting delete request to PENDING status." );

                NotificationUtil.errorNotify( MethodEnum.DELETE );

                return false;
            }

            return true;
        }

    }



}
