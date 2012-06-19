package com.speeddialfolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.speeddialfolder.utils.ImageUtils;

public class MainActivity extends Activity {

	private static final int MENU_ID_ADD_APP = 110;
	private static final int MENU_ID_ADD_FOLDER = 113;
	private static final int MENU_ID_ADD_OTHER = 112;
	private static final int MENU_ID_ADD_WEB = 111;
	private static final int MENU_ID_CHANGE_ICON = 103;
	private static final int MENU_ID_CHANGE_ICON_SD = 107;
	private static final int MENU_ID_CUT = 104;
	private static final int MENU_ID_DELETE = 100;
	private static final int MENU_ID_EDIT = 102;
	private static final int MENU_ID_PASTE = 105;
	private static final int MENU_ID_SETTINGS = 200;
	private static final int MENU_ID_ABOUT = 201;
	private static final int REQUEST_CODE_OFFSET_CHANGE_ICON_SD = 8000;
	private static final int REQUEST_CODE_OFFSET_CHANGE_ICON = 7000;
	private static final int REQUEST_CODE_OFFSET_CHANGE_TITLE = 6000;
	private static final int REQUEST_CODE_OFFSET_CREATE_APP_LINK = 1000;
	private static final int REQUEST_CODE_OFFSET_CREATE_FOLDER = 4000;
	private static final int REQUEST_CODE_OFFSET_WEBLINK = 5000;
	private static final int REQUEST_CODE_SETTINGS = 19;
	private static final String TAG = MainActivity.class.getCanonicalName();
	private static final int[] tones = new int[] { ToneGenerator.TONE_DTMF_1,
		ToneGenerator.TONE_DTMF_2, ToneGenerator.TONE_DTMF_3, ToneGenerator.TONE_DTMF_4,
		ToneGenerator.TONE_DTMF_5, ToneGenerator.TONE_DTMF_6, ToneGenerator.TONE_DTMF_7,
		ToneGenerator.TONE_DTMF_8, ToneGenerator.TONE_DTMF_9 };
	private BookmarksAdapter bookmarkAdapter;
	private Drawable emptyDrawable;
	private LinkedList<Shortcut> folderStack = new LinkedList<Shortcut>();
	private GridView grid;
	private Handler handler;
	private LocalApplication localApp;
	private long parentId = 0;
	private ShortcutManager shortcutManager;
	private ToneGenerator toneGenerator;
	private Drawable backDrawable;
	private Vibrator vibrator;
	private Bitmap bgBitmap;
	private LayoutInflater layoutInflater;
	private AtomicBoolean isToneGeneratorReleased = new AtomicBoolean(false);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		if (getIntent() != null) {
			parentId = getIntent().getLongExtra("parent_id", 0);
		}
		localApp = (LocalApplication) getApplication();
		shortcutManager = localApp.getShortcutManager();
		shortcutManager.setEventListener(new ShortcutManager.EventListener() {
			@Override
			public void onIconLoad() {
				if (bookmarkAdapter != null) {
					bookmarkAdapter.notifyDataSetInvalidated();
				}
			}
		});

		layoutInflater = LayoutInflater.from(getBaseContext());

		grid = (GridView) findViewById(R.id.maingrid);
		grid.setSelection(-1);
		registerForContextMenu(grid);
		handler = new Handler();
		final Animation animation = AnimationUtils.loadAnimation(this, R.anim.grow_fade_in_center);
		emptyDrawable = getResources().getDrawable(R.drawable.add);
		backDrawable = getResources().getDrawable(R.drawable.back);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 33);

		grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				final Shortcut shortcut = bookmarkAdapter.items.get(position);
				if (shortcut != null && shortcut.getType() != Shortcut.TYPE_FOLDER
					&& shortcut.getIntent() != null) {
					tone(position);
					vibrate(position);
					if (getLocalApp().config().animations()) {
						view.startAnimation(animation);
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								startShortcutActivity(shortcut);
							}

						}, 200);
					} else {
						startShortcutActivity(shortcut);
					}
				} else if (shortcut != null && shortcut.getType() == Shortcut.TYPE_FOLDER) {
					openFolder(shortcut);
					vibrateAndTone(position);
				} else if (position == 4 && parentId != 0) {
					back(1);
					vibrateAndTone(position);
				} else if (position == 4 && parentId == 0) {
					vibrateAndTone(position);
					finish();
				}
			}

			private void startShortcutActivity(final Shortcut shortcut) {
				Intent intent = shortcut.getIntent();
				if (intent != null) {
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					final String title = shortcut.getTitle();
					try {
						startActivity(intent);
						if (shortcut.getType() == Shortcut.TYPE_APPLICATION
							&& getLocalApp().config().getCloseOnAppLaunch()) {
							finish();
						} else {
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									back(getLevel());
								}
							}, 700);
						}
					} catch (ActivityNotFoundException e) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getBaseContext(),
									getString(R.string.error_app_not_found, title),
									Toast.LENGTH_SHORT).show();
							}
						});
					} catch (SecurityException e) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getBaseContext(),
									getString(R.string.error_app_security, title),
									Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}

		});

		findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startSettingsActivity();
			}
		});

		openFolder(null);
		applyConfigParams();

		showHelpOnFirstRun();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getMenuInfo() != null
			&& item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo) {
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
			int position = menuInfo.position;
			Shortcut shortcut = (Shortcut) bookmarkAdapter.getItem(position);
			switch (item.getItemId()) {
			case MENU_ID_DELETE:
				deleteShortcut(position, shortcut);
				break;

			case MENU_ID_EDIT:
				editShortcut(shortcut);
				break;

			case MENU_ID_CHANGE_ICON:
				startActivityForResult(new Intent(this, IconChangeActivity.class),
					REQUEST_CODE_OFFSET_CHANGE_ICON + shortcut.getPosition());
				startActivity(new Intent(getBaseContext(), BrowseGalleryActivity.class));
				break;

			case MENU_ID_CHANGE_ICON_SD:
				startActivityForResult(new Intent(this, BrowseGalleryActivity.class),
					REQUEST_CODE_OFFSET_CHANGE_ICON_SD + shortcut.getPosition());
				break;

			case MENU_ID_CUT:
				getLocalApp().clipboard().put(shortcut);
				break;

			case MENU_ID_PASTE:
				pasteShortcut(position, shortcut);
				break;

			case MENU_ID_ADD_APP:
				addApplicationBookmark(position);
				break;

			case MENU_ID_ADD_WEB:
				addWebLinkBookmark(position);
				break;

			case MENU_ID_ADD_OTHER:
				pickShortcut(position + 2000, "Create Shortcut");
				break;

			case MENU_ID_ADD_FOLDER:
				addFolder(position);

				break;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.maingrid && menuInfo != null
			&& menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
			AdapterView.AdapterContextMenuInfo mInfo = (AdapterContextMenuInfo) menuInfo;
			int position = mInfo.position;
			Shortcut shortcut = (Shortcut) bookmarkAdapter.getItem(position);
			if (shortcut != null) {
				menu.setHeaderTitle(shortcut.getTypeStr());
				menu.add(Menu.NONE, MENU_ID_EDIT, Menu.NONE, R.string.menu_edit);
				if (shortcut.getType() == Shortcut.TYPE_FOLDER
					|| shortcut.getType() == Shortcut.TYPE_OTHER) {
					menu.add(Menu.NONE, MENU_ID_CHANGE_ICON, Menu.NONE, R.string.menu_change_icon);
					menu.add(Menu.NONE, MENU_ID_CHANGE_ICON_SD, Menu.NONE,
						R.string.menu_change_icon_sd);
				}
				// menu.add(Menu.NONE, MENU_ID_MOVE, Menu.NONE, "Move");
				menu.add(Menu.NONE, MENU_ID_DELETE, Menu.NONE, R.string.menu_delete);
				menu.add(Menu.NONE, MENU_ID_CUT, Menu.NONE, R.string.menu_cut);
			} else {
				// "Application", "Web Bookmark", "Folder", "Other Bookmark"
				menu.setHeaderTitle(getText(R.string.menu_title_create_shortcut));
				menu.add(Menu.NONE, MENU_ID_ADD_APP, Menu.NONE, R.string.menu_application);
				menu.add(Menu.NONE, MENU_ID_ADD_WEB, Menu.NONE, R.string.menu_web_link);
				menu.add(Menu.NONE, MENU_ID_ADD_OTHER, Menu.NONE, R.string.menu_other_shortcut);
				if (getLevel() < 3) {
					menu.add(Menu.NONE, MENU_ID_ADD_FOLDER, Menu.NONE, R.string.menu_folder);
				}
			}
			if (!getLocalApp().clipboard().isEmpty()) {
				menu.add(Menu.NONE, MENU_ID_PASTE, Menu.NONE, R.string.menu_paste);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ID_SETTINGS, Menu.NONE, R.string.menu_settings);
		menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE, R.string.menu_about);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !getFolderStack().isEmpty()) {
			back(1);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_SETTINGS:
			startSettingsActivity();
			return true;
		case MENU_ID_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode >= REQUEST_CODE_OFFSET_CREATE_APP_LINK
			&& requestCode < REQUEST_CODE_OFFSET_CREATE_APP_LINK + 9 && resultCode != 0) {
			int position = requestCode - REQUEST_CODE_OFFSET_CREATE_APP_LINK;
			Shortcut item = Shortcut.createForApplication(this, data, parentId, position);
			if (item != null) {
				shortcutManager.save(item);
				BookmarksAdapter adapter = bookmarkAdapter;
				adapter.items.set(position, item);
				adapter.notifyDataSetChanged();
			}
		} else if (requestCode >= 2000 && requestCode < 2010 && resultCode != 0) {
			int newRequestCode = requestCode + REQUEST_CODE_OFFSET_CREATE_APP_LINK;
			processShortcut(data, newRequestCode);
		} else if (requestCode >= 3000 && requestCode < 3010 && resultCode != 0) {
			int position = requestCode - 3000;
			createOtherBookmark(data, position);
		} else if (requestCode >= REQUEST_CODE_OFFSET_CREATE_FOLDER
			&& requestCode < REQUEST_CODE_OFFSET_CREATE_FOLDER + 9 && resultCode == 1) {
			int position = requestCode - REQUEST_CODE_OFFSET_CREATE_FOLDER;
			String title = data.getStringExtra("title");
			Shortcut item = new Shortcut(-1, parentId, Shortcut.TYPE_FOLDER, position, title, "");
			shortcutManager.save(item);
			shortcutManager.setIntent(item);
			shortcutManager.setIcon(item);
			bookmarkAdapter.items.set(position, item);
			bookmarkAdapter.notifyDataSetChanged();
		} else if (requestCode >= REQUEST_CODE_OFFSET_WEBLINK
			&& requestCode < REQUEST_CODE_OFFSET_WEBLINK + 9 && resultCode == 1) {
			int position = requestCode - REQUEST_CODE_OFFSET_WEBLINK;
			String title = data.getStringExtra("title");
			String url = data.getStringExtra("url");
			Shortcut shortcut = (Shortcut) bookmarkAdapter.getItem(position);
			if (shortcut == null) {
				shortcut = new Shortcut(-1, parentId, Shortcut.TYPE_WEB, position, title, url);
				shortcutManager.save(shortcut);
				shortcutManager.setIntent(shortcut);
				shortcutManager.setIcon(shortcut);
				bookmarkAdapter.items.set(position, shortcut);
			} else if (shortcut.getType() == Shortcut.TYPE_WEB) {
				shortcut.setTitle(title);
				shortcut.setUri(url);
				shortcutManager.save(shortcut);
			}
			bookmarkAdapter.notifyDataSetChanged();
		} else if (requestCode >= REQUEST_CODE_OFFSET_CHANGE_TITLE
			&& requestCode < REQUEST_CODE_OFFSET_CHANGE_TITLE + 9 && resultCode == 1) {
			int position = requestCode - REQUEST_CODE_OFFSET_CHANGE_TITLE;
			String title = data.getStringExtra("title");
			Shortcut shortcut = (Shortcut) bookmarkAdapter.getItem(position);
			if (shortcut != null) {
				shortcut.setTitle(title);
				shortcutManager.save(shortcut);
				bookmarkAdapter.notifyDataSetChanged();
			}
		} else if (requestCode >= REQUEST_CODE_OFFSET_CHANGE_ICON
			&& requestCode < REQUEST_CODE_OFFSET_CHANGE_ICON + 9 && resultCode == RESULT_OK) {
			int position = requestCode - REQUEST_CODE_OFFSET_CHANGE_ICON;
			Shortcut shortcut = (Shortcut) bookmarkAdapter.getItem(position);
			int resId = data.getIntExtra("resource_id", 0);
			if (shortcut != null && resId > 0) {
				shortcut.setIconResId(-resId);
				shortcutManager.save(shortcut);
				shortcutManager.setIcon(shortcut);
				bookmarkAdapter.notifyDataSetChanged();
			}
		} else if (requestCode >= REQUEST_CODE_OFFSET_CHANGE_ICON_SD
			&& requestCode < REQUEST_CODE_OFFSET_CHANGE_ICON_SD + 9 && resultCode == RESULT_OK) {
			int position = requestCode - REQUEST_CODE_OFFSET_CHANGE_ICON_SD;
			Log.i(TAG, "position: " + position);
			Shortcut shortcut = (Shortcut) bookmarkAdapter.getItem(position);
			String path = data.getStringExtra("path");
			Log.i(TAG, "position: " + path);
			if (shortcut != null && path != null) {
				Bitmap originalBitmap = BitmapFactory.decodeFile(path);
				try {
					Bitmap scaledBitmap = ImageUtils.scaleAndCropImage(originalBitmap, getDip(48));
					Drawable drawable = new BitmapDrawable(scaledBitmap);
					shortcut.setIconResId(0);
					shortcut.setIcon(drawable);
					// shortcutManager.save(shortcut);
					// shortcutManager.setIcon(shortcut);
					bookmarkAdapter.notifyDataSetChanged();
				} finally {
					if (originalBitmap != null && !originalBitmap.isRecycled()) {
						originalBitmap.recycle();
					}
				}

			}
		} else if (requestCode == REQUEST_CODE_SETTINGS) {
			getLocalApp().config().update();
			applyConfigParams();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void onHomeClick(View view) {
		if (getLevel() > 0) {
			back(getLevel());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isToneGeneratorReleased.set(true);
		toneGenerator.release();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey("folderstack")) {
			long[] ids = savedInstanceState.getLongArray("folderstack");
			List<Shortcut> folderStack = shortcutManager.load(ids);
			for (Shortcut folder : folderStack) {
				openFolder(folder);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		LinkedList<Shortcut> folderStack = getFolderStack();
		long[] ids = new long[folderStack.size()];
		for (int i = 0; i < folderStack.size(); i++) {
			ids[i] = folderStack.get(i).getId();
		}
		outState.putLongArray("folderstack", ids);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (getIntent() != null) {
			boolean isRunAction = getIntent().getAction() != null
				&& getIntent().getAction().equals(Intent.ACTION_RUN);
			boolean isMainAction = getIntent().getAction() != null
				&& getIntent().getAction().equals(Intent.ACTION_MAIN);
			boolean isNewTaskFlagSetted = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == Intent.FLAG_ACTIVITY_NEW_TASK;
			if (isRunAction || isNewTaskFlagSetted) {
				back(getLevel());
			}
			if (isMainAction || isRunAction) {
				vibrateAndTone(4);
			}

		}
	}

	void processShortcut(Intent intent, int requestCodeShortcut) {
		startActivityForResult(intent, requestCodeShortcut);
	}

	private void addApplicationBookmark(int position) {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
		pickIntent.putExtra(Intent.EXTRA_TITLE, "Bookmark Application");
		startActivityForResult(pickIntent, REQUEST_CODE_OFFSET_CREATE_APP_LINK + position);
	}

	private void addFolder(final int position) {
		Intent intent = new Intent(this, TitleActivity.class);
		startActivityForResult(intent, REQUEST_CODE_OFFSET_CREATE_FOLDER + position);
	}

	private void addWebLinkBookmark(final int position) {
		Intent intent = new Intent(this, WebLinkActivity.class);
		startActivityForResult(intent, REQUEST_CODE_OFFSET_WEBLINK + position);
	}

	private void applyConfigParams() {
		if (getLocalApp().config().customBackground()) {
			findViewById(R.id.main).setBackgroundColor(
				getLocalApp().config().getCustomBackroundColor());
			if (getLocalApp().config().customBackgroundImage()
				&& getLocalApp().config().getCustomBackgroundUri() != null) {
				new SetImageBackgroundTask().execute(getLocalApp().config()
					.getCustomBackgroundUri());
			}
		} else {
			findViewById(R.id.main).setBackgroundColor(Color.WHITE);
		}
		bookmarkAdapter.notifyDataSetInvalidated();
	}

	private void back(int steps) {
		steps = Math.min(getFolderStack().size(), steps);
		for (int i = 0; i < steps; i++) {
			getFolderStack().removeLast();
		}
		if (!getFolderStack().isEmpty()) {
			openFolder(getFolderStack().removeLast());
		} else {
			openFolder(null);
		}
	}

	private void createNavBar(List<Shortcut> breadcrumbs) {
		final LinearLayout breadcrumbsView = (LinearLayout) findViewById(R.id.breadcrumbs);

		Log.i(TAG, "breadcrumbs orientation is " + breadcrumbsView.getOrientation());
		int orientation = breadcrumbsView.getOrientation();
		breadcrumbsView.removeAllViews();
		int elementsHeihgt = getDip(30);
		if (!breadcrumbs.isEmpty()) {
			int navSize = Math.min(breadcrumbs.size(), 4);
			ImageButton homeButton = createNavHomeButton(navSize > 1, orientation);
			if (orientation == LinearLayout.VERTICAL) {
				breadcrumbsView
					.addView(homeButton, new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				return;
			} else {
				breadcrumbsView.addView(homeButton, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, elementsHeihgt));
			}

			for (int i = 0; i < navSize; i++) {
				final int ii = i;
				final Shortcut shortcut = breadcrumbs.get(i);
				String title = shortcut.getTitle();
				if (title.length() > 9) {
					title = title.substring(0, 6) + "...";
				}
				if (ii < navSize - 1) {
					boolean isLastButton = ii < navSize - 2;
					Button button = createNavButton(ii, title, isLastButton);

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT, elementsHeihgt);

					layoutParams.leftMargin = getDip(-15);
					breadcrumbsView.addView(button, layoutParams);

				} else {
					TextView titleView = createNavText(title);
					breadcrumbsView.addView(titleView, new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT, elementsHeihgt));
				}
			}

			createNavigationSections(navSize, breadcrumbs);

		} else {
			View home = layoutInflater.inflate(R.layout.nav_home, null);
			if (orientation == LinearLayout.VERTICAL) {
				home.setPadding(getDip(7), getDip(12), getDip(7), getDip(12));
			}
			breadcrumbsView.addView(home, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

			createNavigationSections(0, breadcrumbs);
		}
	}

	private void createNavigationSections(int navSize, List<Shortcut> breadcrumbs) {
		LinearLayout navSectionContainer = (LinearLayout) findViewById(R.id.navigation_section_container);
		navSectionContainer.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(getBaseContext());
		if (!breadcrumbs.isEmpty()) {
			for (int i = 0; i < navSize - 1; i++) {
				TextView sectionView = (TextView) inflater.inflate(R.layout.navigation_section,
					null);
				sectionView.setText(breadcrumbs.get(i).getTitle());
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
				navSectionContainer.addView(sectionView, layoutParams);
				final int position = i + 1;
				sectionView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						back(getLevel() - position);
					}
				});
			}
			TextView sectionTitle = (TextView) inflater.inflate(R.layout.navigation_title, null);
			sectionTitle.setText(breadcrumbs.get(breadcrumbs.size() - 1).getTitle());
			navSectionContainer.addView(sectionTitle, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
		}
	}

	private Button createNavButton(final int position, String title, boolean isLastButton) {
		Button button = null;
		if (isLastButton) {
			button = (Button) layoutInflater.inflate(R.layout.nav_button_last, null);
		} else {
			button = (Button) layoutInflater.inflate(R.layout.nav_button, null);
		}
		button.setText(title);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back(getLevel() - position - 1);
			}
		});
		return button;
	}

	private ImageButton createNavHomeButton(boolean isLast, int orientation) {
		ImageButton homeButton = new ImageButton(this);
		homeButton.setImageResource(R.drawable.home);
		if (orientation == LinearLayout.VERTICAL) {
			homeButton.setBackgroundResource(R.drawable.navbutton_settings);
			homeButton.setPadding(getDip(7), getDip(12), getDip(7), getDip(12));
		} else if (isLast) {
			homeButton.setBackgroundResource(R.drawable.navleft);
			homeButton.setPadding(getDip(20), 0, getDip(30), 0);
		} else {
			homeButton.setBackgroundResource(R.drawable.navleft);
			homeButton.setPadding(getDip(20), 0, getDip(20), 0);
		}
		homeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back(getLevel());
			}
		});
		return homeButton;
	}

	private TextView createNavText(String title) {
		TextView titleView = new TextView(this);
		titleView.setPadding(getDip(10), getDip(7), 0, 0);
		titleView.setTextColor(Color.WHITE);
		titleView.setTextSize(11);
		titleView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		titleView.setText(title);
		return titleView;
	}

	private void createOtherBookmark(Intent data, int position) {
		Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		// Drawable icon = null;
		// ShortcutIconResource iconResource = null;
		// int resId = 0;
		//
		// Parcelable extra =
		// data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
		// if (extra != null && extra instanceof ShortcutIconResource) {
		// try {
		// iconResource = (ShortcutIconResource) extra;
		// final PackageManager packageManager = getPackageManager();
		// Resources resources = packageManager
		// .getResourcesForApplication(iconResource.packageName);
		// final int id = resources.getIdentifier(iconResource.resourceName,
		// null, null);
		// icon = resources.getDrawable(id);
		// resId = id;
		// } catch (Exception e) {
		// Log.w(TAG, "Could not load shortcut icon: " + extra);
		// }
		// }
		//
		// if (icon == null) {
		// icon = getPackageManager().getDefaultActivityIcon();
		// }
		//
		Shortcut shortcut = new Shortcut(-1, parentId, Shortcut.TYPE_OTHER, position, name, intent
			.toUri(Intent.URI_INTENT_SCHEME));
		shortcut.setIntent(intent);
		shortcutManager.setIcon(shortcut);
		BookmarksAdapter adapter = bookmarkAdapter;
		adapter.items.set(position, shortcut);
		shortcutManager.save(shortcut);
		adapter.notifyDataSetChanged();
	}

	private void deleteShortcut(int position, Shortcut shortcut) {
		if (shortcut != null) {
			shortcutManager.delete(shortcut);
			bookmarkAdapter.items.set(position, null);
			bookmarkAdapter.notifyDataSetChanged();
		}
	}

	private void editShortcut(Shortcut shortcut) {
		if (shortcut != null && shortcut.getType() == Shortcut.TYPE_WEB) {
			Intent webLinkEditIntent = new Intent(this, WebLinkActivity.class);
			webLinkEditIntent.putExtra("title", shortcut.getTitle());
			webLinkEditIntent.putExtra("url", shortcut.getUri());
			startActivityForResult(webLinkEditIntent, REQUEST_CODE_OFFSET_WEBLINK
				+ shortcut.getPosition());
		} else if (shortcut != null) {
			Intent titleEditIntent = new Intent(this, TitleActivity.class);
			titleEditIntent.putExtra("title", shortcut.getTitle());
			startActivityForResult(titleEditIntent, REQUEST_CODE_OFFSET_CHANGE_TITLE
				+ shortcut.getPosition());
		}
	}

	private LinkedList<Shortcut> getFolderStack() {
		return folderStack;
	}

	private Drawable getImageBackground(Uri uri) {
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
				return bitmapDrawable;
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (OutOfMemoryError e) {
			if (getBgBitmap() != null) {
				getBgBitmap().recycle();
				setBgBitmap(null);
			}
		}
		return null;
	}

	private int getLevel() {
		return getFolderStack().size();
	}

	private LocalApplication getLocalApp() {
		return localApp;
	}

	public int getScreenOrientation() {
		Display display = getWindowManager().getDefaultDisplay();
		int orientation = display.getOrientation();
		if (orientation == Configuration.ORIENTATION_UNDEFINED) {
			Log.i(TAG, "orientation is undefined");
			Configuration config = getResources().getConfiguration();
			orientation = config.orientation;
			if (orientation == Configuration.ORIENTATION_UNDEFINED) {
				if (display.getWidth() == display.getHeight()) {
					orientation = Configuration.ORIENTATION_SQUARE;
				} else { // if widht is less than height than it is portrait
					if (display.getWidth() < display.getHeight()) {
						orientation = Configuration.ORIENTATION_PORTRAIT;
					} else {
						orientation = Configuration.ORIENTATION_LANDSCAPE;
					}
				}
			}
		}
		return orientation;
	}

	private int getDip(int value) {
		return (int) (value * getResources().getDisplayMetrics().density);
	}

	private void openFolder(Shortcut shortcut) {
		long parentId = 0;
		if (shortcut != null) {
			parentId = shortcut.getId();
			getFolderStack().addLast(shortcut);
			Log.i(TAG, "open folder " + shortcut.getTitle());
		}
		bookmarkAdapter = new BookmarksAdapter(shortcutManager.load(parentId));
		grid.setAdapter(bookmarkAdapter);
		grid.setSelection(4);
		grid.requestFocus();
		this.parentId = parentId;
		new BreadcrumbsBuildTask().execute(getFolderStack().toArray(
			new Shortcut[getFolderStack().size()]));
	}

	private void pasteShortcut(int position, Shortcut s) {
		if (getLocalApp().clipboard().isEmpty()) {
			return;
		}
		Shortcut shortcut = getLocalApp().clipboard().get();
		if (shortcut != null) {
			if (s != null) {
				// shortcutManager.delete(s);
				s.setPosition(shortcut.getPosition());
				s.setParentId(shortcut.getParentId());
				shortcutManager.save(s);
			}
			shortcut.setParentId(parentId);
			shortcut.setPosition(position);
			shortcutManager.save(shortcut);
			getLocalApp().clipboard().clear();
			bookmarkAdapter = new BookmarksAdapter(shortcutManager.load(parentId));
			grid.setAdapter(bookmarkAdapter);
		}
	}

	private void pickShortcut(int requestCode, String title) {
		Bundle bundle = new Bundle();

		ArrayList<String> shortcutNames = new ArrayList<String>();
		shortcutNames.add("");
		bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
		pickIntent.putExtra(Intent.EXTRA_TITLE, title);
		pickIntent.putExtras(bundle);

		startActivityForResult(pickIntent, requestCode);
	}

	private void showHelpOnFirstRun() {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		if (pref.getBoolean("first_run", true)) {
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("first_run", false);
			editor.commit();
			// startActivity(new Intent(this, AboutActivity.class));
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getBaseContext(), R.string.text_welcome_message,
						Toast.LENGTH_LONG).show();
				}
			}, 1000);
		}
	}

	private void startSettingsActivity() {
		Intent intent = new Intent(MainActivity.this, SettingsWrapperActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SETTINGS);
	}

	private void tone(int i) {
		if (getLocalApp().config().acousticFeedback()) {
			if (i >= 0 && i < tones.length && !isToneGeneratorReleased.get()) {
				toneGenerator.startTone(tones[i]);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							if (!isToneGeneratorReleased.get()) {
								toneGenerator.stopTone();
							}
						} catch (Exception e) {
							Log.e(TAG, e.toString());
						}
					}
				}, 50);
			}
		}
	}

	private void vibrate(int i) {
		if (getLocalApp().config().hapticFeedback()) {
			vibrator.vibrate(50);
		}
	}

	private void vibrateAndTone(final int position) {
		vibrate(position);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				tone(position);
			}
		}, 50);
	}

	public void setBgBitmap(Bitmap bgBitmap) {
		this.bgBitmap = bgBitmap;
	}

	public Bitmap getBgBitmap() {
		return bgBitmap;
	}

	private class BookmarksAdapter extends BaseAdapter {
		final List<Shortcut> items;

		public BookmarksAdapter(List<Shortcut> items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			Shortcut shortcut = items.get(position);
			if (shortcut != null) {
				return shortcut.getId();
			} else {
				return -position;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			ImageView i;
			TextView t;

			if (convertView == null) {
				v = LayoutInflater.from(MainActivity.this).inflate(R.layout.grid_item, null);
				v.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT,
					GridView.LayoutParams.WRAP_CONTENT));
			} else {
				v = convertView;
			}

			i = (ImageView) v.findViewById(R.id.icon);
			t = (TextView) v.findViewById(R.id.title);
			t.setTextColor(Color.WHITE);
			Shortcut item = items.get(position);
			if (item != null) {
				i.setImageDrawable(item.getIcon());
				t.setText(item.getTitle() != null ? item.getTitle() : item.getUri());
				t.setBackgroundResource(R.drawable.text_background);
				if (item.getType() == Shortcut.TYPE_FOLDER) {
					t.setTextColor(Color.parseColor("#FFCA64"));
				}
			} else if (position == 4 && parentId != 0) {
				i.setImageDrawable(backDrawable);
				t.setText(R.string.grid_back);
				t.setBackgroundResource(R.drawable.text_background);
			} else if (position == 4 && parentId == 0) {
				i.setImageDrawable(backDrawable);
				t.setText(R.string.grid_exit);
				t.setBackgroundResource(R.drawable.text_background);
			} else {
				if (getLocalApp().config().showPlusIcon()) {
					i.setImageDrawable(emptyDrawable);
				} else {
					i.setImageDrawable(null);
				}
				t.setText("");
				t.setBackgroundDrawable(null);
			}
			return v;
		}

	}

	private class BreadcrumbsBuildTask extends AsyncTask<Shortcut, Void, List<Shortcut>> {
		@Override
		protected List<Shortcut> doInBackground(Shortcut... params) {
			return Arrays.<Shortcut> asList(params);
		}

		@Override
		protected void onPostExecute(List<Shortcut> breadcrumbs) {
			createNavBar(breadcrumbs);
		}
	}

	private class SetImageBackgroundTask extends AsyncTask<Uri, Void, Drawable> {

		@Override
		protected Drawable doInBackground(Uri... params) {
			Uri uri = params[0];
			return getImageBackground(uri);
		}

		@Override
		protected void onPostExecute(Drawable result) {
			if (result != null) {
				findViewById(R.id.main).setBackgroundDrawable(result);
			}
		}

	}

}