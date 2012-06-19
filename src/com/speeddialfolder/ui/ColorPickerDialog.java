/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.speeddialfolder.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.speeddialfolder.R;
import com.speeddialfolder.R.id;
import com.speeddialfolder.R.layout;
import com.speeddialfolder.ui.HuePickerView.OnHueChangedListener;
import com.speeddialfolder.ui.HuePickerView.OnHueSelectedListener;
import com.speeddialfolder.ui.LightnessPickerView.OnLightnessChangedListener;

public class ColorPickerDialog extends Dialog {

	public interface OnColorSelectedListener {
		void onColorSelected(int color);
	}

	interface OnColorChangedListener {
		void onColorChanged(int color);
	}

	static class ColorPickerView extends FrameLayout implements OnHueChangedListener,
			OnHueSelectedListener, OnLightnessChangedListener {
		private OnColorChangedListener mColorListener;
		private int mInitialColor;
		private HuePickerView mHuePicker;
		private LightnessPickerView mLightnessPicker;

		ColorPickerView(Context c, OnColorChangedListener l, int color) {
			super(c);
			mColorListener = l;
			mInitialColor = color;

			View rootView = LayoutInflater.from(c).inflate(R.layout.color_picker, null);

			mLightnessPicker = (LightnessPickerView) rootView.findViewById(R.id.lightness_picker);
			mLightnessPicker.setColor(mInitialColor);
			mLightnessPicker.setOnLightnessChangedListener(this);

			mHuePicker = (HuePickerView) rootView.findViewById(R.id.hue_picker);
			mHuePicker.setColor(mInitialColor);
			mHuePicker.setOnHueChangedListener(this);
			mHuePicker.setOnHueSelectedListener(this);

			addView(rootView);
		}

		public void hueChanged(int color) {
			mLightnessPicker.setColor(color);
			mLightnessPicker.invalidate();
		}

		public void hueSelected(int color) {
			mColorListener.onColorChanged(color);
		}

		public void lightnessChanged(int color) {
			mHuePicker.setColor(color);
			mHuePicker.invalidate();
		}
	}

	private OnColorSelectedListener mColorListener;
	private int mInitialColor;

	public ColorPickerDialog(Context context, OnColorSelectedListener listener, int initialColor) {
		super(context);
		mColorListener = listener;
		mInitialColor = initialColor;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new ColorPickerView(getContext(), new OnColorChangedListener() {
			public void onColorChanged(int color) {
				mColorListener.onColorSelected(color);
				dismiss();
			}
		}, mInitialColor));
		setTitle("Pick a Color");
	}

}
