package com.example.gamezone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GamezoneDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
