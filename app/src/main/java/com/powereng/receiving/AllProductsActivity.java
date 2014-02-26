package com.powereng.receiving;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllProductsActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> packagesList;

    // url to get all packages list
    private static String url_all_packages = "http://boi40310ll.powereng.com/android_connect/get_log_all.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ENTRIES = "receiving_log";
    private static final String TAG_TRACKING = "tracking";
    private static final String TAG_DATE = "date_received";

    // packages JSONArray
    JSONArray packages = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_products);

        // Hashmap for ListView
        packagesList = new ArrayList<HashMap<String, String>>();

        // Loading packages in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // on selecting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String tracking = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        EditProductActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_TRACKING, tracking);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Loading packages. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All packages from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_packages, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Packages: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // packages found
                    // Getting Array of Products
                    packages = json.getJSONArray(TAG_ENTRIES);

                    // looping through All Products
                    for (int i = 0; i < packages.length(); i++) {
                        JSONObject c = packages.getJSONObject(i);

                        // Storing each json item in variable
                        String tracking = c.getString(TAG_TRACKING);
                        String date = c.getString(TAG_DATE);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_TRACKING, tracking);
                        map.put(TAG_DATE, date);

                        // adding HashList to ArrayList
                        packagesList.add(map);
                    }
                } else {
                    // no packages found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            NewProductActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all packages
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AllProductsActivity.this, packagesList,
                            R.layout.list_item, new String[] {TAG_TRACKING,
                            TAG_DATE},
                            new int[] { R.id.pid, R.id.name });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}