package com.powereng.receiving;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.powereng.receiving.sync.SyncUtils;

public class MainScreenActivity extends Activity{

    Button btnViewProducts;
    Button btnNewProduct;
    Button btnSignature;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // Buttons
        btnViewProducts = (Button) findViewById(R.id.btnViewProducts);
        btnNewProduct = (Button) findViewById(R.id.btnCreateProduct);
        btnSignature = (Button) findViewById(R.id.btnSignature);

        // view packages click event
        btnViewProducts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching All packages Activity
                Intent i = new Intent(getApplicationContext(), LogViewActivity.class);
                startActivity(i);

            }
        });

        // view packages click event
        btnNewProduct.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                SyncUtils.TriggerRefresh();

            }
        });

        btnSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),CaptureSignature.class);
                startActivity(i);
            }
        });

    }
}
