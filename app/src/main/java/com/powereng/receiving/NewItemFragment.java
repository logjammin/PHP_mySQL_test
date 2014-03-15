package com.powereng.receiving;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Logjammin on 3/15/14.
 */
class NewItemFragment extends Fragment {

    private static final String TAG_SUCCESS = "success";
    OnItemAddedListener mListener;
    private ProgressDialog pDialog;
    //OnItemAddedListener mListener;

    EditText inputTracking;
    Spinner inputCarrier;
    EditText inputPcs;
    EditText inputSender;
    EditText inputRecipient;
    EditText inputPoNum;
    Button btnScan;
    Button btnAdd;
    JSONParser jParser = new  JSONParser();
    private static final String url_create_log_row = "http://boi40310ll.powereng.com/create_log_row.php";

    public NewItemFragment () {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_edit_package, container, false);

        inputTracking = (EditText) rootView.findViewById(R.id.inputTracking);
        inputCarrier = (Spinner) rootView.findViewById(R.id.inputCarrier);
        inputPcs = (EditText) rootView.findViewById(R.id.inputPcs);
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

    //interface for communicating with the activity
    public interface OnItemAddedListener {
        public void OnItemAdded();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemAddedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItemAddedListener");
        }
    }

    class AddItem extends AsyncTask<String, String, String> {
        //TODO: po_num needs to be dealt with on server script before enabling.
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {

            final Calendar c = Calendar.getInstance();
            String date = c.toString();
            String tracking = inputTracking.getText().toString();
            String carrier = inputCarrier.getSelectedItem().toString();
            String numpackages = inputPcs.getText().toString();
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