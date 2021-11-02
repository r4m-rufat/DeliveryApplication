package com.codingwithrufat.deliveryapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.codingwithrufat.deliveryapplication.R
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_register.view.*

class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_login, container, false)
        initializeWidgets(view)
        return view
    }
    private fun initializeWidgets(view: View){

        view.txtSignup.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        view.bt_forget_password.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

    }

}