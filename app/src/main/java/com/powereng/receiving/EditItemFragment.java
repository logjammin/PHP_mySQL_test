package com.powereng.receiving;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.powereng.receiving.net.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Logjammin on 3/15/14.
 */
public class EditItemFragment extends DialogFragment {



    EditText inputTracking;
    Spinner inputCarrier;
    EditText inputPcs;
    EditText inputSender;
    EditText inputRecipient;
    EditText inputPoNum;
    Button btnUpdate;
    Button btnDelete;
    NewItemFragment.OnItemAddedListener mListener;

    HashMap<String, String> packagesList;
    List<NameValuePair> params;
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jParser = new  JSONParser();

    String tracking;
    String numpackages;
    String sender;
    String recipient;
    String ponum;
    String carrier;
    String date;
    ArrayList<String> detailsList;

    public EditItemFragment (ArrayList<String> list) {
       detailsList = list;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(myView());
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setParams(1);
            }
        });
        builder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setParams(0);
            }
        });

        return builder.create();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracking = detailsList.get(0);
        numpackages = detailsList.get(1);
        sender = detailsList.get(2);
        recipient = detailsList.get(3);
        ponum = detailsList.get(4);
        carrier = detailsList.get(5);
    }


    public View myView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_edit_package, null);

        inputTracking = (EditText) rootView.findViewById(R.id.inputTracking);
        inputPcs = (EditText) rootView.findViewById(R.id.inputNumpackages);
        inputSender = (EditText) rootView.findViewById(R.id.inputSender);
        inputRecipient = (EditText) rootView.findViewById(R.id.inputRecipient);
        inputPoNum = (EditText) rootView.findViewById(R.id.inputPoNum);
        inputCarrier = (Spinner) rootView.findViewById(R.id.inputCarrier);

        inputTracking.setText(tracking);
        inputPcs.setText(numpackages);
        inputSender.setText(sender);
        inputRecipient.setText(recipient);
        inputPoNum.setText(ponum);
        inputCarrier.setSelection(getCarrierNumber(carrier));

/*        // save button click event
        btnUpdate.setText(R.string.update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setParams(1);
            }
        });
        btnDelete.setText(R.string.delete);
        // Delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setParams(0);
            }
        });*/
        return rootView;
    }

    public static int getCarrierNumber(String carrier) {
        int carrierNumber = 0;

        String s = carrier;
        if (s.equals("UPS")) {
            carrierNumber = 0;

        } else if (s.equals("FedEx Ground")) {
            carrierNumber = 1;

        } else if (s.equals("FedEx Express")) {
            carrierNumber = 2;

        } else if (s.equals("FleetStreet")) {
            carrierNumber = 3;

        } else if (s.equals("PaperClips A\'Mor")) {
            carrierNumber = 4;

        } else {
            carrierNumber = 5;

        }
        return carrierNumber;
    }

    public void setParams(int i){
        params = new ArrayList<NameValuePair>();
        if (i == 1) {
            //DateFormat c = DateFormat.getDateTimeInstance();
            //TODO: Add timestamp format to all instances of date_received.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            date = sdf.toString();
            carrier = inputCarrier.getSelectedItem().toString();
            tracking = inputTracking.getText().toString();
            numpackages = inputPcs.getText().toString();
            sender = inputSender.getText().toString();
            recipient = inputRecipient.getText().toString();
            //String ponum = inputPoNum.getText().toString();

            // Building Parameters

            params.add(new BasicNameValuePair("date_received", date));
            params.add(new BasicNameValuePair("tracking", tracking));
            params.add(new BasicNameValuePair("carrier", carrier));
            params.add(new BasicNameValuePair("numpackages", numpackages));
            params.add(new BasicNameValuePair("sender", sender));
            params.add(new BasicNameValuePair("recipient", recipient));
            //params.add(new BasicNameValuePair("po_num", ponum));

            mListener.OnItemUpdated(EditItemFragment.this);
        } else {
            String tracking = inputTracking.getText().toString();
            params.add(new BasicNameValuePair("tracking", tracking));
            mListener.OnItemDeleted(EditItemFragment.this);
        }
    }

    public List<NameValuePair> getParams() {
        return params;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NewItemFragment.OnItemAddedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItemAddedListener");
        }
    }




    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            inputTracking.setText(scanResult.toString());

        }
        // else continue with any other code you need in the method
    }



    /*class GetLogDetail extends AsyncTask<String, String, String> {

        *//**
         * Before starting background thread Show Progress Dialog
         * *//*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
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
            ReceivingLog.d("Single Item Detail", json.toString());

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
                    String timestamp = c.getString(TAG_DATE);
                    String tracking = c.getString(TAG_TRACKING);
                    String carrier = c.getString(TAG_CARRIER);
                    String numpackages = c.getString(TAG_PCS);
                    String sender = c.getString(TAG_SENDER);
                    String recipient = c.getString(TAG_RECIPIENT);
                    String ponum = c.getString(TAG_PO);
                    //String sig = c.getString(TAG_SIG);

                    packagesList = new HashMap<String, String>();

                    packagesList.put(TAG_DATE, timestamp);
                    packagesList.put(TAG_TRACKING, tracking);
                    packagesList.put(TAG_CARRIER, carrier);
                    packagesList.put(TAG_SENDER, sender);
                    packagesList.put(TAG_RECIPIENT, recipient);
                    packagesList.put(TAG_PCS, numpackages);
                    packagesList.put(TAG_PO, ponum);

                }else{

                    ReceivingLog.d("Success = ",""+ success);
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

                     // Edit Text
                    //inputDate = (TextView) findViewById(R.id.inputDate);


                    // display item data in EditText
                    inputTracking.setText(packagesList.get(TAG_TRACKING));
                    inputCarrier.setSelection(0);
                    inputPcs.setText(packagesList.get(TAG_PCS));
                    inputSender.setText(packagesList.get(TAG_SENDER));
                    inputRecipient.setText(packagesList.get(TAG_RECIPIENT));
                    inputPoNum.setText(packagesList.get(TAG_PO));


                    //TODO: Deal with signature image


        }}*/
}

