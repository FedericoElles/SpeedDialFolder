package com.speeddialfolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.DisplayMetrics;
import android.util.Log;

import com.speeddialfolder.utils.HttpError;
import com.speeddialfolder.utils.HttpUtils;
import com.speeddialfolder.utils.ImageUtils;

public class ShortcutManager extends SQLiteOpenHelper implements BaseColumns {

	private static final String DB_NAME = "db3x3links";
	private static final int DB_VERSION = 19;
	private static final String TABLE_LINKS = "shortcuts";
	private static final String TABLE_ICONS = "icons";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_URI = "uri";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_PARENT = "parent";
	private static final String COLUMN_POSITION = "position";
	private static final String COLUMN_ICON_RES_ID = "icon_res_id";
	private static final String COLUMN_ICON_RES_NAME = "icon_res_name";
	private static final String COLUMN_SHORTCUT_ID = "shortcut_id";
	private static final String COLUMN_BITMAP = "bitmap";
	private static final String TAG = ShortcutManager.class.getSimpleName();

	public interface EventListener {
		void onIconLoad();
	}

	private Context context;
	private IconManager iconManager;
	private EventListener eventListener;
	private Bitmap underlayBitmap;
	private DisplayMetrics metrics;

	public ShortcutManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
		iconManager = new IconManager(context.getResources());
		underlayBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.window);
		metrics = context.getResources().getDisplayMetrics();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTableLinks(db);
		createTableIcons(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= 15 && oldVersion <= 19) {
			// TODO: migration code here
		} else if (oldVersion < 15) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ICONS);
			createTableLinks(db);
			createTableIcons(db);
		} else {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ICONS);
			createTableIcons(db);
		}
	}

	private void createTableLinks(SQLiteDatabase db) {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE ").append(TABLE_LINKS).append(" (");
		sb.append(_ID).append(" INTEGER PRIMARY KEY, ");
		sb.append(COLUMN_PARENT).append(" INTEGER DEFAULT '0', ");
		sb.append(COLUMN_POSITION).append(" INTEGER, ");
		sb.append(COLUMN_TITLE).append(" VARCHAR(255), ");
		sb.append(COLUMN_URI).append(" VARCHAR(255), ");
		sb.append(COLUMN_TYPE).append(" INTEGER, ");
		sb.append(COLUMN_ICON_RES_NAME).append(" VARCHAR(255), ");
		sb.append(COLUMN_ICON_RES_ID).append(" INTEGER DEFAULT '0' ");
		sb.append(" ); ");
		db.execSQL(sb.toString());

		db.execSQL("INSERT INTO shortcuts(parent, position, type, title, uri) " + " VALUES(0, 0, 3, 'Google', 'http://google.com/')");
		db.execSQL("INSERT INTO shortcuts(parent, position, type, title, uri) " + " VALUES(0, 1, 3, 'Twitter', 'http://twitter.com/')");
		db.execSQL("INSERT INTO shortcuts(parent, position, type, title, uri) " + " VALUES(0, 2, 3, 'Facebook', 'http://facebook.com/')");
		db.execSQL("INSERT INTO shortcuts(parent, position, type, title, uri) " + " VALUES(0, 3, 1, 'Tools', '')");
		db.execSQL("INSERT INTO shortcuts(parent, position, type, title, uri) " + " VALUES(0, 5, 1, 'Games', '')");
	}

	private void createTableIcons(SQLiteDatabase db) {
		StringBuffer sb = new StringBuffer();
		sb.setLength(0);
		sb.append("CREATE TABLE ").append(TABLE_ICONS).append(" (");
		sb.append(_ID).append(" INTEGER PRIMARY KEY, ");
		sb.append(COLUMN_SHORTCUT_ID).append(" INTEGER, ");
		sb.append(COLUMN_BITMAP).append(" BLOB ");
		sb.append(" ); ");
		db.execSQL(sb.toString());
	}

	void setEventListener(EventListener listener) {
		eventListener = listener;
	}

	public void save(Shortcut shortcut) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		if (shortcut.getId() > 0) {
			values.put(_ID, shortcut.getId());
		}
		values.put(COLUMN_PARENT, shortcut.getParentId());
		values.put(COLUMN_TYPE, shortcut.getType());
		values.put(COLUMN_POSITION, shortcut.getPosition());
		values.put(COLUMN_TITLE, shortcut.getTitle());
		values.put(COLUMN_URI, shortcut.getUri());
		values.put(COLUMN_ICON_RES_ID, shortcut.getIconResId());
		values.put(COLUMN_ICON_RES_NAME, shortcut.getIconResName());
		String id = String.valueOf(shortcut.getId());
		if (shortcut.getId() > 0) {
			if (db.update(TABLE_LINKS, values, _ID + "=?", new String[] { id }) == 0) {
				long new_id = db.insert(TABLE_LINKS, COLUMN_POSITION, values);
				shortcut.setId(new_id);
			}
		} else {
			long new_id = db.insert(TABLE_LINKS, COLUMN_POSITION, values);
			shortcut.setId(new_id);
		}
	}

	public List<Shortcut> load(long parentId) {
		List<Shortcut> result = new ArrayList<Shortcut>();
		for (int i = 0; i < 9; i++) {
			result.add(null);
		}
		SQLiteDatabase db = getReadableDatabase();
		String pid = String.valueOf(parentId);
		Cursor cursor = db.query(TABLE_LINKS, new String[] { _ID, COLUMN_PARENT, COLUMN_TYPE, COLUMN_POSITION, COLUMN_TITLE, COLUMN_URI, COLUMN_ICON_RES_ID,
			COLUMN_ICON_RES_NAME }, COLUMN_PARENT + " = ?", new String[] { pid }, null, null, " position ASC ", " 9 ");
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				Shortcut shortcut = createShortcut(cursor);
				result.set(shortcut.getPosition(), shortcut);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	public List<Shortcut> load(long[] ids) {
		List<Shortcut> result = new ArrayList<Shortcut>();
		Shortcut shortcut;
		for (long id : ids) {
			shortcut = loadById(id);
			if (shortcut != null) {
				result.add(shortcut);
			}
		}
		return result;
	}

	private Shortcut loadById(long id) {
		Shortcut result = null;
		SQLiteDatabase db = getReadableDatabase();
		String pid = String.valueOf(id);
		Cursor cursor = db.query(TABLE_LINKS, new String[] { _ID, COLUMN_PARENT, COLUMN_TYPE, COLUMN_POSITION, COLUMN_TITLE, COLUMN_URI, COLUMN_ICON_RES_ID,
			COLUMN_ICON_RES_NAME }, _ID + " = ?", new String[] { pid }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			result = createShortcut(cursor);
		}
		cursor.close();
		return result;
	}

	private Shortcut createShortcut(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
		long parent_id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARENT));
		int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
		int position = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POSITION));
		String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
		String uri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URI));
		Shortcut shortcut = new Shortcut(id, parent_id, type, position, title, uri);
		shortcut.setIconResId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ICON_RES_ID)));
		shortcut.setIconResName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON_RES_NAME)));
		setIntent(shortcut);
		setIcon(shortcut);
		return shortcut;
	}

	void setIcon(Shortcut shortcut) {
		if (shortcut.getIconResId() < 0) {
			shortcut.setIcon(context.getResources().getDrawable(-shortcut.getIconResId()));
			return;
		}
		ComponentName component = shortcut.getIntent().getComponent();
		switch (shortcut.getType()) {
		case Shortcut.TYPE_WEB:
			// iconManager.setIcon(shortcut);
			shortcut.setIcon(context.getResources().getDrawable(R.drawable.default_icon));
			new LoadIconTask().execute(shortcut);
			break;
		case Shortcut.TYPE_APPLICATION:
			shortcut.setIcon(getAppIcon(shortcut, component));
			break;
		case Shortcut.TYPE_FOLDER:
			iconManager.setIcon(shortcut);
			break;
		case Shortcut.TYPE_OTHER:
			Log.i(TAG, "other type url: " + shortcut.getUri());
			iconManager.setIcon(shortcut);
			break;
		}
	}

	private Drawable getCustomIcon(Shortcut shortcut) {
		Bitmap bitmap = loadIcon(shortcut.getId());
		if (bitmap != null) {
			return new BitmapDrawable(bitmap);
		}
		return null;
	}

	private Drawable getAppIcon(Shortcut s, ComponentName component) {
		Bitmap bitmap = loadIcon(s.getId());
		if (bitmap != null) {
			return new BitmapDrawable(bitmap);
		}
		PackageManager packageManager = context.getPackageManager();
		ActivityInfo activityInfo = null;

		try {
			activityInfo = packageManager.getActivityInfo(component, 0);
			Drawable iconDrawable = activityInfo.loadIcon(packageManager);
			if (iconDrawable instanceof BitmapDrawable) {
				bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
				saveIcon(s.getId(), bitmap);
			}
			return iconDrawable;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Couldn't find ActivityInfo for selected application", e);
			return context.getResources().getDrawable(R.drawable.icon_x_removed);
		}
	}

	private Drawable getWebIcon(Shortcut s) {
		Bitmap bitmap = loadIcon(s.getId());
		if (bitmap != null) {
			return new BitmapDrawable(bitmap);
		}
		try {
			URI uri = new URI(s.getUri());
			String host = uri.getHost();
			int size = getDip(42);
			String imgUrl = String.format("http://%s/favicon.ico", host);
			Log.i(TAG, "~~ fetching image " + imgUrl);
			byte[] data = HttpUtils.get(imgUrl);
			if (data != null) {
				float scale = 2.0f * metrics.scaledDensity;
				bitmap = ImageUtils.overlay(BitmapFactory.decodeByteArray(data, 0, data.length), underlayBitmap, scale);
				saveIcon(s.getId(), bitmap);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
				return bitmapDrawable;
			}
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (HttpError.E404 e) {
			Log.e(TAG, "HTTP Error " + e.getErrorCode());
		} catch (HttpError e) {
			Log.e(TAG, "HTTP Error " + e.getErrorCode());
		}
		return null;
	}

	void setIntent(Shortcut shortcut) {
		Intent intent = null;
		switch (shortcut.getType()) {
		case Shortcut.TYPE_WEB:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(shortcut.getUri()));
			shortcut.setIntent(intent);
			break;
		case Shortcut.TYPE_APPLICATION:
			try {
				intent = Intent.parseUri(shortcut.getUri(), Intent.URI_INTENT_SCHEME);
				shortcut.setIntent(intent);
			} catch (URISyntaxException e) {
				Log.e(TAG, e.getMessage());
			}
			break;
		case Shortcut.TYPE_FOLDER:
			intent = new Intent(context, MainActivity.class);
			intent.setFlags(0x00010000); // FLAG_ACTIVITY_NO_ANIMATION
			// intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
			// | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			intent.putExtra("parent_id", shortcut.getId());
			shortcut.setIntent(intent);
			break;
		case Shortcut.TYPE_OTHER:
			Log.i(TAG, "other shortcut uri " + shortcut.getUri());
			try {
				intent = Intent.parseUri(shortcut.getUri(), Intent.URI_INTENT_SCHEME);
				shortcut.setIntent(intent);
			} catch (URISyntaxException e) {
				Log.e(TAG, e.getMessage());
			}
			break;
		}
	}

	public List<Shortcut> getPath(long parent_id) {
		if (parent_id > 0) {
			List<Shortcut> path = new ArrayList<Shortcut>();
			getPath(parent_id, path);
			Collections.reverse(path);
			return path;
		} else {
			return Collections.emptyList();
		}
	}

	private void getPath(long id, List<Shortcut> path) {
		SQLiteDatabase db = getReadableDatabase();
		String pid = String.valueOf(id);
		Cursor cursor = db.query(TABLE_LINKS, new String[] { _ID, COLUMN_PARENT, COLUMN_TYPE, COLUMN_POSITION, COLUMN_TITLE, COLUMN_URI, COLUMN_ICON_RES_ID,
			COLUMN_ICON_RES_NAME }, _ID + " = ?", new String[] { pid }, null, null, null, " 1 ");
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			Shortcut shortcut = createShortcut(cursor);
			path.add(shortcut);
			if (shortcut.getParentId() > 0) {
				getPath(shortcut.getParentId(), path);
			}
		}
		cursor.close();
	}

	public void delete(Shortcut shortcut) {
		Log.i(TAG, "deleting shortcut id: " + shortcut.getId());
		SQLiteDatabase db = getWritableDatabase();
		List<Shortcut> childs = load(shortcut.getId());
		for (Shortcut child : childs) {
			if (child != null)
				delete(child);
		}
		deleteIcon(db, shortcut.getId());
		String id = String.valueOf(shortcut.getId());
		db.delete(TABLE_LINKS, _ID + " = ?", new String[] { id });
	}

	private long saveIcon(long id, Bitmap bitmap) {
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, bais);
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_SHORTCUT_ID, id);
		values.put(COLUMN_BITMAP, bais.toByteArray());
		return db.insert(TABLE_ICONS, COLUMN_SHORTCUT_ID, values);
	}

	private Bitmap loadIcon(long id) {
		Bitmap result = null;
		SQLiteDatabase db = getReadableDatabase();
		String pid = String.valueOf(id);
		Cursor cursor = db.query(TABLE_ICONS, new String[] { _ID, COLUMN_BITMAP }, COLUMN_SHORTCUT_ID + " = ?", new String[] { pid }, null, null, null, " 1 ");
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_BITMAP));
			if (bytes != null) {
				result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
		}
		cursor.close();
		return result;
	}

	private void deleteIcon(SQLiteDatabase db, long id) {
		db.delete(TABLE_ICONS, COLUMN_SHORTCUT_ID + " = ?", new String[] { String.valueOf(id) });
	}

	private int getDip(int value) {
		return (int) (value * context.getResources().getDisplayMetrics().density);
	}

	class LoadIconTask extends AsyncTask<Shortcut, Void, Drawable> {
		private Shortcut s;

		@Override
		protected Drawable doInBackground(Shortcut... params) {
			s = params[0];
			return getWebIcon(s);
		}

		@Override
		protected void onPostExecute(Drawable result) {
			if (result == null)
				return;
			s.setIcon(result);
			if (eventListener != null) {
				eventListener.onIconLoad();
			}
		}

	}

}
