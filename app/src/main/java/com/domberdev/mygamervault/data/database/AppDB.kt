package com.domberdev.mygamervault.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.domberdev.mygamervault.data.database.dao.GameDao
import com.domberdev.mygamervault.data.database.dao.ProfileDao
import com.domberdev.mygamervault.data.database.entities.GameEntity
import com.domberdev.mygamervault.data.database.entities.ProfileEntity

@Database(
    entities = [GameEntity::class, ProfileEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDB : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun profileDao(): ProfileDao

    companion object {
        private var instance: AppDB? = null

        fun getInstance(context: Context): AppDB {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        "MGVDatabase"
                    )
                        .build()
            }
            return instance as AppDB
        }

        fun destroyInstance() {
            instance = null
        }
    }
}