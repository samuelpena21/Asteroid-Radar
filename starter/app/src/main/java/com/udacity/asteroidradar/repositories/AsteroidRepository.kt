package com.udacity.asteroidradar.repositories

import android.util.Log
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.toAsteroidEntity
import com.udacity.asteroidradar.database.toAsteroidModel
import com.udacity.asteroidradar.database.toEntity
import com.udacity.asteroidradar.database.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AsteroidRepository(private val database: AsteroidDatabase) {

    var asteroids: Flow<List<Asteroid>> = database.asteroidDao.getAllAsteroids()
        .map { it.map { asteroid -> asteroid.toAsteroidModel() } }

    var pictureOfDay: Flow<PictureOfDay?> = database.asteroidDao.getPictureOfDay().map { it?.toModel() }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val startDate = getTodayDateFormatted()
                val endDate = getSeventhDayFromToday()
                val result = Network.asteroidsService.getAsteroidsAsync(startDate, endDate)
                val resultObject = parseAsteroidsJsonResult(JSONObject(result)).map { it.toAsteroidEntity() }.toTypedArray()
                database.asteroidDao.insertAll(*resultObject)
            } catch (e: Exception) {
                Log.d("AsteroidRepository", "Exception: ${e.message}")
            }
        }
    }

    suspend fun refreshPictureOfTheDay() {
        withContext(Dispatchers.IO) {
            try {
                val todayDate = getTodayDateFormatted()
                val result = Network.asteroidsService.getImageOfTheDay(todayDate)
                database.asteroidDao.insertAll(result.toEntity())
            } catch (e: Exception) {
                Log.d("AsteroidRepository", "Exception: ${e.message}")
            }
        }
    }

    private fun getTodayDateFormatted(): String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return (dateFormat.format(currentTime))
    }

    private fun getSeventhDayFromToday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return (dateFormat.format(currentTime))
    }

}