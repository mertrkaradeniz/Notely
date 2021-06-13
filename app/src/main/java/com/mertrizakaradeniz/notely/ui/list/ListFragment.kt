package com.mertrizakaradeniz.notely.ui.list

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.adapter.ToDoListAdapter
import com.mertrizakaradeniz.notely.databinding.FragmentListBinding
import com.mertrizakaradeniz.notely.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list), SearchView.OnQueryTextListener {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val toDoViewModel: ToDoViewModel by viewModels()
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
        hideKeyboard(requireActivity())
        setupRecyclerView()
        setupObserver()
        handleClickEvent()
        setupSearch()
        setupOnBackPress()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    searchThroughDatabase(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    searchThroughDatabase(s.toString())
                }
            }
        })
    }

    private fun setupOnBackPress() {
        val onBackPressedCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallBack
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
            toDoViewModel.checkIfDatabaseEmpty(toDo)
            toDoListAdapter.toDoList = toDo
        })

        toDoViewModel.emptyDatabase.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    binding.apply {
                        imgNoData.visibility = View.VISIBLE
                        tvNoData.visibility = View.VISIBLE
                        imgDeleteAll.visibility = View.GONE
                    }
                }
                false -> {
                    binding.apply {
                        imgNoData.visibility = View.INVISIBLE
                        tvNoData.visibility = View.INVISIBLE
                        imgDeleteAll.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun handleClickEvent() {
        toDoListAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putParcelable(getString(R.string.todo), it)
            }
            findNavController().navigate(
                R.id.action_ListFragment_to_addFragment,
                bundle
            )
        }
        binding.apply {
            imgAddNoteMain.setOnClickListener {
                findNavController().navigate(R.id.action_ListFragment_to_addFragment)
            }
            imgAddNote.setOnClickListener {
                findNavController().navigate(R.id.action_ListFragment_to_addFragment)
            }
            imgAddImage.setOnClickListener {
                val bundle = Bundle().apply {
                    putString(
                        requireActivity().getString(R.string.action),
                        requireActivity().getString(R.string.image)
                    )
                }
                findNavController().navigate(R.id.action_ListFragment_to_addFragment, bundle)
            }
            imgWebLink.setOnClickListener {
                val bundle = Bundle().apply {
                    putString(
                        requireActivity().getString(R.string.action),
                        requireActivity().getString(R.string.url)
                    )
                }
                findNavController().navigate(R.id.action_ListFragment_to_addFragment, bundle)
            }
            imgDeleteAll.setOnClickListener {
                confirmRemoval()
            }
            imgLayout.setOnClickListener {
                when (rvNotes.layoutManager) {
                    is LinearLayoutManager -> {
                        imgLayout.setImageResource(R.drawable.ic_layout_grid)
                        rvNotes.layoutManager =
                            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    }
                    is StaggeredGridLayoutManager -> {
                        imgLayout.setImageResource(R.drawable.ic_layout_linear)
                        rvNotes.layoutManager = LinearLayoutManager(requireContext())

                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvNotes.apply {
            adapter = toDoListAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            itemAnimator = SlideInUpAnimator().apply {
                addDuration = 300
            }
            setHasFixedSize(true)
            smoothScrollToPosition(0)
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
            attachToRecyclerView(binding.rvNotes)
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