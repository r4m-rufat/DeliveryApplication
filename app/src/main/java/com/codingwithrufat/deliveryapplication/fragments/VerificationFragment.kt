package com.codingwithrufat.deliveryapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.models.users.User
import com.codingwithrufat.deliveryapplication.utils.constants.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_verification.view.*

class VerificationFragment : Fragment() {

    // variables
    private var verificationID: String? = null
    private var username: String? = null
    private var password: String? = null
    private var phone_number: String? = null
    private var type: String? = null

    // objects
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationID = it.getString("verificationID")
            username = it.getString("username")
            phone_number = it.getString("phone_number")
            password = it.getString("password")
            type = it.getString("type")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_verification, container, false)

        // initialize
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        clickedVerifyProceedButton(view)
        setPhoneNumber(view)

        return view
    }

    private fun clickedVerifyProceedButton(view: View) {
        view.button_verifyProceed.setOnClickListener {
            Toast.makeText(context, "${view.etxt_pin_entry.text.toString()}", Toast.LENGTH_SHORT)
                .show()
            verifyPhoneNumberWithCode(verificationID, view.etxt_pin_entry.text.toString())
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val id = task.result?.user!!.uid

                    val user = User(
                        id,
                        phone_number,
                        username,
                        password,
                        type
                    )

                    if (type.equals(getString(R.string.clients))){
                        firebaseFirestore.collection(getString(R.string.clients))
                            .document(id)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Task is successful")
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Error is ${it.message}")
                            }
                    }else{
                        firebaseFirestore.collection(getString(R.string.couriers))
                            .document(id)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Task is successful")
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Error is ${it.message}")
                            }
                    }


                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context, "verification code is invalid", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun setPhoneNumber(view: View){
        view.txt_phone_number.text = "+994 $phone_number"
    }


}