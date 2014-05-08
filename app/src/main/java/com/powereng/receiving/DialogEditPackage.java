package com.powereng.receiving;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.powereng.receiving.database.LogEntry;

import java.io.FileOutputStream;
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
    FingerPaintView paintView;
    View mView;
    String tracking;
    String carrier;
    String numpackages;
    String sender;
    String recipient;
    String ponum;
    FrameLayout mSignature;
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
        paintView = (FingerPaintView) v.findViewById(R.id.myPaintView);
        inputNumpackages.setText(numpackages);
        inputTracking.setText(tracking);
        inputCarrier.setSelection(getCarrierNumber(carrier));
        inputSender.setText(sender);
        inputRecipient.setText(recipient);
        inputPoNum.setText(ponum);
        mSignature = (FrameLayout) v.findViewById(R.id.signatureFrame);
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
                        mView.setDrawingCacheEnabled(true);
                        save(mView);
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
                //getSignature();
                int vis = mSignature.getVisibility();
                if(vis != 0) {
                    mSignature.setVisibility(View.VISIBLE);
                } else {
                    mSignature.invalidate();
                    mSignature.setVisibility(View.GONE);
                }

            }
        });

        return v;
    }


/*    public void getSignature() {
        Fragment signatureFragment = new CaptureSignature();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment sig = getChildFragmentManager().findFragmentByTag("signature");

        if (sig != null) {
            transaction.remove(sig);
        } else {
            transaction.add(R.id.fragment_container, signatureFragment, "signature").commit();

        }



    }*/
    public void save(View view) {
        //Log.v("log_tag", "Width: " + v.getWidth());
        //Log.v("log_tag", "Height: " + v.getHeight());


        Bitmap mBitmap = getBitmapFromView(view);


        try {
            String fileName = "img" + Math.random() + ".png";
            FileOutputStream mFileOutStream;
            mFileOutStream = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);

            mFileOutStream.flush();
            mFileOutStream.close();
            //String url = MediaStore.Images.Media.insertImage(this.getContentResolver(), mBitmap, "title", null);
            //Log.v("log_tag","url: " + url);
            //In case you want to delete the file
            //boolean deleted = mypath.delete();
            //ReceivingLog.v("log_tag","deleted: " + mypath.toString() + deleted);
            //If you want to convert the image to string use base64 converter

        }
        catch(Exception e) {
            Log.v("log_tag", e.toString());
        }
    }

    public static Bitmap getBitmapFromView(View view) {

        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(),Bitmap.Config.RGB_565);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
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
