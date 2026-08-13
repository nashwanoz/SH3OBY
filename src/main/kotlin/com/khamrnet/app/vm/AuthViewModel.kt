package com.khamrnet.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.khamrnet.app.data.entities.User
import com.khamrnet.app.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AppRepository) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    fun login(username: String, password: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repo.login(username, password)
                if (user != null) {
                    _currentUser.value = user
                    _loginError.value = null
                    onDone(true, user.role.name)
                } else {
                    _loginError.value = "بيانات خاطئة"
                    onDone(false, null)
                }
            } catch (e: Exception) {
                _loginError.value = e.message
                onDone(false, null)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    companion object {
        fun provideFactory(repo: AppRepository): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(repo) as T
                }
            }
        }
    }
}
