package com.powereng.receiving;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogViewActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    static final JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> packagesList;

    FragmentManager fragmentManager;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view_all);
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
                String tracking = ((TextView) view.findViewById(R.id.tracking)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                       PackageDetailActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_TRACKING, tracking);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });
    }

    public void newItem(View view) {
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        NewItemFragment fragment = new NewItemFragment();
        fragmentTransaction.add(R.id.fragment_container,fragment);
        fragmentTransaction.commit();
        //Intent i = new Intent(getApplicationContext(), LogNewActivity.class);
        //startActivity(i);

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


/*    *//**
     * Background Async Task to Get complete product details
     * *//*
    class GetLogDetail extends AsyncTask<String, String, String> {

        *//**
         * Before starting background thread Show Progress Dialog
         * *//*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PackageDetailActivity.this);
            pDialog.setMessage("Loading item detail. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        *//**
         * Getting item detail in background thread
         * *//*
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tracking", tracking));
            // getting item detail by making HTTP request
            // Note that item detail url will use GET request
            JSONObject json = jParser.makeHttpRequest(url_item_detail, "GET", params);

            // check your log for json response
            Log.d("Single Item Detail", json.toString());

            try {
                int success;
                // json success tag
                success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully received item details
                    JSONArray itemObj = json.getJSONArray(TAG_ENTRIES); // JSON Array
                    // get first item object from JSON Array
                    JSONObject c = itemObj.getJSONObject(0);

                    // Storing each json item in variable
                    String date = c.getString(TAG_DATE);
                    String tracking = c.getString(TAG_TRACKING);
                    String carrier = c.getString(TAG_CARRIER);
                    String pcs = c.getString(TAG_PCS);
                    String sender = c.getString(TAG_SENDER);
                    String recipient = c.getString(TAG_RECIPIENT);
                    String ponum = c.getString(TAG_PO);
                    //String sig = c.getString(TAG_SIG);

                    packagesList = new HashMap<String, String>();

                    packagesList.put(TAG_DATE, date);
                    packagesList.put(TAG_TRACKING, tracking);
                    packagesList.put(TAG_CARRIER, carrier);
                    packagesList.put(TAG_SENDER, sender);
                    packagesList.put(TAG_RECIPIENT, recipient);
                    packagesList.put(TAG_PCS, pcs);
                    packagesList.put(TAG_PO, ponum);

                }else{

                    Log.d("Success = ",""+ success);
                }

                // updating UI from Background Thread

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        *//**
         * After completing background task Dismiss the progress dialog
         * **//*
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();

            runOnUiThread(new Runnable() {
                public void run() {
                    // Edit Text
                    inputDate = (TextView) findViewById(R.id.inputDate);
                    inputTracking = (EditText) findViewById(R.id.inputTracking);
                    inputCarrier = (Spinner) findViewById(R.id.inputCarrier);
                    inputPcs = (NumberPicker) findViewById(R.id.numberPicker);
                    inputSender = (EditText) findViewById(R.id.inputSender);
                    inputRecipient = (EditText) findViewById(R.id.inputRecipient);
                    inputPoNum = (EditText) findViewById(R.id.inputPoNum);

                    // display item data in EditText
                    inputDate.setText(packagesList.get(TAG_DATE));
                    inputTracking.setText(packagesList.get(TAG_TRACKING));
                    inputCarrier.setSelection(0);
                    inputPcs.setValue(Integer.parseInt(packagesList.get(TAG_PCS)));
                    inputSender.setText(packagesList.get(TAG_SENDER));
                    inputRecipient.setText(packagesList.get(TAG_RECIPIENT));
                    inputPoNum.setText(packagesList.get(TAG_PO));


                    //TODO: Deal with signature image
                }
            });
        }}
    *//**
     * Background Async Task to  Save product Details
     * *//*
    class SaveItemDetail extends AsyncTask<String, String, String> {

        *//**
         * Before starting background thread Show Progress Dialog
         * *//*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PackageDetailActivity.this);
            pDialog.setMessage("Saving Item ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        *//**
         * Saving product
         * *//*
        protected String doInBackground(String... args) {

            // getting updated data from EditTexts
            String date = inputDate.getText().toString();
            String tracking = inputTracking.getText().toString();
            String carrier = inputCarrier.getSelectedItem().toString();
            String numpackages = String.valueOf(inputPcs.getValue());
            String sender = inputSender.getText().toString();
            String recipient = inputRecipient.getText().toString();
            String ponum = inputPoNum.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_DATE, date));
            params.add(new BasicNameValuePair(TAG_TRACKING, tracking));
            params.add(new BasicNameValuePair(TAG_CARRIER, carrier));
            params.add(new BasicNameValuePair(TAG_PCS, numpackages));
            params.add(new BasicNameValuePair(TAG_SENDER, sender));
            params.add(new BasicNameValuePair(TAG_RECIPIENT, recipient));
            params.add(new BasicNameValuePair(TAG_PO, ponum));

            // sending modified data through http request
            // Notice that update product url accepts POST method
            JSONObject json = jParser.makeHttpRequest(url_update_item,
                    "POST", params);

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about product update
                    setResult(100, i);
                    finish();
                } else {
                    // failed to update product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        *//**
         * After completing background task Dismiss the progress dialog
         * **//*
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once item updated
            pDialog.dismiss();
        }
    }
    *//*****************************************************************
     * Background Async Task to Delete item
     * *//*
    class DeleteItem extends AsyncTask<String, String, String> {

        *//**
         * Before starting background thread Show Progress Dialog
         * *//*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PackageDetailActivity.this);
            pDialog.setMessage("Deleting Item...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        *//**
         * Deleting product
         * *//*
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("tracking", tracking));

                // getting item details by making HTTP request
                JSONObject json = jParser.makeHttpRequest(
                        url_delete_item, "POST", params);

                // check your log for json response
                Log.d("Delete Item", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // item successfully deleted
                    // notify previous activity by sending code 100
                    Intent i = getIntent();
                    // send result code 100 to notify about item deletion
                    setResult(100, i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        *//**
         * After completing background task Dismiss the progress dialog
         * **//*
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once item deleted
            pDialog.dismiss();

        }

    }*/



    public static class LogViewFragment extends ListFragment{

        public LogViewFragment(){}

/*        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
            rootView =
            return rootView;

        }*/

    }

    public class NewItemFragment extends Fragment {

        private ProgressDialog pDialog;
        //OnItemAddedListener mListener;
        TextView inputDate;
        EditText inputTracking;
        Spinner inputCarrier;
        NumberPicker inputPcs;
        EditText inputSender;
        EditText inputRecipient;
        EditText inputPoNum;
        Button btnScan;
        Button btnAdd;


        public NewItemFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState){
            View rootView = inflater.inflate(R.layout.fragment_edit_package, container, false);
            inputDate = (TextView) rootView.findViewById(R.id.inputDate);
            inputTracking = (EditText) rootView.findViewById(R.id.inputTracking);
            inputCarrier = (Spinner) rootView.findViewById(R.id.inputCarrier);

            inputPcs = (NumberPicker) rootView.findViewById(R.id.inputPcs);
            inputPcs.setMinValue(1);
            inputPcs.setMaxValue(20);
            inputPcs.setValue(1);

            inputSender = (EditText) rootView.findViewById(R.id.inputSender);
            inputRecipient = (EditText) rootView.findViewById(R.id.inputRecipient);
            inputPoNum = (EditText) rootView.findViewById(R.id.inputPoNum);
            btnScan = (Button) rootView.findViewById(R.id.btnScan);
            btnAdd = (Button) rootView.findViewById(R.id.btn2);
            btnAdd.setText(R.string.add);
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AddItem().execute();
                }
            });
            btnScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity()); // where this is activity
                    intentIntegrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES); // or QR_CODE_TYPES if you need to scan QR
                }
            });

            return rootView;
        }

         public void onActivityResult(int requestCode, int resultCode, Intent intent) {
               IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
               if (scanResult != null) {
                     inputTracking.setText(scanResult.toString());

                   }
               // else continue with any other code you need in the method


         }

        class AddItem extends AsyncTask<String, String, String> {
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
                String date = inputDate.getText().toString();
                String tracking = inputTracking.getText().toString();
                String carrier = inputCarrier.getSelectedItem().toString();
                String numpackages = String.valueOf(inputPcs.getValue());
                String sender = inputSender.getText().toString();
                String recipient = inputRecipient.getText().toString();
                //String ponum = inputPoNum.getText().toString();

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("date_received", date));
                params.add(new BasicNameValuePair("tracking", tracking));
                params.add(new BasicNameValuePair("carrier", carrier));
                params.add(new BasicNameValuePair("numpackages", numpackages));
                params.add(new BasicNameValuePair("sender", sender));
                params.add(new BasicNameValuePair("recipient", recipient));
                //params.add(new BasicNameValuePair("po_num", ponum));

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
                        //TODO: send this fragment to backstack and create a new instance.
                        Intent i = new Intent(getApplicationContext(), LogViewActivity.class);
                        startActivity(i);

                        // closing this screen
                        finish();
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
                pDialog.dismiss();
            }

        }

/*        public interface OnItemAddedListener {
            public void OnItemAdded(Bundle args);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (OnItemAddedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnItemAddedListener");
            }
        }*/


    }




}