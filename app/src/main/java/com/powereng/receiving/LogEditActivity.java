package com.powereng.receiving;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogEditActivity extends Activity {

    TextView inputDate;
    EditText inputTracking;
    Spinner inputCarrier;
    NumberPicker inputPcs;
    EditText inputSender;
    EditText inputRecipient;
    EditText inputPoNum;
    Button btnSave;
    Button btnDelete;   
    Button scanTracking;

    String tracking;
    ArrayList<HashMap<String, String>> packagesList;
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jParser = new  JSONParser();

    // single product url
    private static final String url_item_detail = "http://boi40310ll.powereng.com/get_log_row.php";

    // url to update product
    private static final String url_update_item = "http://boi40310ll.powereng.com/update_log_row.php";

    // url to delete product
    private static final String url_delete_item = "http://boi40310ll.powereng.com/delete_log_row.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ENTRIES = "receiving_log";
    private static final String TAG_DATE = "date_received";
    private static final String TAG_TRACKING = "tracking";
    private static final String TAG_CARRIER = "carrier";
    private static final String TAG_PCS = "numpackages";
    private static final String TAG_SENDER = "sender";
    private static final String TAG_RECIPIENT = "recipient";
    private static final String TAG_PO = "po_num";
    //private static final String TAG_SIG = "sig";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_edit);

        // save button
        btnSave = (Button) findViewById(R.id.btn1);
        btnDelete = (Button) findViewById(R.id.btn2);
        scanTracking = (Button) findViewById(R.id.btnScan);
        // getting product details from intent
        Intent i = getIntent();

        // getting product id (tracking) from intent
        tracking = i.getStringExtra(TAG_TRACKING);

        // Getting complete product details in background thread
        new GetLogDetail().execute();

        // save button click event
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                new SaveItemDetail().execute();
            }
        });

        // Delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // deleting product in background thread
                deleteDialog();
            }
        });

    }

    public Dialog deleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteItem().execute();
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

    public void mScan2(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this); // where this is activity
        intentIntegrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES); // or QR_CODE_TYPES if you need to scan QR
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                inputTracking.setText(contents);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }


    /**
     * Background Async Task to Get complete product details
     * */
    class GetLogDetail extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogEditActivity.this);
            pDialog.setMessage("Loading item detail. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting item detail in background thread
         * */
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
                    // Edit Text
                    inputDate = (TextView) findViewById(R.id.inputDate);
                    inputTracking = (EditText) findViewById(R.id.inputTracking);
                    inputCarrier = (Spinner) findViewById(R.id.spinCarrier);
                    inputPcs = (NumberPicker) findViewById(R.id.numberPicker);
                    inputSender = (EditText) findViewById(R.id.inputSender);
                    inputRecipient = (EditText) findViewById(R.id.inputRecipient);
                    inputPoNum = (EditText) findViewById(R.id.inputPoNum);

                    // Storing each json item in variable
                    String date = c.getString(TAG_DATE);
                    String tracking = c.getString(TAG_TRACKING);
                    String carrier = c.getString(TAG_CARRIER);
                    String pcs = c.getString(TAG_PCS);
                    String sender = c.getString(TAG_SENDER);
                    String recipient = c.getString(TAG_RECIPIENT);
                    String ponum = c.getString(TAG_PO);
                    //String sig = c.getString(TAG_SIG);

                    // display item data in EditText
                    inputDate.setText(date);
                    inputTracking.setText(tracking);
                    inputCarrier.setSelection(0);
                    inputPcs.setValue(Integer.parseInt(String.valueOf(pcs)));
                    inputSender.setText(sender);
                    inputRecipient.setText(recipient);
                    inputPoNum.setText(ponum);




                }else{
                    // item with tracking not found
                }

            // updating UI from Background Thread

           } catch (JSONException e) {
               e.printStackTrace();
           }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();

/*            runOnUiThread(new Runnable() {
                public void run() {




                    //TODO: Deal with signature image
                }
            });*/
    }}

    /**
     * Background Async Task to  Save product Details
     * */
    class SaveItemDetail extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogEditActivity.this);
            pDialog.setMessage("Saving Item ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving product
         * */
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

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once item updated
            pDialog.dismiss();
        }
    }

    /*****************************************************************
     * Background Async Task to Delete item
     * */
    class DeleteItem extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogEditActivity.this);
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

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once item deleted
            pDialog.dismiss();

        }

    }
}