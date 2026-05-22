package com.j4x.iris_ids.ui.screens.adminlogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j4x.iris_ids.data.local.prefs.UrlConfigStore
import com.j4x.iris_ids.data.remote.api.IrisApi
import com.j4x.iris_ids.domain.usecase.LoginAdminUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UrlCheckStatus { Idle, Checking, Valid, Invalid }

data class AdminLoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val authError: String? = null,
    val isLoading: Boolean = false,
    val navigateToDashboard: Boolean = false,
    val showUrlDialog: Boolean = false,
    val urlInput: String = "",
    val urlCheckStatus: UrlCheckStatus = UrlCheckStatus.Idle,
)

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
    private val loginAdminUseCase: LoginAdminUseCase,
    private val urlConfigStore: UrlConfigStore,
    private val api: IrisApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, usernameError = null, authError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, authError = null) }

    fun login() {
        val s = _uiState.value
        val usernameErr = if (s.username.isBlank()) "Requerido" else null
        val passwordErr = if (s.password.isBlank()) "Requerido" else null
        if (usernameErr != null || passwordErr != null) {
            _uiState.update { it.copy(usernameError = usernameErr, passwordError = passwordErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authError = null) }
            loginAdminUseCase(s.username, s.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, navigateToDashboard = true) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, authError = "Usuario o contraseña incorrectos")
                    }
                }
        }
    }

    fun onNavigated() = _uiState.update { it.copy(navigateToDashboard = false) }

    // ── Configuración de URL ──────────────────────────────────────────────────

    fun openUrlDialog() {
        _uiState.update {
            it.copy(
                showUrlDialog  = true,
                urlInput       = urlConfigStore.getUrl(),
                urlCheckStatus = UrlCheckStatus.Idle,
            )
        }
    }

    fun closeUrlDialog() = _uiState.update { it.copy(showUrlDialog = false) }

    fun onUrlInput(value: String) =
        _uiState.update { it.copy(urlInput = value, urlCheckStatus = UrlCheckStatus.Idle) }

    fun testUrl() {
        val url = _uiState.value.urlInput.trim()
        if (url.isBlank()) return
        _uiState.update { it.copy(urlCheckStatus = UrlCheckStatus.Checking) }
        viewModelScope.launch {
            try {
                urlConfigStore.setUrl(url)
                val response = api.health()
                _uiState.update {
                    it.copy(urlCheckStatus = if (response.isSuccessful) UrlCheckStatus.Valid else UrlCheckStatus.Invalid)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(urlCheckStatus = UrlCheckStatus.Invalid) }
            }
        }
    }

    fun saveUrl() {
        urlConfigStore.setUrl(_uiState.value.urlInput.trim())
        _uiState.update { it.copy(showUrlDialog = false) }
    }
}
