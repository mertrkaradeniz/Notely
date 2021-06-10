package com.mertrizakaradeniz.notely.ui.signin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mertrizakaradeniz.notely.R
import com.mertrizakaradeniz.notely.databinding.FragmentSignInBinding
import com.mertrizakaradeniz.notely.ui.FirebaseViewModel
import com.mertrizakaradeniz.notely.ui.main.MainActivity
import com.mertrizakaradeniz.notely.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel: FirebaseViewModel by viewModels()

    private lateinit var email: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        handleClickEvent()
    }

    override fun onStart() {
        super.onStart()
        if (firebaseViewModel.checkUserLoggedIn()) {
            findNavController().navigate(R.id.action_signInFragment_to_ListFragment)
        }
    }

    private fun handleClickEvent() {
        binding.apply {
            btnSignIn.setOnClickListener {
                email = etEmail.text.toString()
                password = etPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    firebaseViewModel.signIn(email, password)
                }
            }
            tvSignUp.setOnClickListener {
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {

        firebaseViewModel.signInResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Success -> {
                    (requireActivity() as MainActivity).hideProgressBar()
                    findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToListFragment())
                }
                is Resource.Error -> {
                    (requireActivity() as MainActivity).hideProgressBar()
                    Toast.makeText(requireContext(), "Sign in is failed", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    (requireActivity() as MainActivity).showProgressBar()
                }
            }
        }

    }
}