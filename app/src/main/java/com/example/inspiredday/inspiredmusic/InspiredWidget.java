package com.example.inspiredday.inspiredmusic;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class InspiredWidget extends AppWidgetProvider {

    private Intent playIntent;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Tell the AppWidgetManager to perform an update on the current app widget
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);

        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        System.out.println("onEnabled widget");
    }

    @Override
    public void onDisabled(Context context) { // Here we have to clean up any work done
        // Enter relevant functionality for when the last widget is disabled
        System.out.println("onDisabled widget");
    }


    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        if(playIntent == null){
            playIntent = new Intent(context, MainActivity.class);
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.inspired_widget);
        views.setOnClickFillInIntent(R.id.show_button, playIntent);
       /* Intent playIntent = new Intent(context, MainActivity.class);
        playIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent playPausePendingIntent = PendingIntent.getActivity(context, 0, playIntent, 0);

        //Create an intent to launch MainActivity
        Intent action = new Intent(context, InspiredWidget.class);
        action.putExtra("msg", "Starts playing");

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        views.setTextViewText(R.id.song_info, widgetText);
        views.setOnClickPendingIntent(R.id.show_button, playPausePendingIntent);*/

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


}

