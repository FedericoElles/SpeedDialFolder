package com.speeddialfolder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);

		findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		WebView webView = (WebView) findViewById(R.id.webview);
		webView.loadUrl("file:///android_asset/index.html");

	}
}
