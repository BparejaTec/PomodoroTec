package com.bpareja.pomodorotec.wear.presentation.datalayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataReceiver : WearableListenerService() {
    private val _sessionDuration = MutableLiveData(25)
    val sessionDuration: LiveData<Int> = _sessionDuration

    private val _breakDuration = MutableLiveData(5)
    val breakDuration: LiveData<Int> = _breakDuration

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event: DataEvent in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/pomodoro_data") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val session = dataMap.getInt("session_duration", 25)
                val breakTime = dataMap.getInt("break_duration", 5)

                // Publica los cambios en LiveData
                _sessionDuration.postValue(session)
                _breakDuration.postValue(breakTime)
            }
        }
    }
}