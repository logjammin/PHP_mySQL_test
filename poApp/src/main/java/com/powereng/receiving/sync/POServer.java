package com.powereng.receiving.sync;

import com.powereng.receiving.database.POEntry;

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
public interface POServer {

    public static final String API_URL = "http://10.102.5.229";

    public static class POEntries {
        String latestTimestamp;
        List<POMsg> entries;
    }


    public static class POMsg {
        long poID;
        String ponum;
        String projectnum;
        String vendor;
        String description;
        String unitType;
        int total;
        String unitPrice;
        int received;
        int remaining;
        String packslip;
        String validFrom;
        String validUntil;
        int status;


        public POMsg(POEntry entry) {
            poID = entry.poID;
            ponum = entry.ponum;
            projectnum = entry.projectnum;
            vendor = entry.vendor;
            description = entry.description;
            unitType = entry.unitType;
            total = entry.total;
            unitPrice = entry.unitPrice;
            received = entry.received;
            remaining = entry.remaining;
            packslip = entry.packslip;
            validFrom = entry.validFrom;
            validUntil = entry.validUntil;
            status = entry.status;
        }

        public POEntry toDBItem() {
            final POEntry entry = new POEntry();

            entry.poID = poID;
            entry.ponum = ponum;
            entry.projectnum = projectnum;
            entry.vendor = vendor;
            entry.description = description;
            entry.unitType = unitType;
            entry.unitPrice = unitPrice;
            entry.received = received;
            entry.remaining = remaining;
            entry.packslip = packslip;
            entry.validFrom = validFrom;
            entry.validUntil = validUntil;
            entry.status = status;
            return entry;
        }
    }


    public static class Dummy {
        // Methods must have return type
    }

    @GET("/receiving/v1/requisitions")
    POEntries listEntries(@Header("Authorization") String token,
                           //@Query("showDeleted") String showDeleted,
                           @Query("timestampMin") String timestampMin);

    @GET("/receiving/v1/requisitions/{ponum}")
    POMsg getEntry(@Header("Authorization") String token, @Path("ponum") String ponum);

    @PUT("/receiving/v1/requisitions/{ponum}")
    Response getPOEntry(@Header("Authorization") String token, @Path("ponum") String ponum, @Body POMsg item);

    @Multipart
    @POST("/receiving/v1/requisitions/packslip")
    Response addPackingSlip(@Header("Authorization") String token, @Part("packslip") TypedFile packslip);

    @DELETE("/receiving/v1/requisitions/{ponum}")
    Response deletePOEntry(@Header("Authorization") String token, @Path("ponum") String ponum);

    @POST("/receiving/v1/packages")
    Response addPOEntry(@Header("Authorization") String token, @Body POMsg item);

}
