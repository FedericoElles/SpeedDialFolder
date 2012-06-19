package com.speeddialfolder;

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class WebLinkActivity extends Activity {
	private static final Pattern urlPattern = Pattern
			.compile("^(?#Protocol)(?:(?:ht|f)tp(?:s?)\\:\\/\\/|~\\/|\\/)?(?#Username:Password)(?:\\w+:\\w+@)?(?#Subdomains)(?:(?:[-\\w]+\\.)+(?#TopLevel Domains)(?:com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|museum|travel|edu|cat|int|jobs|pro|tel|asia|[a-z]{2}))(?#Port)(?::[\\d]{1,5})?(?#Directories)(?:(?:(?:\\/(?:[-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?(?#Query)(?:(?:\\?(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?(?:[-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)(?:&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?(?:[-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*(?#Anchor)(?:#(?:[-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?$");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.web_link);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.globe);

		Button buttonCreate = (Button) findViewById(R.id.button_create);
		final EditText urlEditBox = (EditText) findViewById(R.id.edit_url);
		final EditText titleEditBox = (EditText) findViewById(R.id.edit_title);
		if (getIntent() != null && getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey("url")
				&& getIntent().getExtras().containsKey("title")) {
			urlEditBox.setText(getIntent().getStringExtra("url"));
			titleEditBox.setText(getIntent().getStringExtra("title"));
			setTitle(R.string.title_edit_web_link);
			buttonCreate.setText(R.string.button_edit);
		} else {
			setTitle(R.string.title_create_web_link);
			buttonCreate.setText(R.string.button_add);
		}

		buttonCreate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String url = urlEditBox.getText().toString().trim().replace(" ", "");
				String title = titleEditBox.getText().toString().trim();
				if (!urlPattern.matcher(url).matches()) {
					urlEditBox.setError(getString(R.string.error_invalid_url));
				} else if (title == null || title.length() == 0) {
					titleEditBox.setError(getString(R.string.error_empty_title));
				} else {
					Intent data = new Intent();
					data.putExtra("url", url);
					data.putExtra("title", title);
					setResult(1, data);
					finish();
				}
			}
		});

		findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		findViewById(R.id.edit_url).requestFocus();
	}
}
