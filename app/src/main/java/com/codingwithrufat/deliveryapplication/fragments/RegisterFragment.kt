package com.codingwithrufat.deliveryapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.utils.conditions.checkingRegistrationFields
import com.codingwithrufat.deliveryapplication.utils.conditions.isPhoneNumberInDatabaseOrNot
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.util.concurrent.TimeUnit


class RegisterFragment : Fragment() {

    private var firebaseUser: FirebaseUser? = null
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
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        db = FirebaseFirestore.getInstance()

        clickedSignInButton(view)
        clickedSignUpButton(view)

        return view
    }

    private fun clickedSignUpButton(view: View) {
        view.button_sign_up.setOnClickListener {
            val phoneNumber = view.edit_phone_number.text.toString().trim()
            val username = view.edit_username.text.toString().trim()
            val password = view.edit_password.text.toString().trim()
            val type = view.spinner.selectedItem.toString()

            if (checkingRegistrationFields(view, phoneNumber, username, password)) {

                view.rel_progress_register.visibility = VISIBLE

                isPhoneNumberInDatabaseOrNot(
                    db,
                    phoneNumber,
                    "${type}s",
                    isInDB = {
                        view.rel_progress_register.visibility = INVISIBLE
                        Toast.makeText(
                            requireContext(),
                            "There is an account with this phone number",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    isNotInDB = {
                        authenticatePhoneNumber(
                            view,
                            phoneNumber,
                            username,
                            password
                        )
                    }
                )
            }

        }
    }

    private fun clickedSignInButton(view: View) {

        view.txtSignIn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_registerFragment_to_loginFragment)
        }

    }

    private fun authenticatePhoneNumber(
        view: View,
        phoneNumber: String,
        username: String,
        password: String
    ) {

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    signInWithCredential(p0)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    view.rel_progress_register.visibility = INVISIBLE
                    Toast.makeText(context, "${exception.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationID: String,
                    resendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    val bundle = Bundle()
                    bundle.putString("verificationID", verificationID)
                    bundle.putString("username", username)
                    bundle.putString("phone_number", phoneNumber)
                    bundle.putString("password", password)
                    bundle.putString("type", view.spinner.selectedItem.toString())
                    bundle.putBoolean("is_coming_from_verification_fragment", false)
                    bundle.putBoolean("is_coming_from_reset_fragment", false)
                    Navigation.findNavController(view)
                        .navigate(R.id.action_registerFragment_to_verificationFragment, bundle)
                }

            }

            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+994$phoneNumber")  // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity()) // Activity (for callback binding)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {

        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                firebaseUser = it.result!!.user!!

            } else {

                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }

}