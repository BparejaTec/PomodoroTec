package com.bpareja.pomodorotec.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bpareja.pomodorotec.NotificationReceiver
import com.bpareja.pomodorotec.R

class PomodoroWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_pomodoro)

            // Intent para iniciar el temporizador
            val startIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = "START_ACTION"
            }
            val startPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_start_button, startPendingIntent)

            // Intent para pausar el temporizador
            val pauseIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = "PAUSE_ACTION"
            }
            val pausePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_pause_button, pausePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
