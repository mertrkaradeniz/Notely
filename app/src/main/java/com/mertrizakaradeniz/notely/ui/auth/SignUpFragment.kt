package com.mertrizakaradeniz.notely.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.databinding.FragmentSignUpBinding
import com.mertrizakaradeniz.notely.ui.main.MainActivity
import com.mertrizakaradeniz.notely.util.Resource
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FirebaseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        handleClickEvent()
    }

    private fun handleClickEvent() {
        binding.apply {
            btnSignUp.setOnClickListener {
                if (etEmail.text.toString().isNotEmpty() && etPassword.text.toString()
                        .isNotEmpty()
                ) {
                    viewModel.signUp(etEmail.text.toString(), etPassword.text.toString())
                }
            }
            tvSignIn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun setupObservers() {
        viewModel.signUpResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    (requireActivity() as MainActivity).progressBarVisibility()
                    Toast.makeText(
                        requireContext(),
                        "Registration is successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    (requireActivity() as MainActivity).progressBarVisibility()
                    Toast.makeText(requireContext(), "Registration is failed", Toast.LENGTH_SHORT)
                        .show()
                }
                is Resource.Loading -> {
                    (requireActivity() as MainActivity).progressBarVisibility()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}