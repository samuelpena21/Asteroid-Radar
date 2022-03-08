package com.udacity.asteroidradar.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.AsteroidDatabase.Companion.getDatabase
import com.udacity.asteroidradar.repositories.AsteroidRepository

class RefreshAsteroidsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidRepository(database)
        return try {
            repository.deleteAsteroidsBeforeToday()
            repository.refreshAsteroids()
            repository.refreshPictureOfTheDay()
            Log.d("RefreshAsteroidsWorker", "Worker, Success")
            Result.success()
        } catch (e: Exception) {
            Log.d("RefreshAsteroidsWorker", "Worker Error, $e")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }
}