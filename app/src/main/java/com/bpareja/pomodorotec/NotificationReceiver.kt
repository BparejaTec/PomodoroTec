package com.bpareja.pomodorotec

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val viewModel = PomodoroViewModel.getInstance(context.applicationContext as Application)

        when (intent.action) {
            "PAUSE_ACTION" -> {
                viewModel.pauseTimer()
            }
            "RESET_ACTION" -> {
                viewModel.resetTimer()
            }
        }
    }
}