package com.speeddialfolder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Button;

public class CCWButton extends Button {

	public CCWButton(Context context) {
		this(context, null);
	}

	public CCWButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CCWButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.rotate(-90, canvas.getWidth() / 2, canvas.getHeight() / 2);
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
	}

}
