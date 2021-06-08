package com.mertrizakaradeniz.notely.ui.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.adapter.ToDoListAdapter
import com.mertrizakaradeniz.notely.databinding.FragmentListBinding
import com.mertrizakaradeniz.notely.ui.SharedViewModel
import com.mertrizakaradeniz.notely.ui.ToDoViewModel
import com.mertrizakaradeniz.notely.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list), SearchView.OnQueryTextListener {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val toDoViewModel: ToDoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private val toDoListAdapter: ToDoListAdapter by lazy { ToDoListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupRecyclerView()
        handleClickEvent()
        setupObserver()
        hideKeyboard(requireActivity())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> toDoViewModel.sortByHighPriority.observe(
                viewLifecycleOwner, {
                    toDoListAdapter.toDoList = it
                })
            R.id.menu_priority_low -> toDoViewModel.sortByLowPriority.observe(
                viewLifecycleOwner, {
                    toDoListAdapter.toDoList = it
                })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupObserver() {
        toDoViewModel.gelAllData.observe(viewLifecycleOwner, { toDo ->
            sharedViewModel.checkIfDatabaseEmpty(toDo)
            toDoListAdapter.toDoList = toDo
        })
        sharedViewModel.emptyDatabase.observe(viewLifecycleOwner, {
            when(it) {
                true -> {
                    binding.apply {
                        imgNoData.visibility = View.VISIBLE
                        tvNoData.visibility = View.VISIBLE
                    }
                }
                false -> {
                    binding.apply {
                        imgNoData.visibility = View.INVISIBLE
                        tvNoData.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    private fun handleClickEvent() {
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_ListFragment_to_addFragment)
        }
        toDoListAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putParcelable(getString(R.string.todo), it)
            }
            findNavController().navigate(
                R.id.action_ListFragment_to_updateFragment,
                bundle
            )
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = toDoListAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            itemAnimator = SlideInUpAnimator().apply {
                addDuration = 300
            }
            setHasFixedSize(true)
        }
        setupItemTouchEvent()
    }

    private fun setupItemTouchEvent() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val toDo = toDoListAdapter.toDoList[position]
                toDoViewModel.deleteItem(toDo)
                Snackbar.make(binding.root, "Note archived", Snackbar.LENGTH_LONG)
                    .apply {
                        setAction("Undo") {
                            toDoViewModel.insertData(toDo)
                        }
                        setActionTextColor(ContextCompat.getColor(context, R.color.yellow))
                        show()
                    }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvList)
        }
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"
        toDoViewModel.searchDatabase(searchQuery).observe(viewLifecycleOwner, { list ->
            list?.let {
                toDoListAdapter.toDoList = list
            }
        })
    }

    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            toDoViewModel.deleteAll()
            Toast.makeText(
                requireContext(),
                "Successfully Removed everything!",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to remove everything?")
        builder.create().show()
    }
}