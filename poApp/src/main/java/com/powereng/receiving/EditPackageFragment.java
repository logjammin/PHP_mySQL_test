package com.powereng.receiving;

import android.app.DialogFragment;
import android.content.Context;
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

import com.google.zxing.integration.android.FragmentIntentIntegrator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.powereng.receiving.database.POEntry;
import com.powereng.receiving.sync.AddPOEntryService;

import java.util.ArrayList;


public class EditPackageFragment extends DialogFragment {


    Button btnScan;
    Spinner inputCarrier;
    EditText inputTracking;
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
    private POEntry entry;
    Context mContext;


    /*public DialogEditPackage(LogEntry logEntry) {
        //this.itemId = id;
        this.entry = logEntry;
    }*/
    public EditPackageFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Holo_Light_Dialog);
        mContext = getActivity().getApplicationContext();

        tracking = entry.ponum;
        carrier = entry.carrier;
        numpackages = entry.vendor;
        sender = entry.description;
        recipient = entry.unitType;
        ponum = entry.projectnum;
        setRetainInstance(true);

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
        btnScan = (Button) v.findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentIntentIntegrator intentIntegrator = new FragmentIntentIntegrator(getActivity().getFragmentManager().findFragmentByTag("addPackage")); // where this is activity
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
                        params.putBoolean("signed", false);
                        params.putStringArrayList("values", list);

                        AddPOEntryService.updatePOEntry(getActivity(), params);
                        getDialog().dismiss();
                    }
                }
        );
        return v;
    }

    public void setLogEntry(POEntry entry) {
        this.entry = entry;
    }

    public POEntry getEntry() {
        return entry;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            inputTracking.setText(scanResult.getContents().toString());
        }
        // else continue with any other code you need in the method
    }
}
