package com.powereng.receiving;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
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

public class DialogAddPackage extends DialogFragment {

	public DialogAddPackage() {
	}
    EditText inputTracking;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL,
				android.R.style.Theme_Holo_Light_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(R.string.add_package);
		final View v = inflater.inflate(R.layout.dialog_add_package, container, false);
        
		final EditText inputTracking = (EditText) v.findViewById(R.id.inputTracking);
        final Spinner inputCarrier = (Spinner) v.findViewById(R.id.inputCarrier);
        final EditText inputNumpackages = (EditText) v.findViewById(R.id.inputNumpackages);
        final AutoCompleteTextView inputSender = (AutoCompleteTextView) v.findViewById(R.id.inputSender);
        final AutoCompleteTextView inputRecipient = (AutoCompleteTextView) v.findViewById(R.id.inputRecipient);
        final EditText inputPoNum = (EditText) v.findViewById(R.id.inputPoNum);



        final Button btnScan = (Button) v.findViewById(R.id.btnScan);

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
							AddLogEntryService.addEntry(getActivity(), params);
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
