package com.speeddialfolder;

import java.io.IOException;

import com.speeddialfolder.utils.ImageUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class BackgroundImageActivity extends Activity {
	private static final int REQUEST_CODE_SELECT_IMAGE = 0;
	private static final String TAG = BackgroundImageActivity.class.getSimpleName();
	private String imageUri;
	private Bitmap bgBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.background_image);

		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
		imageUri = config.getString("background_image_uri", null);
		if (imageUri != null) {
			Uri uri = Uri.parse(imageUri);
			setBackground(uri);
		}

		findViewById(R.id.button_change).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Display display = getWindowManager().getDefaultDisplay();
				int size = Math.min(display.getWidth(), display.getHeight());
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				intent.putExtra("outputX", size);
				intent.putExtra("outputY", size);
				intent.putExtra("scale", true);
				startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
			}
		});
		findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = config.edit();
		editor.putString("background_image_uri", imageUri);
		editor.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_CODE_SELECT_IMAGE == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();
				if (setBackground(uri)) {
					imageUri = uri.toString();
				}
			}
		}
	}

	private boolean setBackground(Uri uri) {
		if (getBgBitmap() != null && !getBgBitmap().isRecycled()) {
			getBgBitmap().recycle();
		}
		try {
			setBgBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
			if (getBgBitmap() != null) {
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(ImageUtils.cropImage(
						getBgBitmap(), metrics.widthPixels, metrics.heightPixels));
				bitmapDrawable.setTileModeXY(TileMode.CLAMP, TileMode.CLAMP);
				findViewById(R.id.main).setBackgroundDrawable(bitmapDrawable);
				return true;
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (OutOfMemoryError e) {
			if (getBgBitmap() != null) {
				getBgBitmap().recycle();
			}
			Toast.makeText(this, R.string.error_set_background, Toast.LENGTH_LONG).show();
		}
		return false;
	}

	public void setBgBitmap(Bitmap bgBitmap) {
		this.bgBitmap = bgBitmap;
	}

	public Bitmap getBgBitmap() {
		return bgBitmap;
	}
}
