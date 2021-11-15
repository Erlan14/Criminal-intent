package com.example.criminal_intent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.criminal_intent.database.CrimeDatabase
import java.lang.IllegalArgumentException
import java.util.*

class CrimeRepository(private val context: Context) {

    private val database = CrimeDatabase.getDatabase(context)

    private val crimeDao = database.crimeDao()

    fun getCrimes(): LiveData<List<Crime>> {
        return crimeDao.getCrimes()
    }

    fun getCrime(id: UUID): Crime? {
        return crimeDao.getCrime(id)
    }


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