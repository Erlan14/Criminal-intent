package com.example.criminal_intent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CrimeListFragment : Fragment() {

    private lateinit var rvCrimes: RecyclerView
    private var adapter: CrimeAdapter = CrimeAdapter(listOf())

    private val vm: CrimeListVM by lazy {
        ViewModelProviders.of(this).get(CrimeListVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        rvCrimes = view.findViewById(R.id.rv_crimes)
        rvCrimes.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.crimesListLiveData.observe(viewLifecycleOwner, {
            updateUI(it)
        })
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter.crimes = crimes
        adapter.notifyDataSetChanged()
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_list_crime, parent, false)
            return when (viewType) {
                //R.layout.item_list_serious_crime -> SeriousCrimeHolder(view)
                else -> CrimeHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val crime = crimes[position]
            when (holder) {
                is CrimeHolder -> holder.bind(crime)
                //is SeriousCrimeHolder -> holder.bind(crime)
                else -> throw IllegalArgumentException("Unknown viewHolder type: $holder")
            }
        }

        override fun getItemCount(): Int {
            return crimes.size
        }

//        override fun getItemViewType(position: Int): Int {
//            val crime = crimes[position]
//            return when {
//                crime.requiresPolice -> R.layout.item_list_serious_crime
//                else -> R.layout.item_list_crime
//            }
//        }
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.iv_solved)

        private lateinit var crime: Crime

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = crime.title
            dateTextView.text = convertDateToString(crime.date)
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        override fun onClick(p0: View?) {
            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }

        private fun convertDateToString(date: Date): String {
            val format = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
            return format.format(date)
        }
    }

//    private inner class SeriousCrimeHolder(view: View) : RecyclerView.ViewHolder(view) {
//        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
//        private val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
//        private val solvedImageView: ImageView = itemView.findViewById(R.id.iv_solved)
//        private val btnCallPolice: Button = itemView.findViewById(R.id.btn_call_police)
//        private lateinit var crime: Crime
//
//        fun bind(crime: Crime) {
//            this.crime = crime
//            titleTextView.text = crime.title
//            dateTextView.text = crime.date.toString()
//            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
//        }
//    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}