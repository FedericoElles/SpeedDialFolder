package com.speeddialfolder.ui;

import com.speeddialfolder.R;
import com.speeddialfolder.R.drawable;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class ColorPreference2 extends Preference {
    private class ColorSelectionListener implements ColorPickerDialog.OnColorSelectedListener{
        public void onColorSelected(int color) {
            ColorPreference2.this.persistInt(color);
        }
    }

    public ColorPreference2(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    protected void onClick() {
        ColorPickerDialog picker = new ColorPickerDialog(
                getContext(), new ColorSelectionListener(), getPersistedInt(getContext().getResources().getColor(R.drawable.default_fg_color)));
        picker.setCanceledOnTouchOutside(true);
        picker.show();
    }
}
