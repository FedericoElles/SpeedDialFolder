package com.speeddialfolder;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ViewSwitcher;

public class BrowseGalleryActivity extends Activity implements AdapterView.OnItemSelectedListener,
	ViewSwitcher.ViewFactory {

	private static final String TAG = BrowseGalleryActivity.class.getSimpleName();
	private ImageSwitcher imageSwitcher;
	private int mPosition;
	private Cursor mCursor;
	private int mIDFieldIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.icon_change);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);

		String[] projection = { MediaStore.Images.Thumbnails._ID };
		mCursor = managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null,
			null, MediaStore.Images.Thumbnails.IMAGE_ID);
		mIDFieldIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

		imageSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		imageSwitcher.setFactory(this);
		imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		imageSwitcher.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] projection = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, null, null, null);
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToPosition(mPosition);
				String path = cursor.getString(columnIndex);
				if (path != null) {
					Intent data = new Intent();
					data.putExtra("path", path);
					setResult(RESULT_OK, data);
				}
				cursor.close();
				finish();
			}
		});

		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(new ExternalImagesAdapter());
		gallery.setOnItemSelectedListener(this);
	}

	@Override
	public View makeView() {
		ImageView imageView = new ImageView(this);
		imageView.setBackgroundColor(0x00000000);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT));
		return imageView;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mCursor.moveToPosition(position);
		int thumbId = mCursor.getInt(mIDFieldIndex);
		Uri uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, Integer
			.toString(thumbId));
		imageSwitcher.setImageURI(uri);
		mPosition = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private class ExternalImagesAdapter extends BaseAdapter {

		public ExternalImagesAdapter() {
		}

		@Override
		public int getCount() {
			return mCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getInt(mIDFieldIndex);
		}

		@Override
		public long getItemId(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getInt(mIDFieldIndex);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = null;
			if (convertView == null) {
				imageView = new ImageView(getBaseContext());
				imageView.setAdjustViewBounds(true);
				imageView.setLayoutParams(new Gallery.LayoutParams(30, 30));
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setBackgroundResource(R.drawable.picture_frame);
			} else {
				imageView = (ImageView) convertView;
			}
			mCursor.moveToPosition(position);
			int id = mCursor.getInt(mIDFieldIndex);
			imageView.setImageURI(Uri.withAppendedPath(
				MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, Integer.toString(id)));
			return imageView;
		}

	}

}
