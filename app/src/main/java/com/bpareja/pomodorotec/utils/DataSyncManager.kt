package com.bpareja.pomodorotec.utils

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object DataSyncManager {

    fun sendPomodoroData(context: Context, sessionDuration: Int, breakDuration: Int) {
        val dataClient: DataClient = Wearable.getDataClient(context)
        val putDataMapRequest = PutDataMapRequest.create("/pomodoro_data").apply {
            dataMap.putInt("session_duration", sessionDuration)
            dataMap.putInt("break_duration", breakDuration)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        dataClient.putDataItem(putDataMapRequest.asPutDataRequest().setUrgent())
    }

    fun sendTimerState(
        context: Context,
        timeLeft: String,
        phase: String,
        isRunning: Boolean,
        progress: Float
    ) {
        val dataClient: DataClient = Wearable.getDataClient(context)
        val putDataMapRequest = PutDataMapRequest.create("/pomodoro_state").apply {
            dataMap.putString("time_left", timeLeft)
            dataMap.putString("phase", phase)
            dataMap.putBoolean("is_running", isRunning)
            dataMap.putFloat("progress", progress)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        dataClient.putDataItem(putDataMapRequest.asPutDataRequest().setUrgent())
    }

    fun sendAction(context: Context, action: String) {
        val dataClient: DataClient = Wearable.getDataClient(context)
        val putDataMapRequest = PutDataMapRequest.create("/pomodoro_action").apply {
            dataMap.putString("action", action)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        dataClient.putDataItem(putDataMapRequest.asPutDataRequest().setUrgent())
    }
}