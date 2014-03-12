package com.powereng.receiving;

import android.app.Activity;
import android.app.ProgressDialog;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogNewActivity extends Activity {

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    TextView inputDate;
    EditText inputTracking;
    Spinner inputCarrier;
    NumberPicker inputPcs;
    EditText inputSender;
    EditText inputRecipient;
    EditText inputPoNum;
    Button scanTracking;

    // url to create new product
    private static String url_create_log_row = "http://boi40310ll.powereng.com/create_log_row.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_package);

        inputDate = (TextView) findViewById(R.id.inputDate);
        final Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);

        // set current date into textview
        inputDate.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(yy).append(" ").append("-").append(mm + 1).append("-")
                .append(dd));

        scanTracking = (Button) findViewById(R.id.btnScan);

        //scanTracking.setOnClickListener(mScan);
        // Edit Text
        inputTracking = (EditText) findViewById(R.id.inputTracking);
        inputCarrier = (Spinner) findViewById(R.id.spinCarrier);
        inputPcs = (NumberPicker) findViewById(R.id.numberPicker);
        inputPcs.setMinValue(1);
        inputPcs.setMaxValue(20);
        inputPcs.setValue(1);

        inputSender = (EditText) findViewById(R.id.inputSender);
        inputRecipient = (EditText) findViewById(R.id.inputRecipient);

        Button btnBack = (Button) findViewById(R.id.btn1);
        btnBack.setText(R.string.back);
        Button btnNext = (Button) findViewById(R.id.btn2);
        btnNext.setText(R.string.nextItem);

        // button click event
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new AddLogEntry().execute();
            }
        });
    }

    public void mScan2(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this); // where this is activity
        intentIntegrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES); // or QR_CODE_TYPES if you need to scan QR
    }
    public Button.OnClickListener mScan = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "ALL_CODE_TYPES");
            startActivityForResult(intent, 0);
            }
    };

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
     * Background Async Task to Create new product
     * */
    class AddLogEntry extends AsyncTask<String, String, String> {
        //TODO: po_num needs to be dealt with on server script before enabling.
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LogNewActivity.this);
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
            JSONObject json = jsonParser.makeHttpRequest(url_create_log_row,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), LogNewActivity.class);
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

}