package com.speeddialfolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ViewSwitcher;

public class IconChangeActivity extends Activity implements AdapterView.OnItemSelectedListener,
		ViewSwitcher.ViewFactory {
	private ImageSwitcher imageSwitcher;
	private int resourceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.icon_change);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);

		imageSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		imageSwitcher.setFactory(this);
		imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		imageSwitcher.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (resourceId != 0) {
					Intent data = new Intent();
					data.putExtra("resource_id", resourceId);
					setResult(RESULT_OK, data);
				}
				finish();
			}
		});

		Gallery g = (Gallery) findViewById(R.id.gallery);
		g.setAdapter(new ImageAdapter(this));
		g.setOnItemSelectedListener(this);
	}

	@Override
	public View makeView() {
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0x00000000);
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		return i;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		resourceId = drawableIds[position];
		imageSwitcher.setImageResource(resourceId);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	public class ImageAdapter extends BaseAdapter {
		private Context context;

		public ImageAdapter(Context c) {
			context = c;
		}

		@Override
		public int getCount() {
			return drawableIds.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return drawableIds[position];
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = null;
			if (convertView == null) {
				i = new ImageView(context);
				i.setAdjustViewBounds(true);
				i.setLayoutParams(new Gallery.LayoutParams(30, 30));
				i.setBackgroundResource(R.drawable.picture_frame);
			} else {
				i = (ImageView) convertView;
			}
			i.setImageResource(drawableIds[position]);
			return i;
		}

	}

	int[] drawableIds = new int[] { R.drawable.icon_android, R.drawable.icon_application,
			R.drawable.icon_archive, R.drawable.icon_attachment, R.drawable.icon_blog_post,
			R.drawable.icon_calculator, R.drawable.icon_calendar_date,
			R.drawable.icon_calendar_empty, R.drawable.icon_cd, R.drawable.icon_chart,
			R.drawable.icon_clock, R.drawable.icon_comments_big, R.drawable.icon_community_users,
			R.drawable.icon_computer, R.drawable.icon_computers, R.drawable.icon_contacts,
			R.drawable.icon_database, R.drawable.icon_favorite, R.drawable.folder,
			R.drawable.icon_folder_full, R.drawable.icon_gallery, R.drawable.icon_help,
			R.drawable.icon_home, R.drawable.icon_image, R.drawable.icon_info,
			R.drawable.icon_lock_disabled, R.drawable.icon_mail, R.drawable.icon_movie_track,
			R.drawable.icon_music, R.drawable.icon_note_edit, R.drawable.icon_phone,
			R.drawable.icon_play, R.drawable.icon_process_big, R.drawable.icon_rss,
			R.drawable.icon_search, R.drawable.icon_settings, R.drawable.icon_she_user,
			R.drawable.icon_she_users, R.drawable.icon_shopping_cart, R.drawable.icon_sound,
			R.drawable.icon_users, R.drawable.icon_warning };

}
