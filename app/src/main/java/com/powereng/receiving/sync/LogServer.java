package com.powereng.receiving.sync;

import com.powereng.receiving.database.LogEntry;

import java.util.List;
import java.util.Map;

import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
* Created by qgallup on 5/2/2014.
*/
public interface LogServer {

    public static final String API_URL = "http://10.102.5.229";

    public static class LogEntries {
        //String latestTimestamp;
        List<LogMSG> entries;
    }


    public static class LogMSG {
        String timestamp;
        String tracking;
        String carrier;
        String numpackages;
        String sender;
        String recipient;
        String ponum;
        String sig;
        //boolean deleted;


        public LogMSG(LogEntry entry) {
            tracking = entry.tracking;
            carrier = entry.carrier;
            numpackages = entry.numpackages;
            sender = entry.sender;
            recipient = entry.recipient;
            ponum = entry.ponum;
            sig = entry.sig;
            //deleted = (entry.deleted == 1);
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

            /*if (deleted) {
                entry.deleted = 1;
            }*/
            return entry;
        }
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


    @DELETE("/receiving/v1/packages/{tracking}")
    Dummy deleteEntry(@Header("Authorization") String token, @Path("tracking") String tracking);


    @POST("/receiving/v1/packages")
    Response addEntry(@Header("Authorization") String token, @QueryMap Map params);

    /*    @POST("/receiving/v1/packages")
    LogMSG addEntry(@Header("Authorization") String token, @Body LogMSG item);*/

}
