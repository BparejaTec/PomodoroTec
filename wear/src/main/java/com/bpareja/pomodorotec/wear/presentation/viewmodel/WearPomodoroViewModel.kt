package com.bpareja.pomodorotec.wear.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bpareja.pomodorotec.wear.presentation.datalayer.DataReceiver
import com.bpareja.pomodorotec.wear.presentation.datalayer.WearDataSyncManager
import com.bpareja.pomodorotec.wear.presentation.datalayer.WearStateRepository

class WearPomodoroViewModel(@Suppress("UNUSED_PARAMETER") dataReceiver: DataReceiver) : ViewModel() {

val sessionDuration = WearStateRepository.sessionDuration
val breakDuration = WearStateRepository.breakDuration
val timeLeft = WearStateRepository.timeLeft
val phase = WearStateRepository.phase
val isRunning = WearStateRepository.isRunning
val progress = WearStateRepository.progress

fun startTimerAction(context: Context) {
    WearDataSyncManager.sendAction(context, "start_timer")
}

fun pauseTimerAction(context: Context) {
    WearDataSyncManager.sendAction(context, "pause_timer")
}

fun resetTimerAction(context: Context) {
    WearDataSyncManager.sendAction(context, "reset_timer")
}

fun skipBreakAction(context: Context) {
    WearDataSyncManager.sendAction(context, "skip_break")
}

companion object {
    fun provideFactory(dataReceiver: DataReceiver): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WearPomodoroViewModel(dataReceiver) as T
            }
        }
    }
}
}