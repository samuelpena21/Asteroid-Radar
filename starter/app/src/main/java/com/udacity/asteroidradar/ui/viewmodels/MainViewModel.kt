package com.udacity.asteroidradar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.utils.getTodayDateFormatted
import com.udacity.asteroidradar.repositories.AsteroidRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UIState {
    object Idle : UIState()
    object Loading : UIState()
    object Success : UIState()
}

enum class AsteroidFilter {
    WEEK, TODAY
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val database = AsteroidDatabase.getDatabase(app)
    private val repository = AsteroidRepository(database)

    private var _asteroids: StateFlow<List<Asteroid>> = repository.asteroids.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private var _filteredList: MutableStateFlow<List<Asteroid>> = MutableStateFlow(_asteroids.value)
    val asteroids: StateFlow<List<Asteroid>> = _filteredList

    val pictureOfDay: Flow<PictureOfDay?> = repository.pictureOfDay
    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Idle)
    val uiState: Flow<UIState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.emit(UIState.Loading)
            repository.refreshAsteroids()
            repository.refreshPictureOfTheDay()
            filterBy()
            _uiState.emit(UIState.Success)
        }
    }

    fun filterBy(filter: AsteroidFilter = AsteroidFilter.WEEK) {
        viewModelScope.launch {
            when (filter) {
                AsteroidFilter.WEEK -> {
                    _filteredList.emit(_asteroids.value)
                }
                AsteroidFilter.TODAY -> {
                    _filteredList.emit(_asteroids.value.filter { it.closeApproachDate ==
                        getTodayDateFormatted() })
                }
            }
        }
    }
}