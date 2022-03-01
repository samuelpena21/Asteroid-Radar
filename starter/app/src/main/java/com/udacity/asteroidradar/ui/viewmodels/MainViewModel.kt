package com.udacity.asteroidradar.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(app: Application) : AndroidViewModel(app) {


    init {
        viewModelScope.launch {
            try {
                val result = Network.asteroidsService.getAsteroidsAsync(
                    "2022-02-28", "2022-03-07"
                )
                val resultObject = parseAsteroidsJsonResult(JSONObject(result))
                Log.d("MainViewModel","$resultObject")
            } catch (e: Exception) {

            }

        }
    }
}