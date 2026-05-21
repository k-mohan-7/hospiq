package com.simats.hospiq.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.network.models.AppNotification
import com.simats.hospiq.utils.DemoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NotificationState {
    object Loading : NotificationState()
    data class Success(val notifications: List<AppNotification>) : NotificationState()
    data class Error(val message: String) : NotificationState()
}

class NotificationViewModel : ViewModel() {
    private val _state = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val state: StateFlow<NotificationState> = _state

    fun loadNotifications(userId: Int) {
        viewModelScope.launch {
            _state.value = NotificationState.Loading
            try {
                val response = RetrofitInstance.api.getNotifications(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.value = NotificationState.Success(
                        response.body()!!.data?.notifications ?: emptyList()
                    )
                } else {
                    _state.value = NotificationState.Success(DemoData.notifications)
                }
            } catch (e: Exception) {
                _state.value = NotificationState.Success(DemoData.notifications)
            }
        }
    }
}
