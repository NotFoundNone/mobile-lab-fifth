package com.example.fifthmobilelab

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fifthmobilelab.dao.CharacterDao
import com.example.fifthmobilelab.entity.CharacterEntity
import com.example.fifthmobilelab.util.Converters

@Database(entities = [CharacterEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "game_of_thrones_db"
                ).fallbackToDestructiveMigration() // Полностью пересоздаёт базу при изменении версии
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
