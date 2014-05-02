package com.powereng.receiving.sync;

import com.powereng.receiving.database.LogEntry;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
* Created by qgallup on 5/2/2014.
*/
public interface LogServer {

    /**
     * Change the IP to the address of your server
     */
    // Server-app uses no prefixes in the URL
    public static final String API_URL = "http://10.102.5.229";
    // Server on App Engine will have a Base URL like this
    //public static final String API_URL = "http://192.168.1.17:8080/_ah/api/links/v1";

    public static class LogEntries {
        String latestTimestamp;
        List<LogMSG> entries;
    }

    /**
     * We could have used LogEntry class directly instead.
     * But to make it compatible with both servers, I chose
     * to make this converter class to handle the deleted field.
     * Converting the integer to boolean for the JSON message.
     */
    public static class LogMSG {
        String timestamp;
        String tracking;
        String carrier;
        String numpackages;
        String sender;
        String recipient;
        String ponum;
        String sig;
        boolean deleted;


        public LogMSG(LogEntry entry) {
            tracking = entry.tracking;
            carrier = entry.carrier;
            numpackages = entry.numpackages;
            sender = entry.sender;
            recipient = entry.recipient;
            ponum = entry.ponum;
            sig = entry.sig;
            deleted = (entry.deleted == 1);
        }

        public LogEntry toDBItem() {
            final LogEntry entry = new LogEntry();
            //entry.timestamp = timestamp;
            entry.tracking = tracking;
            entry.carrier = carrier;
            entry.numpackages = numpackages;
            entry.sender = sender;
            entry.recipient = recipient;
            entry.ponum = ponum;
            entry.sig = sig;

            if (deleted) {
                entry.deleted = 1;
            }
            return entry;
        }
    }

    public static class RegId {
        public String regid;
    }

    public static class Dummy {
        // Methods must have return type
    }

    @GET("/receiving/v1/packages")
    LogEntries listEntries(@Header("Authorization") String token);
                           //@Query("showDeleted") String showDeleted,
                           //@Query("timestampMin") String timestampMin);

    @GET("/receiving/v1/packages/{tracking}")
    LogMSG getEntry(@Header("Authorization") String token, @Path("tracking") String tracking);

    @DELETE("/receiving/v1/packages{tracking}")
    Dummy deleteEntry(@Header("Authorization") String token, @Path("tracking") String tracking);

    @POST("/receiving")
    LogMSG addEntry(@Header("Authorization") String token, @Body LogMSG item);

}
