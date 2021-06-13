package com.mertrizakaradeniz.notely.ui.add

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.data.model.Priority
import com.mertrizakaradeniz.notely.data.model.ToDo
import com.mertrizakaradeniz.notely.databinding.FragmentAddBinding
import com.mertrizakaradeniz.notely.databinding.LayoutAddUrlBinding
import com.mertrizakaradeniz.notely.databinding.LayoutBottomSheetBinding
import com.mertrizakaradeniz.notely.databinding.LayoutDeleteNoteBinding
import com.mertrizakaradeniz.notely.ui.auth.FirebaseViewModel
import com.mertrizakaradeniz.notely.ui.main.MainActivity
import com.mertrizakaradeniz.notely.util.Constant.PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE
import com.mertrizakaradeniz.notely.util.Constant.REQUEST_CODE_IMAGE_PICK
import com.mertrizakaradeniz.notely.util.Resource
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddFragment : Fragment(R.layout.fragment_add), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private var _bottomSheetBinding: LayoutBottomSheetBinding? = null
    private val bottomSheetBinding get() = _bottomSheetBinding!!
    private var _urlDialogBinding: LayoutAddUrlBinding? = null
    private val urlDialogBinding get() = _urlDialogBinding!!
    private var _deleteNoteDialogBinding: LayoutDeleteNoteBinding? = null
    private val deleteNoteDialogBinding get() = _deleteNoteDialogBinding!!

    private val toDoAddViewModel: ToDoAddViewModel by viewModels()
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private var dialogAddURL: AlertDialog? = null
    private var dialogDeleteNote: AlertDialog? = null
    private lateinit var dialog: BottomSheetDialog

    private lateinit var toDo: ToDo
    private lateinit var selectedImageUrl: String
    private var selectedColor: String = "#333333"
    private var currentFile: Uri? = null

    private val calendar: Calendar = Calendar.getInstance()
    private var isAlarmSet = false

    private var flagDelete = false
    private lateinit var action: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleArguments()
        dialog = BottomSheetDialog((requireContext()))
        setSubtitleIndicatorColor()
        handleActionRequest()
        initBottomSheet()
        setDateTime()
        handleClickEvent()
        binding.spPriorities.onItemSelectedListener = toDoAddViewModel.listener
        bottomSheetHandleClickEvent(dialog)
    }

    private fun handleArguments() {
        action = if (arguments?.get(requireActivity().getString(R.string.action)) != null) {
            arguments?.get(requireActivity().getString(R.string.action)) as String
        } else {
            ""
        }
        if (arguments?.get(getString(R.string.todo)) != null) {
            toDo = arguments?.get(getString(R.string.todo)) as ToDo
            flagDelete = true
            populateUI()
        } else {
            initToDo()
            flagDelete = false
        }
    }

    private fun handleActionRequest() {
        if (action.isNotBlank()) {
            when (action) {
                requireActivity().getString(R.string.image) -> {
                    requestStoragePermission()
                }
                requireActivity().getString(R.string.url) -> {
                    showAddURLDialog()
                }
            }
        }
    }

    private fun initToDo() {
        val priority = binding.spPriorities.selectedItem.toString()
        selectedImageUrl = ""
        toDo = ToDo(
            0,
            "",
            "",
            "",
            toDoAddViewModel.parsePriority(priority),
            "",
            "",
            "",
            "",
        )
    }

    private fun populateUI() {
        binding.apply {
            etNoteTitle.setText(toDo.title)
            etNoteSubtitle.setText(toDo.subtitle)
            etNote.setText(toDo.noteText)
            tvDateTime.text = toDo.dateTime
            if (toDo.imageUrl != null && toDo.imageUrl?.trim()!!.isNotEmpty()) {
                imgNote.load(toDo.imageUrl)
                imgNote.visibility = View.VISIBLE
                imgRemoveImage.visibility = View.VISIBLE
                selectedImageUrl = toDo.imageUrl!!
            } else {
                imgNote.visibility = View.GONE
                imgRemoveImage.visibility = View.GONE
            }
            if (toDo.webLink != null && toDo.webLink?.trim()!!.isNotEmpty()) {
                llWebURL.visibility = View.VISIBLE
                tvWebURL.text = toDo.webLink
            }
            when (toDo.priority) {
                Priority.HIGH -> {
                    spPriorities.setSelection(0)
                }
                Priority.MEDIUM -> {
                    spPriorities.setSelection(1)
                }
                Priority.LOW -> {
                    spPriorities.setSelection(2)
                }
            }
        }
    }

    private fun setDateTime() {
        binding.tvDateTime.text =
            SimpleDateFormat(
                "EEEE, dd MMMM yyyy HH:mm a",
                Locale.getDefault()
            ).format(Date())
    }

    private fun handleClickEvent() {
        binding.apply {
            imgSave.setOnClickListener {
                insertToDo()
            }
            imgBack.setOnClickListener {
                requireActivity().onBackPressed()
            }
            imageButton.setOnClickListener {
                dialog.show()
            }
            imgRemoveWebUrl.setOnClickListener {
                tvWebURL.text = null
                llWebURL.visibility = View.GONE
            }
            imgRemoveImage.setOnClickListener {
                imgNote.setImageBitmap(null)
                imgNote.visibility = View.GONE
                imgRemoveImage.visibility = View.GONE
                selectedImageUrl = ""
            }
        }
    }

    private fun setReminder() {
        toDoAddViewModel.cancelNotification()
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
            val title = etNoteTitle.text.toString()
            val subtitle = etNoteSubtitle.text.toString()
            val noteText = etNote.text.toString()
            val dateTime = tvDateTime.text.toString()
            val priority = spPriorities.selectedItem.toString()
            val validation = toDoAddViewModel.verifyDataFromUser(title, subtitle, noteText)
            if (validation) {
                if (binding.imgNote.drawable != null) {
                    getImageUrl()
                } else {
                    selectedImageUrl = ""
                }
                val newToDo = ToDo(
                    0,
                    title,
                    dateTime,
                    subtitle,
                    toDoAddViewModel.parsePriority(priority),
                    noteText,
                    selectedImageUrl,
                    selectedColor,
                    ""
                )
                newToDo.id = toDo.id
                if (binding.llWebURL.visibility == View.VISIBLE) {
                    newToDo.webLink = tvWebURL.text.toString()
                }
                toDoAddViewModel.upsertNote(newToDo)
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
                toDoAddViewModel.setupNotification(calendar, toDo)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                currentFile = it
                binding.imgNote.apply {
                    load(it)
                    visibility = View.VISIBLE
                }
                binding.imgRemoveImage.visibility = View.VISIBLE
                firebaseViewModel.upload(currentFile!!)
            }
        }
    }

    private fun getImageUrl() {
        firebaseViewModel.fileUploadResult.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Success -> {
                    (requireActivity() as MainActivity).progressBarVisibility()
                    firebaseViewModel.filePath.observe(viewLifecycleOwner, { url ->
                        selectedImageUrl = url
                    })
                }
                is Resource.Error -> {
                    selectedImageUrl = ""
                    (requireActivity() as MainActivity).progressBarVisibility()
                    Toast.makeText(
                        requireContext(),
                        "Image uploading is failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {
                    (requireActivity() as MainActivity).progressBarVisibility()
                }
            }
        })
    }

    private fun initBottomSheet() {
        val bottomSheet = layoutInflater.inflate(R.layout.layout_bottom_sheet, null as ViewGroup?)
        _bottomSheetBinding = LayoutBottomSheetBinding.inflate(
            layoutInflater,
            bottomSheet as ViewGroup,
            false
        )
        dialog.setContentView(bottomSheetBinding.root)
    }

    private fun initURLDialog() {
        val urlDialog = layoutInflater.inflate(R.layout.layout_add_url, null as ViewGroup?)
        _urlDialogBinding = LayoutAddUrlBinding.inflate(
            layoutInflater,
            urlDialog as ViewGroup,
            false
        )
    }

    private fun initDeleteNoteDialog() {
        val deleteDialog = layoutInflater.inflate(R.layout.layout_delete_note, null as ViewGroup?)
        _deleteNoteDialogBinding = LayoutDeleteNoteBinding.inflate(
            layoutInflater,
            deleteDialog as ViewGroup,
            false
        )
    }

    private fun bottomSheetHandleClickEvent(dialog: BottomSheetDialog) {
        bottomSheetBinding.apply {
            viewColor1.setOnClickListener {
                selectedColor = "#333333"
                initializeImageColor()
                imageColor1.setImageResource(R.drawable.ic_done)
                setSubtitleIndicatorColor()
            }
            viewColor2.setOnClickListener {
                selectedColor = "#FDBE3B"
                initializeImageColor()
                imageColor2.setImageResource(R.drawable.ic_done)
                setSubtitleIndicatorColor()
            }
            viewColor3.setOnClickListener {
                selectedColor = "#FF4B42"
                initializeImageColor()
                imageColor3.setImageResource(R.drawable.ic_done)
                setSubtitleIndicatorColor()
            }
            viewColor4.setOnClickListener {
                selectedColor = "#3A52FC"
                initializeImageColor()
                imageColor4.setImageResource(R.drawable.ic_done)
                setSubtitleIndicatorColor()
            }
            viewColor5.setOnClickListener {
                selectedColor = "#000000"
                initializeImageColor()
                imageColor5.setImageResource(R.drawable.ic_done)
                setSubtitleIndicatorColor()
            }

            if (toDo.color.trim().isNotEmpty()) {
                bottomSheetBinding.apply {
                    when (toDo.color) {
                        "#FDBE3B" -> viewColor2.performClick()
                        "#FF4B42" -> viewColor3.performClick()
                        "#3A52FC" -> viewColor4.performClick()
                        "##000000" -> viewColor5.performClick()
                    }
                }
            }

            llAddImage.setOnClickListener {
                dialog.dismiss()
                requestStoragePermission()
            }
            llAddUrl.setOnClickListener {
                dialog.dismiss()
                showAddURLDialog()
            }

            if (flagDelete) {
                llDeleteNote.visibility = View.VISIBLE
                llDeleteNote.setOnClickListener {
                    dialog.dismiss()
                    showDeleteDialog()
                }
            }

            llAddReminder.setOnClickListener {
                dialog.dismiss()
                setReminder()
            }
        }
    }

    private fun initializeImageColor() {
        bottomSheetBinding.apply {
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
        }
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }

    private fun showDeleteDialog() {
        initDeleteNoteDialog()
        if (dialogDeleteNote == null) {
            AlertDialog.Builder(requireContext()).apply {
                setView(deleteNoteDialogBinding.root)
                dialogDeleteNote = create()
            }
            if (dialogDeleteNote?.window != null) {
                dialogDeleteNote?.window?.setBackgroundDrawable(ColorDrawable(0))
            }
            deleteNoteDialogBinding.apply {
                tvDeleteNote.setOnClickListener {
                    toDoAddViewModel.deleteItem(toDo)
                    dialogDeleteNote?.dismiss()
                    findNavController().navigate(R.id.action_addFragment_to_ListFragment)
                }
                tvCancel.setOnClickListener {
                    dialogDeleteNote?.dismiss()
                }
            }
        }
        dialogDeleteNote?.show()
    }

    private fun showAddURLDialog() {
        initURLDialog()
        if (dialogDeleteNote == null) {
            AlertDialog.Builder(requireContext()).apply {
                setView(urlDialogBinding.root)
                dialogAddURL = create()
            }
            if (dialogAddURL?.window != null) {
                dialogAddURL?.window?.setBackgroundDrawable(ColorDrawable(0))
            }
            urlDialogBinding.apply {
                etUrl.requestFocus()
                tvAdd.setOnClickListener {
                    if (etUrl.text.toString().trim().isEmpty()) {
                        Toast.makeText(requireContext(), "Enter URL", Toast.LENGTH_SHORT).show()
                    } else if (!Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                        Toast.makeText(requireContext(), "Enter valid URL", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        binding.apply {
                            tvWebURL.text = etUrl.text.toString()
                            llWebURL.visibility = View.VISIBLE
                        }
                        dialogAddURL?.dismiss()
                    }
                }
                tvCancel.setOnClickListener {
                    dialogAddURL?.dismiss()
                }
            }
        }
        dialogAddURL?.show()
    }

    private fun requestStoragePermission() {
        EasyPermissions.requestPermissions(
            this,
            "You cannot choose image without Storage Permission.",
            PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, listOf(perms.first()))) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestStoragePermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _bottomSheetBinding = null
        _urlDialogBinding = null
        _deleteNoteDialogBinding = null
    }
}