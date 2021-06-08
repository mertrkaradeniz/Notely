package com.mertrizakaradeniz.notely.ui.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.Priority
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.databinding.FragmentUpdateBinding
import com.mertrizakaradeniz.notely.ui.SharedViewModel
import com.mertrizakaradeniz.notely.ui.ToDoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFragment : Fragment(R.layout.fragment_update) {

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private lateinit var toDo: ToDo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toDo = arguments?.get(getString(R.string.todo)) as ToDo
        setHasOptionsMenu(true)
        populateUI()
        binding.spCurrentPriorities.onItemSelectedListener = mSharedViewModel.listener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun populateUI() {
        binding.apply {
            etCurrentTitle.setText(toDo.title)
            etCurrentDescription.setText(toDo.description)
            when(toDo.priority) {
                Priority.HIGH -> {spCurrentPriorities.setSelection(0)}
                Priority.MEDIUM -> {spCurrentPriorities.setSelection(1)}
                Priority.LOW -> {spCurrentPriorities.setSelection(2)}
            }
        }
    }

    private fun updateItem() {
        binding.apply {
            val title = etCurrentTitle.text.toString()
            val description = etCurrentDescription.text.toString()
            val getPriority = spCurrentPriorities.selectedItem.toString()
            val validation = mSharedViewModel.verifyDataFromUser(title, description)
            if (validation) {
                val updatedItem = ToDo(
                    toDo.id,
                    title,
                    mSharedViewModel.parsePriority(getPriority),
                    description
                )
                mToDoViewModel.updateData(updatedItem)
                Toast.makeText(requireContext(), "Successfully updated!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_updateFragment_to_ListFragment)
            }else {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _,_ ->
            mToDoViewModel.deleteItem(toDo)
            Toast.makeText(
                requireContext(),
                "Successfully Removed: ${toDo.title}",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_updateFragment_to_ListFragment)
        }
        builder.setNegativeButton("No") { _,_ -> }
        builder.setTitle("Delete '${toDo.title}'?")
        builder.setMessage("Are you sure you want to remove '${toDo.title}'?")
        builder.create().show()
    }
}