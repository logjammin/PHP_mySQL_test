package com.powereng.receiving;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

public class LogViewActivity extends ListActivity implements NewItemFragment.OnItemAddedListener{

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    static final JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> packagesList;
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    ArrayList<String> itemDetails;
    EditItemFragment editItemFragment;
    NewItemFragment newItemFragment;

    // url to get all packages list
    private static final String url_all_packages = "http://boi40310ll.powereng.com/get_log_all.php";
    private static final String url_item_detail = "http://boi40310ll.powereng.com/get_log_row.php";
    private static final String url_update_item = "http://boi40310ll.powereng.com/update_log_row.php";
    private static final String url_delete_item = "http://boi40310ll.powereng.com/delete_log_row.php";
    private static final String url_create_log_row = "http://boi40310ll.powereng.com/create_log_row.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ENTRIES = "receiving_log";
    private static final String TAG_TRACKING = "tracking";
    private static final String TAG_DATE = "date_received";
    private static final String TAG_CARRIER = "carrier";
    private static final String TAG_SENDER = "sender";
    private static final String TAG_RECIPIENT = "recipient";
    private static final String TAG_PCS = "numpackages";
    private static final String TAG_PO = "po_num";
    private static final String TAG_SIG = "sig";
    // packages JSONArray
    JSONArray packages = null;
    public LogViewActivity(){}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view_all);
        fragmentManager = getFragmentManager();
        // Hashmap for ListView
        packagesList = new ArrayList<HashMap<String, String>>();

        // Loading packages in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // on selecting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem


                itemDetails = new ArrayList<String>();

                itemDetails.add(((TextView) view.findViewById(R.id.tracking)).getText()
                        .toString());
                itemDetails.add(((TextView) view.findViewById(R.id.numpack)).getText()
                        .toString());
                itemDetails.add(((TextView) view.findViewById(R.id.sender)).getText()
                        .toString());
                itemDetails.add(((TextView) view.findViewById(R.id.recipient)).getText()
                        .toString());
                itemDetails.add(((TextView) view.findViewById(R.id.ponum)).getText()
                        .toString());
                itemDetails.add(((TextView) view.findViewById(R.id.carrier)).getText()
                        .toString());

                updateItem();

            }
        });
    }

    public ArrayList<String> getItemDetails() {
        return itemDetails;
    }

    public void updateItem() {

        Fragment container = fragmentManager.findFragmentById(R.id.fragment_container);
        fragmentTransaction = fragmentManager.beginTransaction();
        editItemFragment = new EditItemFragment(itemDetails);
        //Is the fragment already there?
        if (container != null) {
            fragmentTransaction.replace(R.id.fragment_container, editItemFragment);
            //fragmentTransaction.addToBackStack(null);
        } else {
            fragmentTransaction.add(R.id.fragment_container, editItemFragment);
        }

        fragmentTransaction.commit();

    }


    //start NewItemFragment
    public void newItem(View view) {

        Fragment container = fragmentManager.findFragmentById(R.id.fragment_container);

        //Is the fragment already there?
        if (container != null) {
            return;
        } else {
            fragmentTransaction = fragmentManager.beginTransaction();
            newItemFragment = new NewItemFragment();
            fragmentTransaction.add(R.id.fragment_container, newItemFragment);
            fragmentTransaction.commit();
        }

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

    //TODO: update log view to reflect the new data.
    @Override
    public void OnItemAdded() {
        newItemFragment = (NewItemFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        List<NameValuePair> params = newItemFragment.getParams();
        new AddItem(params).execute();
    }

    @Override
    public void OnItemUpdated() {
        editItemFragment = (EditItemFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        List<NameValuePair> params = editItemFragment.getParams();
        new SaveItemDetail(params).execute();
    }

    @Override
    public void OnItemDeleted() {
        editItemFragment = (EditItemFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        List<NameValuePair> params = editItemFragment.getParams();
        new DeleteItem(params).execute();
    }


    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogViewActivity.this);
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

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_DATE, date);
                        map.put(TAG_TRACKING, tracking);
                        map.put(TAG_CARRIER, carrier);
                        map.put(TAG_SENDER, sender);
                        map.put(TAG_RECIPIENT, recipient);
                        map.put(TAG_PCS, pcs);
                        map.put(TAG_PO, ponum);
                        map.put(TAG_SIG, sig);

                        // adding HashList to ArrayList
                        packagesList.add(map);
                    }
                } else {
                    // no packages found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            LogNewActivity.class);
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
                            LogViewActivity.this, packagesList,
                            R.layout.list_item, new String[] {TAG_DATE,TAG_TRACKING,
                            TAG_CARRIER,TAG_SENDER,TAG_RECIPIENT,TAG_PCS,TAG_PO,TAG_SIG},
                            new int[] { R.id.date, R.id.tracking, R.id.carrier, R.id.sender,
                                    R.id.recipient, R.id.numpack, R.id.ponum, R.id.signature});

                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }

    class AddItem extends AsyncTask<String, String, String> {
        List<NameValuePair> params;
        public AddItem(List<NameValuePair> nameValuePairList){
            params = nameValuePairList;
        }

        //TODO: po_num needs to be dealt with on server script before enabling.
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogViewActivity.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jParser.makeHttpRequest(url_create_log_row,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product


                    fragmentTransaction = fragmentManager.beginTransaction();
                    NewItemFragment fragment = new NewItemFragment();

                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    //TODO: show new package in log

                } else {
                    // failed to create product
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
            // dismiss the dialog once done
            /*Context context = getApplicationContext();
                    CharSequence text = "Record inserted successfully!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();*/
            pDialog.dismiss();
        }

    }

    class SaveItemDetail extends AsyncTask<String, String, String> {

        List<NameValuePair> params;
        public SaveItemDetail(List<NameValuePair> nameValuePairList){
            params = nameValuePairList;
        }
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogViewActivity.this);
            pDialog.setMessage("Saving Item ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         * */
        protected String doInBackground(String... args) {



            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jParser.makeHttpRequest(url_update_item,
                    "POST", params);

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated

                    editItemFragment = (EditItemFragment)fragmentManager.findFragmentById(R.id.fragment_container);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(editItemFragment);



                } else {
                    // failed to update product
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
            // dismiss the dialog once item updated
            /*                    Context context = getApplicationContext();
                    CharSequence text = "Record updated successfully!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();*/
            pDialog.dismiss();
        }
    }
    class DeleteItem extends AsyncTask<String, String, String> {

        List<NameValuePair> params;
        public DeleteItem(List<NameValuePair> nameValuePairList){
                params = nameValuePairList;
            }
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogViewActivity.this);
            pDialog.setMessage("Deleting Item...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting product
         * */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            try {


                // getting item details by making HTTP request
                JSONObject json = jParser.makeHttpRequest(
                        url_delete_item, "POST", params);

                // check your log for json response
                Log.d("Delete Item", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {


                    editItemFragment = (EditItemFragment)fragmentManager.findFragmentById(R.id.fragment_container);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(editItemFragment);
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
            // dismiss the dialog once item deleted
            /*                    Context context = getApplicationContext();
                    CharSequence text = "Record deleted successfully!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();*/
            pDialog.dismiss();

        }

    }

}