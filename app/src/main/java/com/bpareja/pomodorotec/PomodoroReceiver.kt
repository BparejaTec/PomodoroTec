package com.bpareja.pomodorotec

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel

class PomodoroReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "PAUSE_TIMER" -> PomodoroViewModel.instance?.pauseTimer()
            "RESUME_TIMER" -> PomodoroViewModel.instance?.startTimer()
            "SKIP_BREAK" -> PomodoroViewModel.skipBreak()
            "END_TIMER" -> PomodoroViewModel.instance?.resetTimer() // Detiene y reinicia
        }
    }
}