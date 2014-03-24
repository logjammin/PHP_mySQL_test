package com.powereng.receiving;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
    ToggleButton btnAdd;
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;

    EditItemFragment editItemFragment;
    NewItemFragment newItemFragment;

    // url to get all packages list
    private static final String url_all_packages = "http://boi40310ll.powereng.com/get_log_all.php";
    private static final String url_update_item = "http://boi40310ll.powereng.com/update_log_row.php";
    private static final String url_delete_item = "http://boi40310ll.powereng.com/delete_log_row.php";
    private static final String url_create_log_row = "http://boi40310ll.powereng.com/create_log_row.php";

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
        btnAdd = (ToggleButton) findViewById(R.id.button_add);
        btnAdd.setOnCheckedChangeListener(addPackage);

        // on selecting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(itemClickListener);
        //lv.setLongClickable(true);
        //lv.setOnItemLongClickListener(itemLongClickListener);

    }

    public AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        private ArrayList<String> itemDetails;
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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


            fragmentTransaction = fragmentManager.beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = new EditItemFragment(itemDetails);
            newFragment.show(fragmentTransaction, "dialog");
        }
    };

    public AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            TextView tv = (TextView) view.findViewById(R.id.tracking);
            String tracking = tv.getText().toString();
            params.add(new BasicNameValuePair("tracking", tracking));

            new DeleteItem(params).execute();
            return false;
        }
    };

    void showDialog() {


        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.

    }

    public CompoundButton.OnCheckedChangeListener addPackage = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            //Fragment container = fragmentManager.findFragmentById(R.id.fragment_container);
            fragmentTransaction = fragmentManager.beginTransaction();
            newItemFragment = new NewItemFragment();

            if (isChecked) {

                fragmentTransaction.add(R.id.fragment_container, newItemFragment);

            } else {
                fragmentTransaction.remove(newItemFragment);
            }
            fragmentTransaction.commit();
        }
    };
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


    public Dialog deleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LogViewActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setMessage(R.string.delete_message).setTitle(R.string.delete_title);

        AlertDialog dialog = builder.create();

        return dialog;
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
    public void OnItemUpdated(DialogFragment dialog) {
        editItemFragment = (EditItemFragment) fragmentManager.findFragmentByTag("dialog");
        List<NameValuePair> params = editItemFragment.getParams();
        new SaveItemDetail(params).execute();
    }

    @Override
    public void OnItemDeleted(DialogFragment dialog) {
        editItemFragment = (EditItemFragment) fragmentManager.findFragmentByTag("dialog");
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
        int success;
        String message;
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
                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

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
            if (message !=null) {
                if (success == 1) {
                    fragmentTransaction = fragmentManager.beginTransaction();
                    NewItemFragment fragment = new NewItemFragment();

                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    //TODO: show new package in log
                }

            } else {
                message = "Error connecting to server";
            }
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
            pDialog.dismiss();
        }

    }

    class SaveItemDetail extends AsyncTask<String, String, String> {

        List<NameValuePair> params;
        public SaveItemDetail(List<NameValuePair> nameValuePairList){
            params = nameValuePairList;
        }
        int success;
        String message;
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
            try {
            JSONObject json = jParser.makeHttpRequest(url_update_item,
                    "POST", params);

            // check json success tag
                Log.d("Update Item", json.toString());

                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

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
            if (message == null) {
                 message = "Error connecting to server";
            }
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
            pDialog.dismiss();
        }
    }

    class DeleteItem extends AsyncTask<String, String, String> {

        List<NameValuePair> params;
        public DeleteItem(List<NameValuePair> nameValuePairList){
                params = nameValuePairList;
            }

        int success;
        String message;
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

            try {
                // getting item details by making HTTP request
                JSONObject json = jParser.makeHttpRequest(
                        url_delete_item, "POST", params);

                // check your log for json response
                Log.d("Delete Item", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

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
            if (message !=null) {
                if (success == 1) {
                    editItemFragment = (EditItemFragment) fragmentManager.findFragmentById(R.id.fragment_container);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.detach(editItemFragment);
                }

            } else {
                message = "Error connecting to server";
            }
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();

            pDialog.dismiss();

        }

    }

}