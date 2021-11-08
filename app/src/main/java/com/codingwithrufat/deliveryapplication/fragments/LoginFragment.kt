package com.codingwithrufat.deliveryapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.utils.conditions.checkLoginFields
import com.codingwithrufat.deliveryapplication.utils.conditions.isPhoneNumberInDatabaseOrNot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_register.view.*

class LoginFragment : Fragment() {

    // objects
    private lateinit var db: FirebaseFirestore

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

        db = FirebaseFirestore.getInstance()
        clickedTexts(view)
        clickedLoginButton(view)
        return view
    }
    private fun clickedTexts(view: View){

        view.txtSignup.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        view.bt_forget_password.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

    }

    private fun clickedLoginButton(view: View){

        view.button_login.setOnClickListener {

            val type = view.spinnerForLogin.selectedItem.toString()
            val phone_number = view.edit_phone_number_login.text.toString().trim()
            val password = view.edit_password_login.text.toString().trim()

            if (checkLoginFields(view, phone_number, password)){

                isPhoneNumberInDatabaseOrNot(
                    db,
                    phone_number,
                    type,
                    isInDB = {

                             db.collection("${type}s")
                                 .whereEqualTo("phone_number", phone_number)
                                 .whereEqualTo("password", password)
                                 .get()
                                 .addOnCompleteListener { task ->

                                     if (task.isSuccessful && !task.result!!.isEmpty) {
                                         Toast.makeText(
                                             requireContext(),
                                             "girmelidir",
                                             Toast.LENGTH_SHORT
                                         )
                                             .show()

                                     }else{
                                         Toast.makeText(
                                             requireContext(),
                                             "girmemelidir",
                                             Toast.LENGTH_SHORT
                                         )
                                             .show()
                                     }

                                 }.addOnFailureListener {
                                     Toast.makeText(requireContext(), "Something went wrong. Try again later.", Toast.LENGTH_SHORT)
                                         .show()
                                 }

                    },
                    isNotInDB = {
                        Toast.makeText(
                            requireContext(),
                            "There is no account with this phone number!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

            }

        }

    }

}