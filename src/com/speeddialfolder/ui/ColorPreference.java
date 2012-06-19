package com.speeddialfolder.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.speeddialfolder.R;
import com.speeddialfolder.R.id;
import com.speeddialfolder.R.layout;
import com.speeddialfolder.ui.ColorPickerDialog.OnColorChangedListener;

public class ColorPreference extends DialogPreference {
	private int color;

	public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWidgetLayoutResource(R.layout.color_preference);
	}

	public ColorPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);

		ColorPickerDialog.ColorPickerView colorPickerView = new ColorPickerDialog.ColorPickerView(
				getContext(), new OnColorChangedListener() {
					@Override
					public void onColorChanged(int color) {
						setColor(color);
						persistInt(color);
						notifyDependencyChange(shouldDisableDependents());
						notifyChanged();
						if (getDialog() != null)
							getDialog().dismiss();
					}
				}, getColor());
		builder.setView(colorPickerView);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getIndex(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setColor(restoreValue ? getPersistedInt(getColor()) : (Integer) defaultValue);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		View colorView = view.findViewById(R.id.color_view);
		if (colorView != null) {
			colorView.setBackgroundColor(getColor());
		}
	}

	private int getColor() {
		return color;
	}

	private void setColor(int color) {
		this.color = color;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.color = getColor();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setColor(myState.color);
	}

	private static class SavedState extends BaseSavedState {
		int color;

		public SavedState(Parcel source) {
			super(source);
			color = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(color);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
