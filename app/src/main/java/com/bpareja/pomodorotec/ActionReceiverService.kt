package com.bpareja.pomodorotec

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bpareja.pomodorotec.pomodoro.PomodoroViewModel
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class ActionReceiverService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/pomodoro_action"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val action = dataMap.getString("action", "")
                Log.d("ActionReceiverService", "Acción recibida desde Wear OS: $action")

                Handler(Looper.getMainLooper()).post {
                    val vm = PomodoroViewModel.instance ?: return@post
                    when (action) {
                        "start_timer" -> {
                            if (vm.timeLeft.value == "25:00" && vm.progress.value == 0f) {
                                vm.startFocusSession()
                            } else {
                                vm.startTimer()
                            }
                        }
                        "pause_timer" -> vm.pauseTimer()
                        "reset_timer" -> vm.resetTimer()
                        "skip_break" -> PomodoroViewModel.skipBreak()
                        else -> Log.d("ActionReceiverService", "Acción desconocida: $action")
                    }
                }
            }
        }
    }
}