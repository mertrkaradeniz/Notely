package com.mertrizakaradeniz.notely.ui.add

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.databinding.FragmentAddBinding
import com.mertrizakaradeniz.notely.ui.FirebaseViewModel
import com.mertrizakaradeniz.notely.ui.SharedViewModel
import com.mertrizakaradeniz.notely.ui.ToDoViewModel
import com.mertrizakaradeniz.notely.ui.main.MainActivity
import com.mertrizakaradeniz.notely.util.Constant.REQUEST_CODE_IMAGE_PICK
import com.mertrizakaradeniz.notely.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFragment : Fragment(R.layout.fragment_add) {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val toDoViewModel: ToDoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var currentFile: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        handleClickEvent()
        binding.spPriorities.onItemSelectedListener = sharedViewModel.listener
    }

    private fun handleClickEvent() {
        binding.imageButton.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add) {
            insertToDo()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertToDo() {
        binding.apply {
            val title = etTitle.text.toString()
            val description = etDescription.text.toString()
            val priority = spPriorities.selectedItem.toString()
            val validation = sharedViewModel.verifyDataFromUser(title, description)
            if (validation) {
                val newData = ToDo(
                    0,
                    title,
                    sharedViewModel.parsePriority(priority),
                    description
                )
                firebaseViewModel.fileUploadResult.observe(viewLifecycleOwner, { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            (requireActivity() as MainActivity).hideProgressBar()
                            toDoViewModel.insertData(newData)
                            Toast.makeText(
                                requireContext(),
                                "Successfully added!",
                                Toast.LENGTH_SHORT
                            ).show()
                            firebaseViewModel.filePath.observe(viewLifecycleOwner, {
                                Log.d("SAAS", it)
                            })
                            findNavController().navigate(R.id.action_addFragment_to_ListFragment)
                        }
                        is Resource.Error -> {
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
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                currentFile = it
                binding.imageView.load(it)
                firebaseViewModel.upload(currentFile!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}