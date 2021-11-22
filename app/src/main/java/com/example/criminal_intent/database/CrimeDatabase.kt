package com.example.criminal_intent.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminal_intent.Crime

@Database(entities = [Crime::class], version = 2)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao

    companion object {
        private const val DATABASE_NAME = "crime-database"

        fun getDatabase(context: Context): CrimeDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CrimeDatabase::class.java,
                DATABASE_NAME)
                .addMigrations(migration_1_2)
                .build()
        }

        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }

}