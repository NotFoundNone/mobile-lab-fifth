package com.example.mobilelab.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mobilelab.R
import com.example.mobilelab.databinding.SignInFragmentBinding

class SignInFragment : Fragment() {

    private var _binding: SignInFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignInFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.signIn) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.signInButton.setOnClickListener {
            val login = binding.email.text.toString()
            val password = binding.password.text.toString()

            if ((login == "Dema" || login == "Nikita") && password == "123456") {
                Log.d("SignInFragment", "Auth success!")

                val action = SignInFragmentDirections.actionSignInFragmentToCharactersFragment(login)
                findNavController().navigate(action)
            } else {
                Log.d("SignInFragment", "Auth failed!")
                Toast.makeText(activity, "Неверные данные", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}