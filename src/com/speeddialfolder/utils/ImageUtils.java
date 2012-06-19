package com.speeddialfolder.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageUtils {

	public static Bitmap cropImage(Bitmap bitmap, int width, int height) {
		return transform(new Matrix(), bitmap, width, height);
	}

	public static Bitmap overlay(Bitmap bitmap, Bitmap underlay, float scale) {
		Bitmap target = Bitmap.createBitmap(underlay.getWidth(), underlay.getHeight(), Config.ARGB_4444);
		Canvas canvas = new Canvas(target);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(underlay, 0, 0, paint);
		float left = (underlay.getWidth() - bitmap.getWidth() * scale) / 2;
		float top = (underlay.getHeight() - bitmap.getHeight() * scale) / 2;
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		canvas.setMatrix(matrix);
		canvas.drawBitmap(bitmap, left / scale, top / scale, paint);
		return target;
	}

	private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if ((deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.getWidth()), deltaYHalf
				+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
			// Log.v(TAG, "draw " + src.toString() + " ==> " + dst.toString());
			c.drawBitmap(source, src, dst, null);
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / (float) targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

		if (b1 != source) {
			b1.recycle();
		}
		return b2;
	}

	public static Bitmap scaleAndCropImage(Bitmap bitmap, int size) {
		int width;
		int height;
		float ratio;

		width = bitmap.getWidth(); // 1600
		height = bitmap.getHeight(); // 1200

		if (width > size || height > size) {
			ratio = (float) height / (float) width;
			width = size;
			height = (int) (ratio * size);

			if (height > size) {
				ratio = (float) width / (float) height;
				height = size;
				width = (int) (ratio * size);
			}

			return transform(bitmap, width, height);
		}

		return bitmap;
	}

	private static Bitmap transform(Bitmap source, int targetWidth, int targetHeight) {
		int width;
		int height;
		float scaleX;
		float scaleY;
		Matrix matrix;

		width = source.getWidth();
		height = source.getHeight();

		scaleX = ((float) targetWidth) / width;
		scaleY = ((float) targetHeight) / height;

		matrix = new Matrix();

		matrix.postScale(scaleX, scaleY);

		return Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
	}

}
