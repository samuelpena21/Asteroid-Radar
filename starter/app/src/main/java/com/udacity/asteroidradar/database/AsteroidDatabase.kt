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
import kotlinx.coroutines.flow.Flow

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

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM AsteroidEntity")
    fun getAllAsteroids(): Flow<List<AsteroidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg asteroidEntity: AsteroidEntity)

    @Delete
    suspend fun delete(asteroidEntity: AsteroidEntity)

    @Query("DELETE FROM AsteroidEntity")
    suspend fun deleteAll()
}

@Database(entities = [AsteroidEntity::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao

    companion object {
        private lateinit var INSTANCE: AsteroidDatabase

        fun getDatabase(context: Context): AsteroidDatabase {
            synchronized(AsteroidDatabase::class.java) {
                if (::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AsteroidDatabase::class.java,
                        "asteroids"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}