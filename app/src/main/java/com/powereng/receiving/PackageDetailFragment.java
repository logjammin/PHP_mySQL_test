package com.powereng.receiving;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.powereng.receiving.database.LogEntry;
import com.powereng.receiving.sync.AddLogEntryService;

import java.io.FileOutputStream;

/**
 * Created by Logjammin on 5/15/14.
 */
public class PackageDetailFragment extends DialogFragment {


    TextView detailCarrier;
    TextView detailTracking;
    TextView detailNumpackages;
    TextView detailSender;
    TextView detailRecipient;
    TextView detailPoNum;
    FingerPaintView paintView;
    String tracking;
    String carrier;
    String numpackages;
    String sender;
    String recipient;
    String ponum;
    String sigName;
    LinearLayout mSignature;
    private LogEntry entry;
    Context mContext;
    EditText yourName;


    public PackageDetailFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Holo_Light_Dialog);
        mContext = getActivity().getApplicationContext();

        tracking = entry.tracking;
        carrier = entry.carrier;
        numpackages = entry.numpackages;
        sender = entry.sender;
        recipient = entry.recipient;
        ponum = entry.ponum;
        setRetainInstance(true);
    }


    //TODO: create new layout file for editing package details.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.get_signature);
        final View v = inflater.inflate(R.layout.fragment_package_detail, container,
                false);

        detailTracking = (TextView) v.findViewById(R.id.detailTracking);
        detailCarrier = (TextView) v.findViewById(R.id.detailCarrier);
        detailNumpackages = (TextView) v.findViewById(R.id.detailNumpackages);
        detailSender = (TextView) v.findViewById(R.id.detailSender);
        detailRecipient = (TextView) v.findViewById(R.id.detailRecipient);
        detailPoNum = (TextView) v.findViewById(R.id.detailPoNum);
        paintView = (FingerPaintView) v.findViewById(R.id.myPaintView);
        detailNumpackages.setText(numpackages);
        detailTracking.setText(tracking);
        detailCarrier.setText(carrier);
        detailSender.setText(sender);
        detailRecipient.setText(recipient);
        detailPoNum.setText(ponum);
        yourName = (EditText) v.findViewById(R.id.yourName);

        mSignature = (LinearLayout) v.findViewById(R.id.signatureFrame);


        v.findViewById(R.id.dialog_no).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getDialog().dismiss();
                    }
                });

        v.findViewById(R.id.dialog_yes).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        paintView.setDrawingCacheEnabled(true);

                        Bundle params = new Bundle();

                        Uri uri = entry.getUri();
                        String entryUri = uri.toString();
                        params.putString("uri", entryUri);

                        // Add in background
                        
                            sigName += yourName.getText();
                            if (save(paintView, sigName)) {
                                params.putBoolean("signed", true);
                                params.putString("signee", sigName);                            
                        }                        
                        AddLogEntryService.updateEntry(getActivity(), params);
                        getDialog().dismiss();
                    }
                }
        );

        return v;
    }

    public void setLogEntry(LogEntry entry) {
        this.entry = entry;
    }

    public LogEntry getEntry() {
        return entry;
    }

    public Boolean save(View view, String name) {
        //Log.v("log_tag", "Width: " + v.getWidth());
        //Log.v("log_tag", "Height: " + v.getHeight());

        Bitmap mBitmap = getBitmapFromView(view);
        String fileName = tracking + "_" + name + ".png";

        try {

            FileOutputStream mFileOutStream;
            mFileOutStream = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();
            return true;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Failed to save signature!", Toast.LENGTH_SHORT).show();
            Log.v("log_tag", e.toString());
            return false;
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

}
