package com.powereng.receiving;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ShareToActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_share_to);
		final Intent intent = getIntent();
		if (intent != null) {
			handleIntent(intent);
		}
		Toast.makeText(this, R.string.link_added, Toast.LENGTH_SHORT).show();
		finish();
	}

	private void handleIntent(final Intent intent) {
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			//AddLogEntryService.addEntry(this, intent.getStringExtra(Intent.EXTRA_TEXT));
		}
	}

}
