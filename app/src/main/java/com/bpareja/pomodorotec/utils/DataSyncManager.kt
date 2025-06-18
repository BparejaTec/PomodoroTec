package com.bpareja.pomodorotec.utils

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object DataSyncManager {

    /**
     * Enviar datos de Pomodoro al móvil.
     * @param context Contexto de la aplicación.
     * @param sessionDuration Duración de la sesión en minutos.
     * @param breakDuration Duración del descanso en minutos.
     */
    fun sendPomodoroData(context: Context, sessionDuration: Int, breakDuration: Int) {
        val dataClient: DataClient = Wearable.getDataClient(context)
        val putDataMapRequest = PutDataMapRequest.create("/pomodoro_data")
        putDataMapRequest.dataMap.putInt("session_duration", sessionDuration)
        putDataMapRequest.dataMap.putInt("break_duration", breakDuration)
        dataClient.putDataItem(putDataMapRequest.asPutDataRequest())
    }

    /**
     * Enviar una acción al móvil (por ejemplo, iniciar o pausar el temporizador).
     * @param context Contexto de la aplicación.
     * @param action Acción a enviar (e.g., "start_timer", "pause_timer").
     */
    fun sendAction(context: Context, action: String) {
        val dataClient: DataClient = Wearable.getDataClient(context)
        val putDataMapRequest = PutDataMapRequest.create("/pomodoro_action")
        putDataMapRequest.dataMap.putString("action", action) // Añadir la acción
        dataClient.putDataItem(putDataMapRequest.asPutDataRequest()) // Enviar la acción
    }
}
