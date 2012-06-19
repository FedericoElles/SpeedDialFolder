package com.speeddialfolder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class TitleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.title);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.pencil);

		Button buttonCreate = (Button) findViewById(R.id.button_create);
		final EditText titleEditBox = (EditText) findViewById(R.id.edit_title);
		if (getIntent() != null && getIntent().getExtras() != null
				&& getIntent().getExtras().containsKey("title")) {
			titleEditBox.setText(getIntent().getStringExtra("title"));
			setTitle(R.string.title_edit_title);
			buttonCreate.setText(R.string.button_edit);
		} else {
			setTitle(R.string.title_create_folder);
			buttonCreate.setText(R.string.button_create);
		}

		buttonCreate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String title = titleEditBox.getText().toString();
				if (title == null || title.length() == 0) {
					titleEditBox.setError(getString(R.string.error_empty_title));

				} else {
					Intent data = new Intent();
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
		findViewById(R.id.edit_title).requestFocus();
	}

}
