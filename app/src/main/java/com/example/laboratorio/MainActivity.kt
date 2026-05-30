package com.example.laboratorio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laboratorio.ui.Navigation
import com.example.laboratorio.ui.theme.LaboratorioTheme
import com.example.laboratorio.ui.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = applicationContext as DemoDataApp
            val sessionVm: SessionViewModel = viewModel(
                factory = SessionViewModel.Factory(app.sessionManager)
            )

            val isDarkModePref by sessionVm.isDarkMode.collectAsState()
            val darkTheme = isDarkModePref ?: isSystemInDarkTheme()

            LaboratorioTheme(darkTheme = darkTheme) {
                Navigation()
            }
        }
    }
}

//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.material3.MaterialTheme
//import com.illareklab.demodata.ui.Navigation
//import com.illareklab.demodata.ui.theme.AppTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            AppTheme {
//                // El entry point cede el control al módulo de enrutamiento gráfico
//                Navigation()
//            }
//        }
//    }
//}