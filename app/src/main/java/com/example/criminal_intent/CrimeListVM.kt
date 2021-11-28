package com.example.criminal_intent

import androidx.lifecycle.ViewModel

class CrimeListVM : ViewModel() {

    private val crimeRepository = CrimeRepository.getInstance()

    val crimesListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

}