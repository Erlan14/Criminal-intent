package com.example.criminal_intent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class CrimeListFragment : Fragment() {

    interface Callbacks {
        fun onCrimeSelected(uuid: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var rvCrimes: RecyclerView
    private lateinit var tvClickToAdd: TextView
    private lateinit var tvEmptyLabel: TextView
    private var adapter: CrimeAdapter = CrimeAdapter()

    private val vm: CrimeListVM by lazy {
        ViewModelProviders.of(this).get(CrimeListVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("CrimeListFragment", "onCreateView - $savedInstanceState")
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        rvCrimes = view.findViewById(R.id.rv_crimes)
        tvClickToAdd = view.findViewById(R.id.tv_click_to_add)
        tvEmptyLabel = view.findViewById(R.id.tv_empty_label)
        rvCrimes.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("CrimeListFragment", "onViewCreated")
        vm.crimesListLiveData.observe(viewLifecycleOwner, {
            updateUI(it)
        })
        tvClickToAdd.setOnClickListener { addCrime() }
    }

    override fun onStart() {
        super.onStart()
        Log.i("CrimeListFragment", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("CrimeListFragment", "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.i("CrimeListFragment", "onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.i("CrimeListFragment", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("CrimeListFragment", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("CrimeListFragment", "onDestroy")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
        Log.i("CrimeListFragment", "onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
        Log.i("CrimeListFragment", "onDetach")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                addCrime()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addCrime() {
        val crime = Crime()
        vm.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    private fun updateUI(crimes: List<Crime>) {
        when (crimes.size) {
            0 -> setEmptyListView(true)
            else -> {
                adapter.submitList(crimes)
                setEmptyListView(false)
            }
        }
    }

    private fun setEmptyListView(isEmpty: Boolean) {
        rvCrimes.isVisible = !isEmpty
        tvClickToAdd.isVisible = isEmpty
        tvEmptyLabel.isVisible = isEmpty
    }

    private inner class CrimeAdapter : ListAdapter<Crime, CrimeHolder>(diffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.item_list_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = getItem(position)
            holder.bind(crime)
        }
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
            callbacks?.onCrimeSelected(crime.id)
        }

        private fun convertDateToString(date: Date): String {
            val format = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
            return format.format(date)
        }
    }

    companion object {

        val diffCallback = object : DiffUtil.ItemCallback<Crime>() {
            override fun areItemsTheSame(oldItem: Crime, newItem: Crime) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Crime, newItem: Crime) = oldItem == newItem
        }

        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}