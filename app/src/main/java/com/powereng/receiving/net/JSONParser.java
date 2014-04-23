package com.powereng.receiving.net;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    JSONArray packages = null;

    // url to get all packages list
    private static final String url_all_packages = "http://boi40310ll.powereng.com/get_log_all.php";
    private static final String url_update_item = "http://boi40310ll.powereng.com/update_log_row.php";
    private static final String url_delete_item = "http://boi40310ll.powereng.com/delete_log_row.php";
    private static final String url_create_log_row = "http://boi40310ll.powereng.com/create_log_row.php";
    private static final String url_sync = "http://boi40310ll.powereng.com/android_sync.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ENTRIES = "receiving_log";
    private static final String TAG_TRACKING = "tracking";
    private static final String TAG_DATE = "date_received";
    private static final String TAG_CARRIER = "carrier";
    private static final String TAG_SENDER = "sender";
    private static final String TAG_RECIPIENT = "recipient";
    private static final String TAG_PCS = "numpackages";
    private static final String TAG_PO = "po_num";
    private static final String TAG_SIG = "sig";

    // constructor
    public JSONParser() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod
    public InputStream makeHttpRequest(String url, String method,
                                      List<NameValuePair> params) {

        // Making HTTP request
        try {

            // check for request method
            if (method == "POST") {
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } else if (method == "GET") {
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    public JSONObject parse(InputStream stream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            stream.close();
                        json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }

    //TODO: add steps from "do in background" from each of the four AsyncTask classes.

    public List<Entry> loadAllEntries(JSONObject json) {
        List<Entry> params = new ArrayList<Entry>();
        // getting JSON string from URL
        //JSONObject json = this.makeHttpRequest(url_all_packages, "GET", null);

        // Check your log cat for JSON response
        Log.d("All Packages: ", json.toString());

        try {
            // Checking for SUCCESS TAG
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1) {
                // packages found
                // Getting Array of packages
                packages = json.getJSONArray(TAG_ENTRIES);

                // looping through All Products
                for (int i = 0; i < packages.length(); i++) {

                    JSONObject c = packages.getJSONObject(i);

                    // Storing each json item in variable
                    String date = c.getString(TAG_DATE);
                    String tracking = c.getString(TAG_TRACKING);
                    String carrier = c.getString(TAG_CARRIER);
                    String sender = c.getString(TAG_SENDER);
                    String recipient = c.getString(TAG_RECIPIENT);
                    String pcs = c.getString(TAG_PCS);
                    String ponum = c.getString(TAG_PO);
                    String sig = c.getString(TAG_SIG);

                    Entry entry = new Entry(date, tracking, carrier,
                            sender, recipient, pcs, ponum, sig);

                    params.add(entry);
                }
            } else {
                // no packages found
                // make new toast
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return params;

    }

    public void updateEntries(List<Entry> list) {

    }

    public static class Entry {
        public final String date;
        public final String tracking;
        public final String carrier;
        public final String sender;
        public final String recipient;
        public final String pcs;
        public final String ponum;
        public final String sig;

        Entry(String date, String tracking, String carrier, String sender,
              String recipient, String pcs, String ponum, String sig) {
            this.date = date;
            this.tracking = tracking;
            this.carrier = carrier;
            this.sender = sender;
            this.recipient = recipient;
            this.pcs = pcs;
            this.ponum = ponum;
            this.sig = sig;
        }
    }
}