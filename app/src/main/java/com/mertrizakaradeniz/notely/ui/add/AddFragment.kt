package com.mertrizakaradeniz.notely.ui.add

import android.app.Activity
import android.app.TimePickerDialog
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
import java.util.*

@AndroidEntryPoint
class AddFragment : Fragment(R.layout.fragment_add) {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val toDoViewModel: ToDoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var toDo: ToDo
    private lateinit var imageUrl: String
    private val calendar: Calendar = Calendar.getInstance()
    private var currentFile: Uri? = null
    private var isAlarmSet = false

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
        when (item.itemId) {
            R.id.menu_add -> insertToDo()
            R.id.menu_reminder -> setReminder()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setReminder() {
        toDoViewModel.cancelNotification()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        }
        TimePickerDialog(
            requireContext(),
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
        isAlarmSet = true
    }

    private fun insertToDo() {
        binding.apply {
            val title = etTitle.text.toString()
            val description = etDescription.text.toString()
            val priority = spPriorities.selectedItem.toString()
            val validation = sharedViewModel.verifyDataFromUser(title, description)
            if (validation) {
                if (binding.imageView.drawable != null) {
                    getImageUrl()
                } else {
                    imageUrl = ""
                }
                toDo = ToDo(
                    0,
                    title,
                    sharedViewModel.parsePriority(priority),
                    description,
                    imageUrl
                )
                toDoViewModel.insertData(toDo)
                Toast.makeText(
                    requireContext(),
                    "Successfully added!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_addFragment_to_ListFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill out all fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (isAlarmSet) {
                toDoViewModel.setupNotification(calendar, toDo)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}