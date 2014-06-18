package com.powereng.receiving.sync;

import com.powereng.receiving.database.LogEntry;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/**
* Created by qgallup on 5/2/2014.
*/
public interface RetroServer {

    public static final String API_URL = "http://10.102.5.229";

    public static class LogEntries {
        String latestTimestamp;
        List<LogMSG> entries;
    }


    public static class LogMSG {
        String tracking;
        String carrier;
        String numpackages;
        String sender;
        String recipient;
        String ponum;
        String sig;
        String timestamp;
        int sync_status;


        public LogMSG(LogEntry entry) {
            tracking = entry.tracking;
            carrier = entry.carrier;
            numpackages = entry.numpackages;
            sender = entry.sender;
            recipient = entry.recipient;
            ponum = entry.ponum;
            sig = entry.sig;
            timestamp = entry.timestamp;
            sync_status = entry.sync_status;
        }

        public LogEntry toDBItem() {
            final LogEntry entry = new LogEntry();

            entry.tracking = tracking;
            entry.carrier = carrier;
            entry.numpackages = numpackages;
            entry.sender = sender;
            entry.recipient = recipient;
            entry.ponum = ponum;
            entry.sig = sig;
            entry.timestamp = timestamp;
            entry.sync_status = sync_status;
            return entry;
        }
    }


    public static class Dummy {
        // Methods must have return type
    }

    @GET("/receiving/v1/packages")
    LogEntries listEntries(@Header("Authorization") String token,
                           //@Query("showDeleted") String showDeleted,
                           @Query("timestampMin") String timestampMin);

    @GET("/receiving/v1/packages/{tracking}")
    LogMSG getEntry(@Header("Authorization") String token, @Path("tracking") String tracking);

    @PUT("/receiving/v1/packages/{tracking}")
    Response updateEntry(@Header("Authorization") String token, @Path("tracking") String tracking, @Body LogMSG item);

    //update with signature
    @Multipart
    @POST("/receiving/v1/signature")
    Response addSignature(@Header("Authorization") String token, @Part("signature")TypedFile signature);

    @DELETE("/receiving/v1/packages/{tracking}")
    Response deleteEntry(@Header("Authorization") String token, @Path("tracking") String tracking);

    @POST("/receiving/v1/packages")
    Response addEntry(@Header("Authorization") String token, @Body LogMSG item);

}
