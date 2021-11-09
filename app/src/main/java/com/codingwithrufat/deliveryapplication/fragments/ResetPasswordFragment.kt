package com.codingwithrufat.deliveryapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.utils.conditions.checkChangePasswordFields
import com.codingwithrufat.deliveryapplication.utils.conditions.isPhoneNumberInDatabaseOrNot
import com.codingwithrufat.deliveryapplication.utils.constants.TAG
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_reset_password.view.*
import java.util.concurrent.TimeUnit


class ResetPasswordFragment : Fragment() {

    // variables
    private var is_coming_from_verification_fragment: Boolean? = false
    private var phone_number: String? = null
    private var type: String? = null

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            is_coming_from_verification_fragment =
                it.getBoolean("is_coming_from_verification_fragment")
            if (is_coming_from_verification_fragment!!) {
                type = it.getString("type")
                phone_number = it.getString("phone_number")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)

        // initialize
        db = FirebaseFirestore.getInstance()

        designSetUp(view)
        clickedSendButton(view)
        clickedChangePassword(view)

        return view

    }

    /**
     * this page performs 2 function
     * 1) Phone number which change the password of this account
     * 2) Change password page
     */
    private fun designSetUp(view: View) {

        if (is_coming_from_verification_fragment!!) {
            view.layoutForChangePassword.visibility = View.VISIBLE
            view.layoutForSendVerification.visibility = View.GONE
        } else {
            view.layoutForChangePassword.visibility = View.GONE
            view.layoutForSendVerification.visibility = View.VISIBLE
        }

    }

    private fun clickedChangePassword(view: View) {

        view.button_change_password.setOnClickListener {

            val new_password = view.edit_new_password.text.toString().trim()
            val confirm_password = view.edit_confirm_password.text.toString().trim()

            if (checkChangePasswordFields(
                    view,
                    new_password, confirm_password
                )
            ) {
                view.txt_condition_resetPassword.text = "Password is changing..."
                view.rel_progress_resetPassword.visibility = View.VISIBLE
                updatePasswordInDatabase(view, new_password)
            }

        }

    }

    private fun updatePasswordInDatabase(view: View, password: String) {

        db.collection("${type}s")
            .whereEqualTo("phone_number", phone_number)
            .get()
            .addOnCompleteListener { task ->
                var uid = ""
                if (task.isSuccessful && !task.result!!.isEmpty) {
                    for (querySnapshot in task.result!!) {

                        uid = querySnapshot.getString("id").toString()

                    }

                    val hashMap = HashMap<String, Any>()
                    hashMap["password"] = password

                    Toast.makeText(context, "$uid", Toast.LENGTH_SHORT).show()

                    db.collection("${type}s")
                        .document(uid)
                        .update(hashMap)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Password has changed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    view.rel_progress_resetPassword.visibility =
                        View.INVISIBLE // after changed password

                }
            }

    }

    private fun clickedSendButton(view: View) {

        view.send_verificationCode_forResetPassword.setOnClickListener {

            val phoneNumber = view.edit_phone_number.text.toString().trim()
            val spinnerItem = view.spinnerResetPassword.selectedItem.toString()

            if (phoneNumber.length == 9) { // the length of Azerbaijan numbers is 9

                view.rel_progress_resetPassword.visibility = View.VISIBLE

                isPhoneNumberInDatabaseOrNot(
                    db,
                    phoneNumber,
                    "${spinnerItem}s", // in db collection is Clients
                    isInDB = {
                        authenticatePhoneNumber(view, phoneNumber)
                    },
                    isNotInDB = {
                        view.rel_progress_resetPassword.visibility = View.INVISIBLE
                        Toast.makeText(
                            requireContext(),
                            "There is no account with this phone number!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

            } else {
                view.edit_phone_number.error = getString(R.string.number_format_wrong)
            }

        }

    }

    private fun authenticatePhoneNumber(view: View, phoneNumber: String) {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                view.rel_progress_resetPassword.visibility = View.INVISIBLE
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Toast.makeText(context, "${exception.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationID: String,
                resendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                val bundle = Bundle()
                bundle.putString("verificationID", verificationID)
                bundle.putString("phone_number", phoneNumber)
                bundle.putString("type", view.spinnerResetPassword.selectedItem.toString())
                bundle.putBoolean("is_coming_from_reset_fragment", true)
                Navigation.findNavController(view)
                    .navigate(R.id.action_resetPasswordFragment_to_verificationFragment, bundle)
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

                Log.d(TAG, "signInWithCredential: Successfully")

            } else {

                Toast.makeText(requireContext(), it.exception.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }

}
