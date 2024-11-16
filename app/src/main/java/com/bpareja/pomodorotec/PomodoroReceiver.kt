package com.bpareja.pomodorotec

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel

class PomodoroReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "SKIP_BREAK") {
            PomodoroViewModel.skipBreak() // Esto debería llamar a startFocusSession y restablecer el tiempo de enfoque
        }
    }
}
