package com.bpareja.pomodorotec

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.WearableListenerService

class ActionReceiverService : WearableListenerService() {
    override fun onDataChanged(dataEvents: com.google.android.gms.wearable.DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == com.google.android.gms.wearable.DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/pomodoro_action"
            ) {
                val dataMap = com.google.android.gms.wearable.DataMapItem.fromDataItem(event.dataItem).dataMap
                val action = dataMap.getString("action", "")
                Log.d("ActionReceiverService", "Acción recibida: $action")

                // Manejar la acción (start_timer, pause_timer, etc.)
                when (action) {
                    "start_timer" -> Log.d("ActionReceiverService", "Iniciar temporizador")
                    "pause_timer" -> Log.d("ActionReceiverService", "Pausar temporizador")
                    else -> Log.d("ActionReceiverService", "Acción desconocida")
                }
            }
        }
    }
}
