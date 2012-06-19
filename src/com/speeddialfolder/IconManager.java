package com.speeddialfolder;

import android.content.res.Resources;

public class IconManager {
	private Resources resources;

	public IconManager(Resources resources) {
		this.resources = resources;
	}

	public void setIcon(Shortcut shortcut) {
		if (shortcut != null) {
			switch (shortcut.getType()) {
			case Shortcut.TYPE_WEB:
				shortcut.setIcon(resources.getDrawable(R.drawable.default_icon));
				break;
			case Shortcut.TYPE_OTHER:
				shortcut.setIcon(resources.getDrawable(R.drawable.default_icon));
				break;
			case Shortcut.TYPE_FOLDER:
				shortcut.setIcon(resources.getDrawable(R.drawable.folder));
				break;
			}
		}
	}
}