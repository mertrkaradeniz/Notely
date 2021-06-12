package com.mertrizakaradeniz.notely.ui.update

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.Priority
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.databinding.FragmentUpdateBinding
import com.mertrizakaradeniz.notely.ui.FirebaseViewModel
import com.mertrizakaradeniz.notely.ui.SharedViewModel
import com.mertrizakaradeniz.notely.ui.ToDoViewModel
import com.mertrizakaradeniz.notely.ui.main.MainActivity
import com.mertrizakaradeniz.notely.util.Constant
import com.mertrizakaradeniz.notely.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateFragment : Fragment(R.layout.fragment_update) {

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by viewModels()
    private val toDoViewModel: ToDoViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var toDo: ToDo
    private lateinit var imageUrl: String
    private var currentFile: Uri? = null

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
        Log.d("SAAS", toDo.toString())
        setHasOptionsMenu(true)
        populateUI()
        handleClickEvent()
        binding.spCurrentPriorities.onItemSelectedListener = sharedViewModel.listener
    }

    private fun handleClickEvent() {
        binding.imgCurrent.setOnClickListener {
            imageUrl = ""
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, Constant.REQUEST_CODE_IMAGE_PICK)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constant.REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                currentFile = it
                binding.imgCurrent.load(it)
                firebaseViewModel.upload(currentFile!!)
                getImageUrl()
            }
        }
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
            imgCurrent.load(toDo.imageUrl)
            when(toDo.priority) {
                Priority.HIGH -> {spCurrentPriorities.setSelection(0)}
                Priority.MEDIUM -> {spCurrentPriorities.setSelection(1)}
                Priority.LOW -> {spCurrentPriorities.setSelection(2)}
            }
        }
    }

    private fun updateItem() {
        getImageUrl()
        binding.apply {
            val title = etCurrentTitle.text.toString()
            val description = etCurrentDescription.text.toString()
            val getPriority = spCurrentPriorities.selectedItem.toString()
            val validation = sharedViewModel.verifyDataFromUser(title, description)
            if (validation) {
                val updatedItem = ToDo(
                    toDo.id,
                    title,
                    sharedViewModel.parsePriority(getPriority),
                    description,
                    imageUrl
                )
                toDoViewModel.updateData(updatedItem)
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
            toDoViewModel.deleteItem(toDo)
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

    private fun getImageUrl() {
        firebaseViewModel.fileUploadResult.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Success -> {
                    (requireActivity() as MainActivity).hideProgressBar()
                    firebaseViewModel.filePath.observe(viewLifecycleOwner, { url ->
                        imageUrl = url
                    })
                }
                is Resource.Error -> {
                    imageUrl = ""
                    (requireActivity() as MainActivity).hideProgressBar()
                    Toast.makeText(
                        requireContext(),
                        "Image uploading is failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {
                    (requireActivity() as MainActivity).showProgressBar()
                }
            }
        })
    }
}