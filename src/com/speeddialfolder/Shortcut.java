package com.speeddialfolder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Shortcut {
	public static final int TYPE_APPLICATION = 2;
	public static final int TYPE_FOLDER = 1;
	public static final int TYPE_OTHER = 4;
	public static final int TYPE_WEB = 3;

	private static final String TAG = Shortcut.class.getCanonicalName();

	public static Shortcut createForApplication(Context context, Intent intent, long parentId,
			int position) {
		ComponentName component = intent.getComponent();
		PackageManager packageManager = context.getPackageManager();
		ActivityInfo activityInfo = null;

		try {
			activityInfo = packageManager.getActivityInfo(component, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Couldn't find ActivityInfo for selected application", e);
		}
		if (activityInfo != null) {
			String title = (String) activityInfo.loadLabel(packageManager);
			if (title == null) {
				title = activityInfo.name;
			}
			String uri = intent.toUri(Intent.URI_INTENT_SCHEME);
			Log.i(TAG, "Uri for intent: " + uri);
			Shortcut result = new Shortcut(-1, parentId, Shortcut.TYPE_APPLICATION, position,
					title, uri);
			result.setIntent(intent);
			result.setIcon(activityInfo.loadIcon(packageManager));
			return result;
		}
		return null;
	}

	private Drawable icon;
	private int iconResId;
	private String iconResName;
	private long id;
	private Intent intent;
	private long parentId;
	private int position;
	private String title;
	private int type;

	private String uri;

	public Shortcut(long id, long parentId, int type, int position, String title, String uri) {
		this.id = id;
		this.parentId = parentId;
		this.type = type;
		this.position = position;
		this.title = title;
		this.uri = uri;
	}

	public Drawable getIcon() {
		return icon;
	}

	public int getIconResId() {
		return iconResId;
	}

	public String getIconResName() {
		return iconResName;
	}

	public long getId() {
		return id;
	}

	public Intent getIntent() {
		return intent;
	}

	public long getParentId() {
		return parentId;
	}

	public int getPosition() {
		return position;
	}

	public String getTitle() {
		return title;
	}

	public int getType() {
		return type;
	}

	public String getTypeStr() {
		switch (type) {
		case TYPE_APPLICATION:
			return "Application";
		case TYPE_FOLDER:
			return "Folder";
		case TYPE_OTHER:
			return "Other Shortcut";
		case TYPE_WEB:
			return "Web Link";
		}
		return null;
	}

	public String getUri() {
		return uri;
	}

	void setIconResId(int iconResId) {
		this.iconResId = iconResId;
	}

	void setIconResName(String iconResName) {
		this.iconResName = iconResName;
	}

	void setPosition(int position) {
		this.position = position;
	}

	void setTitle(String title) {
		this.title = title;
	}

	void setUri(String uri) {
		this.uri = uri;
	}

	void setIcon(Drawable drawable) {
		this.icon = drawable;
	}

	void setId(long id) {
		this.id = id;
	}

	void setIntent(Intent intent) {
		this.intent = intent;
	}

	void setParentId(long parentId) {
		this.parentId = parentId;
	}

}
