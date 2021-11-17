package com.example.criminal_intent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class CrimeVM : ViewModel() {

    private val crimeRepository = CrimeRepository.getInstance()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    val crimeLiveData: LiveData<Crime> = Transformations.switchMap(crimeIdLiveData) {
        crimeRepository.getCrime(it)
    }

    fun loadCrime(uuid: UUID) {
        crimeIdLiveData.value = uuid
    }

    fun updateCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }

}