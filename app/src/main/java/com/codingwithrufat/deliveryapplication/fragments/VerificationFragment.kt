package com.codingwithrufat.deliveryapplication.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.models.users.User
import com.codingwithrufat.deliveryapplication.utils.constants.ONE_MINUTE_TIME
import com.codingwithrufat.deliveryapplication.utils.constants.SECOND_MS
import com.codingwithrufat.deliveryapplication.utils.constants.TAG
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.fragment_verification.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class VerificationFragment : Fragment() {

    // variables
    private var verificationID: String? = null
    private var username: String? = null
    private var password: String? = null
    private var phone_number: String? = null
    private var type: String? = null
    private var is_coming_from_reset_fragment: Boolean? = null

    // objects
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            is_coming_from_reset_fragment = it.getBoolean("is_coming_from_reset_fragment")
            if (is_coming_from_reset_fragment!!){
                verificationID = it.getString("verificationID")
                type = it.getString("type")
                phone_number = it.getString("phone_number")
            }else{
                verificationID = it.getString("verificationID")
                username = it.getString("username")
                phone_number = it.getString("phone_number")
                password = it.getString("password")
                type = it.getString("type")
            }
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
        clickedResendOTP(view)
        countdownTimer(view)

        return view
    }

    private fun clickedVerifyProceedButton(view: View) {
        view.button_verifyProceed.setOnClickListener {
            if (is_coming_from_reset_fragment!!){

            }else{
                verifyPhoneNumberWithCode(verificationID, view.etxt_pin_entry.text.toString()) // coming from sign up page
            }
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

                    if (type.equals("Client")) {
                        firebaseFirestore.collection(getString(R.string.clients))
                            .document(id)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Task is successful")
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Error is ${it.message}")
                            }
                    } else {
                        firebaseFirestore.collection("Courier")
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

    private fun verifyPhoneNumber(view: View, credential: PhoneAuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val bundle = Bundle()
                    bundle.putString("phone_number", phone_number)
                    bundle.putString("type", type)
                    bundle.putBoolean("is_coming_from_verification_fragment", true)
                    Navigation.findNavController(view).navigate(R.id.action_verificationFragment_to_resetPasswordFragment)

                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context, "Verification code is invalid", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }



    private fun clickedResendOTP(view: View) {

        view.setOnClickListener {

            // text equals time when clicked that's why it must be checked
            if (view.txt_resend_otp.text == getString(R.string.bt_resend)) {
                authenticatePhoneNumber()
            }

            countdownTimer(view)

        }

    }

    private fun countdownTimer(view: View){
        val ms = SECOND_MS
        var counter = ONE_MINUTE_TIME

        /**
         * when counter is working then its ui changed
         */

        view.txt_resend_otp.typeface = Typeface.DEFAULT_BOLD

        CoroutineScope(Main).launch {

            repeat(60) {
                counter--

                // counter is set
                view.txt_resend_otp.text = "00:$counter"

                if (counter == 0) {
                    view.txt_resend_otp.text = getString(R.string.bt_resend) // if counter time is finished then text is set

                    view.txt_resend_otp.typeface = Typeface.DEFAULT // changed again to default
                }

                delay(ms)

            }

        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun setPhoneNumber(view: View) {
        view.txt_phone_number.text = "+994 $phone_number"
    }

    private fun authenticatePhoneNumber() {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Toast.makeText(requireContext(), "${exception.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                id: String,
                resendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationID = id
            }

        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber("+994$phone_number")  // Phone number to verify
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