package com.example.laboratorio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.laboratorio.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    val isLoggedIn = sessionManager.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val username = sessionManager.currentUsername.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val isDarkMode = sessionManager.isDarkMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null // null significa que usará el del sistema
    )

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        // Para el laboratorio: credenciales locales fijas.
        // En producción se delega a un backend o EncryptedSharedPreferences.
        if (username == "jkn" && password == "jkn") {
            viewModelScope.launch {
                sessionManager.login(username)
                onResult(true)
            }
        } else {
            onResult(false)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setDarkMode(enabled)
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionViewModel(sessionManager) as T
    }
}