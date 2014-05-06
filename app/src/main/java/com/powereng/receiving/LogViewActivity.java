package com.powereng.receiving;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

//TODO:create PoViewActivity
public class LogViewActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_view);

		getFragmentManager().beginTransaction()
				.add(R.id.mainContent, new LogViewFragment()).commit();

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
