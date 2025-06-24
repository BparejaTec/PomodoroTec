package com.bpareja.pomodorotec

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PomodoroWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Por cada widget activo
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            // Lee los datos desde SharedPreferences (guardados por el ViewModel)
            val prefs = context.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)
            val phase = prefs.getString("phase", "Concentración") ?: "Concentración"
            val timeLeft = prefs.getString("timeLeft", "25:00") ?: "25:00"
            val progress = prefs.getInt("progress", 0)

            val views = RemoteViews(context.packageName, R.layout.widget_pomodoro)
            views.setTextViewText(R.id.widget_phase, phase)
            views.setTextViewText(R.id.widget_time, timeLeft)
            views.setProgressBar(R.id.widget_progress, 100, progress, false)

            // Botón para abrir la app principal
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_open_app, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}