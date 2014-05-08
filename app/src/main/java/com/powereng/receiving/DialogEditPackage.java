package com.powereng.receiving;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
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
import com.powereng.receiving.database.LogEntry;

import java.util.ArrayList;

/**
 * Simple confirm dialog fragment.
 *
 */

public class DialogEditPackage extends DialogFragment {



    EditText inputTracking;
    Button btnScan;
    Button btnSig;
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

    LogEntry entry;

/*    public DialogEditPackage(Cursor cursor) {
        this.mCursor = cursor;
    }*/
    public DialogEditPackage(LogEntry logEntry) {
        //this.itemId = id;
        this.entry = logEntry;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Holo_Light_Dialog);

        //DatabaseHandler db = DatabaseHandler.getInstance(getActivity());
        //LogEntry entry = db.getLogEntry(itemId);

        tracking = entry.tracking;
        carrier = entry.carrier;
        numpackages = entry.numpackages;
        sender = entry.sender;
        recipient = entry.recipient;
        ponum = entry.ponum;

    }

    public static int getCarrierNumber(String s) {
        int carrierNumber;

        //String s = carrier;
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
        final View v = inflater.inflate(R.layout.dialog_edit_package, container,
                false);

        inputTracking = (EditText) v.findViewById(R.id.inputTracking);
        inputCarrier = (Spinner) v.findViewById(R.id.inputCarrier);
        inputNumpackages = (EditText) v.findViewById(R.id.inputNumpackages);
        inputSender = (EditText) v.findViewById(R.id.inputSender);
        inputRecipient = (EditText) v.findViewById(R.id.inputRecipient);
        inputPoNum = (EditText) v.findViewById(R.id.inputPoNum);

        inputNumpackages.setText(numpackages);
        inputTracking.setText(tracking);
        inputCarrier.setSelection(getCarrierNumber(carrier));
        inputSender.setText(sender);
        inputRecipient.setText(recipient);
        inputPoNum.setText(ponum);

        btnSig = (Button) v.findViewById(R.id.btnGetSignature);
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
                        //list.add(entry.getUri());
                        list.add(inputTracking.getText().toString());
                        list.add(inputCarrier.getSelectedItem().toString());
                        list.add(inputNumpackages.getText().toString());
                        list.add(inputSender.getText().toString());
                        list.add(inputRecipient.getText().toString());
                        list.add(inputPoNum.getText().toString());

                        Bundle params = new Bundle();

                        Uri uri = entry.getUri();
                        String entryUri = uri.toString();
                        params.putString("uri", entryUri);
                        params.putStringArrayList("values", list);
                        if (!params.isEmpty()) {
                            // Add in background
                            //TODO: check to see if anything changed. if not, no need to sync.
                            AddLogEntryService.updateEntry(getActivity(), params);
                            getDialog().dismiss();
                        }
                    }
                }
        );

        btnSig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSignature();
            }
        });

        return v;
    }

    public void getSignature() {
        Fragment signatureFragment = new CaptureSignature();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        Fragment sig = getChildFragmentManager().findFragmentByTag("signature");

        if (sig != null) {
            transaction.remove(sig);
            btnSig.setEnabled(true);
        } else {
            transaction.add(R.id.fragment_container, signatureFragment, "signature").commit();
            btnSig.setEnabled(false);
        }



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
