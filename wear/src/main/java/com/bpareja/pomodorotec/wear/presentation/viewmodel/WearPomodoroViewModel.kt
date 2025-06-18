package com.bpareja.pomodorotec.wear.presentation.viewmodel

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bpareja.pomodorotec.utils.DataSyncManager
import com.bpareja.pomodorotec.wear.presentation.datalayer.DataReceiver

class WearPomodoroViewModel(private val dataReceiver: DataReceiver) : ViewModel() {

    val sessionDuration = dataReceiver.sessionDuration // Duración de sesión sincronizada
    val breakDuration = dataReceiver.breakDuration // Duración de descanso sincronizada

    private val _timeLeft = MutableLiveData("25:00") // Tiempo restante visible
    val timeLeft: LiveData<String> = _timeLeft

    private var countDownTimer: CountDownTimer? = null

    /**
     * Enviar acción para iniciar el temporizador al móvil.
     */
    fun startTimerAction(context: Context) {
        DataSyncManager.sendAction(context, "start_timer")
    }

    /**
     * Enviar acción para pausar el temporizador al móvil.
     */
    fun pauseTimerAction(context: Context) {
        DataSyncManager.sendAction(context, "pause_timer")
    }

    /**
     * Iniciar el temporizador localmente en Wear OS.
     */
    fun startTimer(durationInMinutes: Int) {
        val durationInMillis = durationInMinutes * 60 * 1000L
        countDownTimer?.cancel() // Cancela cualquier temporizador previo

        countDownTimer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                _timeLeft.postValue(String.format("%02d:%02d", minutes, seconds))
            }

            override fun onFinish() {
                _timeLeft.postValue("00:00")
            }
        }
        countDownTimer?.start()
    }

    /**
     * Factory para instanciar el ViewModel con DataReceiver.
     */
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
