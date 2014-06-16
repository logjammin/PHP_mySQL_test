package com.powereng.receiving;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.FragmentIntentIntegrator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.powereng.receiving.sync.AddLogEntryService;

import java.util.ArrayList;

/**
 * Simple confirm dialog fragment.
 * 
 */

public class AddPackageFragment extends DialogFragment {

	public AddPackageFragment() {}

    EditText inputTracking;
    Spinner inputCarrier;
    EditText inputNumpackages;
    SenderTextView inputSender;
    RecipientTextView inputRecipient;
    EditText inputPoNum;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setStyle(DialogFragment.STYLE_NORMAL,
		//		android.R.style.Theme_Holo_Light_Dialog);
        setHasOptionsMenu(true);

	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.add_package_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ok:
                if (inputTracking.getText() != null) {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(inputTracking.getText().toString());
                    list.add(inputCarrier.getSelectedItem().toString());
                    list.add(inputNumpackages.getText().toString());
                    list.add(inputSender.getText().toString());
                    list.add(inputRecipient.getText().toString());
                    list.add(inputPoNum.getText().toString());

                    Bundle params = new Bundle();
                    params.putStringArrayList("values", list);

                    if (!params.isEmpty()) {
                        // Add in background
                        AddLogEntryService.addEntry(getActivity(), params);
                        //getDialog().dismiss();
                        dismiss();
                        View v = getActivity().findViewById(R.id.header);
                        v.setVisibility(View.VISIBLE);
                    }
                    //TODO: clear this fragment
                    //FragmentManager fm = Manager();

                    //notify user that tracking number can't be empty
                } else Toast.makeText(getActivity(),"Tracking cannot be empty!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_cancel:
                dismiss();
                View v = getActivity().findViewById(R.id.header);
                v.setVisibility(View.VISIBLE);
                break;
        }

        return false;
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//getDialog().setTitle(R.string.add_package);
		final View v = inflater.inflate(R.layout.add_package_newline, container, false);
        
		inputTracking = (EditText) v.findViewById(R.id.tracking);
        inputCarrier = (Spinner) v.findViewById(R.id.carrier);
        inputNumpackages = (EditText) v.findViewById(R.id.numpackages);
        inputSender = (SenderTextView) v.findViewById(R.id.sender);
        inputRecipient = (RecipientTextView) v.findViewById(R.id.recipient);
        inputPoNum = (EditText) v.findViewById(R.id.ponum);



        final Button btnScan = (Button) v.findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentIntentIntegrator intentIntegrator = new FragmentIntentIntegrator(getActivity().getFragmentManager().findFragmentByTag("addPackage")); // where this is activity
                intentIntegrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES); // or QR_CODE_TYPES if you need to scan QR
            }
        });


		/* v.findViewById(R.id.dialog_no).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						//getDialog().dismiss();
                        dismiss();
					}
				});
		v.findViewById(R.id.dialog_yes).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
                        //make sure tracking number is set
                        if (inputTracking.getText() != null) {
                            ArrayList<String> list = new ArrayList<String>();
                            list.add(inputTracking.getText().toString());
                            list.add(inputCarrier.getSelectedItem().toString());
                            list.add(inputNumpackages.getText().toString());
                            list.add(inputSender.getText().toString());
                            list.add(inputRecipient.getText().toString());
                            list.add(inputPoNum.getText().toString());

                            Bundle params = new Bundle();
                            params.putStringArrayList("values", list);

                            if (!params.isEmpty()) {
                                // Add in background
                                AddLogEntryService.addEntry(getActivity(), params);
                                //getDialog().dismiss();
                                dismiss();
                            }
                          //notify user that tracking number can't be empty
                        } else Toast.makeText(getActivity(),"Tracking cannot be empty!", Toast.LENGTH_SHORT).show();
					}
				});*/


        return v;
	}

    public void mScan(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity()); // where this is activity
        intentIntegrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES); // or QR_CODE_TYPES if you need to
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
