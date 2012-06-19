package com.speeddialfolder;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;

public class LocalApplication extends Application {
	private ShortcutManager shortcutManager;
	private Clipboard clipboard;
	private Config config;

	@Override
	public void onCreate() {
		super.onCreate();
		shortcutManager = new ShortcutManager(getApplicationContext());
		clipboard = new Clipboard();
		config = new Config();
		config.update();
	}

	public Clipboard clipboard() {
		return clipboard;
	}

	public Config config() {
		return config;
	}

	public ShortcutManager getShortcutManager() {
		return shortcutManager;
	}

	public class Clipboard {
		private Shortcut shortcut;

		private Clipboard() {
		}

		public void put(Shortcut s) {
			this.shortcut = s;
		}

		public Shortcut get() {
			return shortcut;
		}

		public void clear() {
			shortcut = null;
		}

		public boolean isEmpty() {
			return shortcut == null;
		}
	}

	public class Config {
		private boolean isHapticFeedback = false;
		private boolean isAnimations = true;
		private boolean isCustomBackground = false;
		private boolean isShowPlusIcon = true;
		private int customBackgroundColor;
		private String customBackgroundUri;
		private SharedPreferences preferences;
		private boolean isCustomBackgroundImage;
		private boolean isAcousticFeedback = false;
		private boolean closeOnAppLaunch = true;

		private Config() {
			preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}

		public void update() {
			isAnimations = preferences.getBoolean("animations", true);
			isHapticFeedback = preferences.getBoolean("haptic_feedback", false);
			isAcousticFeedback = preferences.getBoolean("acoustic_feedback", false);
			isCustomBackground = preferences.getBoolean("custom_background", false);
			isCustomBackgroundImage = preferences.getBoolean("enable_background_image", false);
			isShowPlusIcon = preferences.getBoolean("show_plus_icon", true);
			closeOnAppLaunch = preferences.getBoolean("close_after_launch_app", true);
			customBackgroundColor = preferences.getInt("background_color", Color.WHITE);
			customBackgroundUri = preferences.getString("background_image_uri", null);
		}

		public boolean hapticFeedback() {
			return isHapticFeedback;
		}

		public boolean animations() {
			return isAnimations;
		}

		public boolean acousticFeedback() {
			return isAcousticFeedback;
		}

		public boolean customBackground() {
			return isCustomBackground;
		}

		public boolean customBackgroundImage() {
			return isCustomBackgroundImage;
		}

		public boolean showPlusIcon() {
			return isShowPlusIcon;
		}

		public int getCustomBackroundColor() {
			return customBackgroundColor;
		}

		public Uri getCustomBackgroundUri() {
			if (customBackgroundUri != null) {
				return Uri.parse(customBackgroundUri);
			}
			return null;
		}

		public boolean getCloseOnAppLaunch() {
			return closeOnAppLaunch;
		}
	}

}
