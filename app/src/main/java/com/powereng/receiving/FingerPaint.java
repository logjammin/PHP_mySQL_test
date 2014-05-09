/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powereng.receiving;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileOutputStream;

public class FingerPaint extends Activity{
    Button mClear, mGetSign, mCancel;
    ViewGroup mContent;
    FingerPaintView paintView;
    //Bitmap mBitmap;
    EditText yourName;
    View mView;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signature);
        paintView = (FingerPaintView) findViewById(R.id.myPaintView2);
        yourName = (EditText) findViewById(R.id.yourName);
        mClear = (Button)findViewById(R.id.clear);
        mGetSign = (Button)findViewById(R.id.getsign);
        mCancel = (Button)findViewById(R.id.cancel);

        mView = paintView;
        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Cleared");
                paintView.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Saved");

                mView.setDrawingCacheEnabled(true);
                save(mView);

                // Bundle b = new Bundle();
                //b.putString("status", "done");
                //Intent intent = new Intent();
                //intent.putExtras(b);
                //setResult(RESULT_OK,intent);


            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Canceled");


                //Bundle b = new Bundle();
                //b.putString("status", "cancel");
                //Intent intent = new Intent();
                //intent.putExtras(b);
                //setResult(RESULT_OK,intent);
                finish();
            }
        });

    }

    public void save(View view) {
        //Log.v("log_tag", "Width: " + v.getWidth());
        //Log.v("log_tag", "Height: " + v.getHeight());


        Bitmap mBitmap = getBitmapFromView(view);


        try
        {
            String fileName = "img" + Math.random() + ".png";
            FileOutputStream mFileOutStream;
            mFileOutStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);

            mFileOutStream.flush();
            mFileOutStream.close();

            Bundle bundle = new Bundle();
            bundle.putString("fname", fileName);
            AddLogEntryService.addSignature(this, bundle);

        }
        catch(Exception e)
        {
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


    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int SAVE_MENU_ID = Menu.FIRST + 1;
    private static final int CLEAR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, SAVE_MENU_ID, 0, "Save").setShortcut('4', 's');
        menu.add(0, CLEAR_MENU_ID, 0, "Clear").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

        /****   Is this the mechanism to extend with filter effects?
         Intent intent = new Intent(null, getIntent().getData());
         intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
         menu.addIntentOptions(
         Menu.ALTERNATIVE, 0,
         new ComponentName(this, NotesList.class),
         null, intent, 0, null);
         *****/
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                //new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case SAVE_MENU_ID:
                //mContent = sigView.getView();

                return true;
            case CLEAR_MENU_ID:

                Log.d("FingerPaint", "called " + mContent);
                return true;
            case ERASE_MENU_ID:

                return true;
            case SRCATOP_MENU_ID:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
