package com.example.criminal_intent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val ARG_CRIME_ID = "crime_id"

class CrimeFragment : Fragment() {

    private lateinit var crime: Crime

    private val crimeVM: CrimeVM by lazy {
        ViewModelProviders.of(this)[CrimeVM::class.java]
    }

    private lateinit var etTitle: EditText
    private lateinit var btnDate: Button
    private lateinit var cbSolved: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeVM.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        etTitle = view.findViewById(R.id.et_title)
        btnDate = view.findViewById(R.id.btn_date)
        cbSolved = view.findViewById(R.id.cb_solved)

        btnDate.apply {
            text = crime.date.toString()
            isEnabled = false
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        val titleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etTitle.addTextChangedListener(titleTextWatcher)

        cbSolved.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeVM.crimeLiveData.observe(viewLifecycleOwner, {
            crime = it
            updateUI()
        })
    }

    override fun onStop() {
        super.onStop()
        crimeVM.updateCrime(crime)
    }

    private fun updateUI() {
        etTitle.setText(crime.title)
        btnDate.text = crime.date.toString()
        cbSolved.isChecked = crime.isSolved
        cbSolved.jumpDrawablesToCurrentState()
    }

    companion object {
        fun newInstance(uuid: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, uuid)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}