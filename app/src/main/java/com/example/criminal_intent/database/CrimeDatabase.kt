package com.example.criminal_intent.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.criminal_intent.Crime

@Database(entities = [Crime::class], version = 1)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao

    companion object {
        private const val DATABASE_NAME = "crime-database"

        fun getDatabase(context: Context): CrimeDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CrimeDatabase::class.java,
                DATABASE_NAME).build()
        }
    }

}