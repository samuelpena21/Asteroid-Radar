package com.udacity.asteroidradar.repositories

import android.util.Log
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.toAsteroidEntity
import com.udacity.asteroidradar.database.toAsteroidModel
import com.udacity.asteroidradar.database.toEntity
import com.udacity.asteroidradar.database.toModel
import com.udacity.asteroidradar.utils.getSeventhDayFromToday
import com.udacity.asteroidradar.utils.getTodayDateFormatted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
                //TODO: Create the worker to delete the asteroids before today. Or filter the database to
                //avoid showing the asteroids before today.
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

}