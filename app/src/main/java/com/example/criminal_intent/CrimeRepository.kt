package com.example.criminal_intent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.criminal_intent.database.CrimeDatabase
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.Executors

class CrimeRepository(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()

    private val database = CrimeDatabase.getDatabase(context)

    private val crimeDao = database.crimeDao()

    private val filesDir = context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Crime>> {
        return crimeDao.getCrimes()
    }

    fun getCrime(id: UUID): LiveData<Crime?> {
        return crimeDao.getCrime(id)
    }

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.update(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.insert(crime)
        }
    }

    fun getCrimePhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)


    companion object {
        private var instance: CrimeRepository? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = CrimeRepository(context)
            }
        }

        fun getInstance(): CrimeRepository {
            return instance ?: throw IllegalArgumentException("CrimeRepository must be initialized")
        }
    }
}