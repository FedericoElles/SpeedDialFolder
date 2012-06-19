package com.speeddialfolder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget1x1 extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int appWidgetId : appWidgetIds) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget1x1);
			views.setOnClickPendingIntent(R.id.button_launch, getLaunchIntent(context));
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	private PendingIntent getLaunchIntent(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Intent.ACTION_RUN);
		intent.putExtra("top", true);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		return pendingIntent;
	}
}
