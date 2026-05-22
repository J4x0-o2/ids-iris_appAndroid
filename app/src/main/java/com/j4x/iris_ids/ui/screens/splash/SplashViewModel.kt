package com.j4x.iris_ids.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.prefs.UrlConfigStore
import com.j4x.iris_ids.data.remote.api.IrisApi
import com.j4x.iris_ids.domain.usecase.GetActiveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ServerStatus { Checking, Online, Offline }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getActiveSession: GetActiveSessionUseCase,
    private val api: IrisApi,
    private val urlConfigStore: UrlConfigStore,
) : ViewModel() {

    sealed class Destination {
        data object Home : Destination()
        data object ShowButtons : Destination()
    }

    private val _destination = MutableStateFlow<Destination?>(null)
    val destination: StateFlow<Destination?> = _destination.asStateFlow()

    private val _serverStatus = MutableStateFlow(ServerStatus.Checking)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _currentUrl = MutableStateFlow(urlConfigStore.getUrl())
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    init {
        checkServer()

        viewModelScope.launch {
            delay(1200)
            val session = getActiveSession().first()
            _destination.value = if (session != null) Destination.Home else Destination.ShowButtons
        }
    }

    fun recheck() {
        _currentUrl.value = urlConfigStore.getUrl()
        checkServer()
    }

    private fun checkServer() {
        _serverStatus.value = ServerStatus.Checking
        viewModelScope.launch {
            try {
                val response = api.health()
                _serverStatus.value = if (response.isSuccessful) ServerStatus.Online else ServerStatus.Offline
            } catch (e: Exception) {
                _serverStatus.value = ServerStatus.Offline
            }
        }
    }

    fun onNavigated() {
        _destination.value = null
    }
}
