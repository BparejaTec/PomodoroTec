/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.bpareja.pomodorotec.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bpareja.pomodorotec.wear.presentation.theme.PomodoroTecTheme
import com.bpareja.pomodorotec.wear.presentation.datalayer.DataReceiver
import com.bpareja.pomodorotec.wear.presentation.ui.WearPomodoroScreen
import com.bpareja.pomodorotec.wear.presentation.viewmodel.WearPomodoroViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Inicializa el DataReceiver
        val dataReceiver = DataReceiver()

        // Crea una instancia del ViewModel utilizando el ViewModelProvider
        val viewModel: WearPomodoroViewModel by viewModels {
            WearPomodoroViewModel.provideFactory(dataReceiver)
        }

        setContent {
            WearApp(viewModel) // Asegúrate de estar pasando el ViewModel aquí
        }
    }
}

@Composable
fun WearApp(viewModel: WearPomodoroViewModel) {
    PomodoroTecTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WearPomodoroScreen(viewModel = viewModel)
        }
    }
}
