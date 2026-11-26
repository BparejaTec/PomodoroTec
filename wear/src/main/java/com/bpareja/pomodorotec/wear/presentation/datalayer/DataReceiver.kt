package com.bpareja.pomodorotec.wear.presentation.datalayer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

object WearDataSyncManager {
fun sendAction(context: Context, action: String) {
    val dataClient = Wearable.getDataClient(context)
    val putDataMapRequest = PutDataMapRequest.create("/pomodoro_action").apply {
        dataMap.putString("action", action)
        dataMap.putLong("timestamp", System.currentTimeMillis())
    }
    dataClient.putDataItem(putDataMapRequest.asPutDataRequest().setUrgent())
}
}

object WearStateRepository {
private val _sessionDuration = MutableLiveData(25)
val sessionDuration: LiveData<Int> = _sessionDuration

private val _breakDuration = MutableLiveData(5)
val breakDuration: LiveData<Int> = _breakDuration

private val _timeLeft = MutableLiveData("25:00")
val timeLeft: LiveData<String> = _timeLeft

private val _phase = MutableLiveData("Concentración")
val phase: LiveData<String> = _phase

private val _isRunning = MutableLiveData(false)
val isRunning: LiveData<Boolean> = _isRunning

private val _progress = MutableLiveData(0f)
val progress: LiveData<Float> = _progress

fun updateDurations(session: Int, breakTime: Int) {
    _sessionDuration.postValue(session)
    _breakDuration.postValue(breakTime)
}

fun updateTimerState(time: String, currentPhase: String, running: Boolean, currentProgress: Float) {
    _timeLeft.postValue(time)
    _phase.postValue(currentPhase)
    _isRunning.postValue(running)
    _progress.postValue(currentProgress)
}
}

class DataReceiver : WearableListenerService() {
override fun onDataChanged(dataEvents: DataEventBuffer) {
    for (event: DataEvent in dataEvents) {
        if (event.type == DataEvent.TYPE_CHANGED) {
            val path = event.dataItem.uri.path
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            when (path) {
                "/pomodoro_data" -> {
                    val session = dataMap.getInt("session_duration", 25)
                    val breakTime = dataMap.getInt("break_duration", 5)
                    WearStateRepository.updateDurations(session, breakTime)
                }
                "/pomodoro_state" -> {
                    val timeLeft = dataMap.getString("time_left", "25:00")
                    val phase = dataMap.getString("phase", "Concentración")
                    val isRunning = dataMap.getBoolean("is_running", false)
                    val progress = dataMap.getFloat("progress", 0f)
                    WearStateRepository.updateTimerState(timeLeft, phase, isRunning, progress)
                }
            }
        }
    }
}
}