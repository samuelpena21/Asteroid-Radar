package com.udacity.asteroidradar.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.repositories.AsteroidRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class UIState {
    object Idle: UIState()
    object Loading: UIState()
    object Success: UIState()
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val database = AsteroidDatabase.getDatabase(app)
    private val repository = AsteroidRepository(database)


    val asteroids: Flow<List<Asteroid>> = repository.asteroids
    val pictureOfDay: Flow<PictureOfDay?> = repository.pictureOfDay
    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Idle)
    val uiState: Flow<UIState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.emit(UIState.Loading)
            repository.refreshAsteroids()
            repository.refreshPictureOfTheDay()
            _uiState.emit(UIState.Success)
        }
    }
}