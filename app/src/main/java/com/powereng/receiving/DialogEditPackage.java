package com.powereng.receiving;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

/**
 * Simple confirm dialog fragment.
 *
 */

public class DialogEditPackage extends DialogFragment {



    EditText inputTracking;
    Button btnScan;
    Spinner inputCarrier;
    EditText inputNumpackages;
    EditText inputSender;
    EditText inputRecipient;
    EditText inputPoNum;

    String tracking;
    String carrier;
    String numpackages;
    String sender;
    String recipient;
    String ponum;

    String date;
    Cursor mCursor;

    public DialogEditPackage(Cursor cursor) {
        this.mCursor = cursor;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Holo_Light_Dialog);
        tracking = mCursor.getString(0);
        carrier = mCursor.getString(1);
        numpackages = mCursor.getString(2);
        sender = mCursor.getString(3);
        recipient = mCursor.getString(4);
        ponum = mCursor.getString(5);


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

    //TODO: create new layout file for editing package details.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.edit_package);
        final View v = inflater.inflate(R.layout.dialog_add_package, container,
                false);

        inputTracking = (EditText) v.findViewById(R.id.inputTracking);
        inputCarrier = (Spinner) v.findViewById(R.id.inputCarrier);
        inputNumpackages = (EditText) v.findViewById(R.id.inputNumpackages);
        inputSender = (EditText) v.findViewById(R.id.inputSender);
        inputRecipient = (EditText) v.findViewById(R.id.inputRecipient);
        inputPoNum = (EditText) v.findViewById(R.id.inputPoNum);

        inputTracking.setText(tracking);
        inputNumpackages.setText(numpackages);
        inputSender.setText(sender);
        inputRecipient.setText(recipient);
        inputPoNum.setText(ponum);
        inputCarrier.setSelection(getCarrierNumber(carrier));

        btnScan = (Button) v.findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity()); // where this is activity
                intentIntegrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES); // or QR_CODE_TYPES if you need to scan QR
            }
        });


        v.findViewById(R.id.dialog_no).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getDialog().dismiss();
                    }
                });

        v.findViewById(R.id.dialog_yes).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(inputTracking.getText().toString());
                        list.add(inputCarrier.getSelectedItem().toString());
                        list.add(inputNumpackages.getText().toString());
                        list.add(inputSender.getText().toString());
                        list.add(inputRecipient.getText().toString());
                        list.add(inputPoNum.getText().toString());

                        Bundle params = new Bundle();

//                        params.putString("tracking", tracking);
//                        params.putString("carrier", carrier);
//                        params.putString("numpackages", numpackages);
//                        params.putString("sender", sender);
//                        params.putString("recipient", recipient);
//                        params.putString("ponum", ponum);

                        params.putStringArrayList("values", list);
                        if (!params.isEmpty()) {
                            // Add in background
                            AddLogEntryService.updateEntry(getActivity(), params);
                            getDialog().dismiss();
                        }
                    }
                });



        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            inputTracking.setText(scanResult.toString());
        }
        // else continue with any other code you need in the method
    }
}
