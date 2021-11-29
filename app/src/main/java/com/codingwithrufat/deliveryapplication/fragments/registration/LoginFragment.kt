package com.codingwithrufat.deliveryapplication.fragments.registration

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.activities.ClientActivity
import com.codingwithrufat.deliveryapplication.utils.conditions.checkLoginFields
import com.codingwithrufat.deliveryapplication.utils.conditions.isPhoneNumberInDatabaseOrNot
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_login.view.*

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
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        db = FirebaseFirestore.getInstance()
        clickedTexts(view)
        clickedLoginButton(view)
        clickedbackfromLogin(view)
        return view
    }
    private fun clickedbackfromLogin(view:View){
        view.ic_backFromLogin.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun clickedTexts(view: View) {

        view.txtSignup.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        view.bt_forget_password.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

    }

    private fun clickedLoginButton(view: View) {

        view.button_login.setOnClickListener {

            val type = view.spinnerForLogin.selectedItem.toString()
            val phone_number = view.edit_phone_number_login.text.toString().trim()
            val password = view.edit_password_login.text.toString().trim()

            if (checkLoginFields(view, phone_number, password)) {

                view.rel_progress_login.visibility = View.VISIBLE

                isPhoneNumberInDatabaseOrNot(
                    db,
                    phone_number,
                    "${type}s",
                    isInDB = {

                        db.collection("${type}s")
                            .whereEqualTo("phone_number", phone_number)
                            .whereEqualTo("password", password)
                            .get()
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful && !task.result!!.isEmpty) {
                                var intent=Intent(context,ClientActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Something went wrong. Try again later.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                                view.rel_progress_login.visibility = View.INVISIBLE

                            }.addOnFailureListener {
                                view.rel_progress_login.visibility = View.INVISIBLE
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong. Try again later.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                    },
                    isNotInDB = {

                        view.rel_progress_login.visibility = View.INVISIBLE // progress is invisible

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