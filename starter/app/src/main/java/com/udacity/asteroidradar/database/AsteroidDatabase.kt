package com.udacity.asteroidradar.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Entity
data class AsteroidEntity(
    @PrimaryKey
    val id: Long,
    val codename: String,
    val closeApproachDate: String,
    val absoluteMagnitude: Double,
    val estimatedDiameter: Double,
    val relativeVelocity: Double,
    val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean
)

@Entity
data class PictureOfDayEntity(
    @PrimaryKey
    val date: String,
    val mediaType: String,
    val title: String,
    val url: String
)

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM AsteroidEntity ORDER BY closeApproachDate ASC")
    fun getAllAsteroids(): Flow<List<AsteroidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg asteroidEntity: AsteroidEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg pictureOfDayEntity: PictureOfDayEntity)

    @Query("SELECT * FROM PictureOfDayEntity ORDER BY date DESC LIMIT 1")
    fun getPictureOfDay(): Flow<PictureOfDayEntity?>

    @Delete
    suspend fun delete(asteroidEntity: AsteroidEntity)

    @Query("DELETE FROM AsteroidEntity WHERE closeApproachDate <= DATE('now')")
    suspend fun deleteOldAsteroids()

    @Query("DELETE FROM PictureOfDayEntity WHERE date <= DATE('now')")
    suspend fun deleteOldPictures()

    @Delete
    suspend fun delete(pictureOfDayEntity: PictureOfDayEntity)

    @Query("DELETE FROM AsteroidEntity")
    suspend fun deleteAll()
}

@Database(entities = [AsteroidEntity::class, PictureOfDayEntity::class], version = 2)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao

    companion object {
        private lateinit var INSTANCE: AsteroidDatabase

        fun getDatabase(context: Context): AsteroidDatabase {
            synchronized(AsteroidDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AsteroidDatabase::class.java,
                        "asteroids"
                    ).fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}

fun Asteroid.toAsteroidEntity(): AsteroidEntity {
    return AsteroidEntity(
        id = id,
        codename = codename,
        closeApproachDate = closeApproachDate,
        absoluteMagnitude = absoluteMagnitude,
        estimatedDiameter = estimatedDiameter,
        relativeVelocity = relativeVelocity,
        distanceFromEarth = distanceFromEarth,
        isPotentiallyHazardous = isPotentiallyHazardous
    )
}

fun AsteroidEntity.toAsteroidModel(): Asteroid {
    return Asteroid(
        id = id,
        codename = codename,
        closeApproachDate = closeApproachDate,
        absoluteMagnitude = absoluteMagnitude,
        estimatedDiameter = estimatedDiameter,
        relativeVelocity = relativeVelocity,
        distanceFromEarth = distanceFromEarth,
        isPotentiallyHazardous = isPotentiallyHazardous
    )
}

fun PictureOfDay.toEntity(): PictureOfDayEntity {
    return PictureOfDayEntity(
        date = date,
        mediaType = mediaType,
        title = title,
        url = url
    )
}

fun PictureOfDayEntity.toModel(): PictureOfDay {
    return PictureOfDay(
        date = date,
        mediaType = mediaType,
        title = title,
        url = url
    )
}