package com.jbutewicz.flashlightbyjoe;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class FlashlightWidgetProvider extends AppWidgetProvider {

	// We to implement the AppWidgetProvider to use a widget. Defines the basic
	// methods that allow you to programmatically interface with the App Widget,
	// based on broadcast events. Through it, you will receive broadcasts when
	// the App Widget is updated.

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Intent receiver = new Intent(context, FlashlightWidgetReceiver.class);
		receiver.setAction("COM_FLASHLIGHT");
		receiver.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				receiver, 0);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		views.setOnClickPendingIntent(R.id.flashlight_widget_imageview,
				pendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, views);

	}

}