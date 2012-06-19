package com.speeddialfolder;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;

public class SettingsWrapperActivity extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings_wrapper);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("Settings").setIndicator("settings").setContent(
				new Intent(this, SettingsActivity.class)));

	}

	public void onHomeClick(View view) {
		finish();
	}
}
