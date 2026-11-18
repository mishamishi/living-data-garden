package com.example.livingdatagarden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livingdatagarden.data.GardenRepository
import com.example.livingdatagarden.data.GardenResult
import com.example.livingdatagarden.data.GardenSnapshot
import com.example.livingdatagarden.data.OpenMeteoGardenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GardenUiState(
    val snapshot: GardenSnapshot? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val live: Boolean = true,
)

class GardenViewModel(
    private val repo: GardenRepository = OpenMeteoGardenRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(GardenUiState())
    val state: StateFlow<GardenUiState> = _state.asStateFlow()

    private var liveJob: Job? = null

    init {
        startLive()
    }

    /** Fetches once; ignored while another fetch is running. Keeps the old snapshot on failure. */
    fun refresh() {
        if (_state.value.isRefreshing) return
        _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            when (val result = repo.fetch()) {
                is GardenResult.Success ->
                    _state.update { it.copy(snapshot = result.snapshot, isRefreshing = false, error = null) }
                is GardenResult.Failure ->
                    _state.update { it.copy(isRefreshing = false, error = result.reason) }
            }
        }
    }

    fun setLive(live: Boolean) {
        if (live == _state.value.live && (!live || liveJob?.isActive == true)) return
        _state.update { it.copy(live = live) }
        if (live) startLive() else {
            liveJob?.cancel()
            liveJob = null
        }
    }

    private fun startLive() {
        liveJob?.cancel()
        liveJob = viewModelScope.launch {
            while (true) {
                refresh()
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    private companion object {
        const val REFRESH_INTERVAL_MS = 15 * 60 * 1000L
    }
}
