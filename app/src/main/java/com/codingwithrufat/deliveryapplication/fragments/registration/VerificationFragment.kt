package com.codingwithrufat.deliveryapplication.fragments.registration

import android.content.Intent
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
import com.codingwithrufat.deliveryapplication.activities.ClientActivity
import com.codingwithrufat.deliveryapplication.activities.CourierActivity
import com.codingwithrufat.deliveryapplication.models.users.User
import com.codingwithrufat.deliveryapplication.models.users_detail.ClientDetail
import com.codingwithrufat.deliveryapplication.models.users_detail.CourierDetail
import com.codingwithrufat.deliveryapplication.utils.constants.ONE_MINUTE_TIME
import com.codingwithrufat.deliveryapplication.utils.constants.SECOND_MS
import com.codingwithrufat.deliveryapplication.utils.constants.TAG
import com.codingwithrufat.deliveryapplication.utils.location.FindCurrentLocation
import com.codingwithrufat.deliveryapplication.utils.prefence.MyPrefence
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
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

    //variables for courier database
    private var courier_latitude: Double? = null
    private var courier_longitude: Double? = null
    private var busy: Boolean? = false
    private var food_id: String? = null
    private var destination_latitude: Double? = null
    private var destination_longitude: Double? = null
    var devicestoken = arrayListOf<String>()

    //variables for client database
    private var ordered_food_id: String? = null
    var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var data_ref = firebaseDatabase.reference

    // objects
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var mylocation: FindCurrentLocation
    private lateinit var prefence: MyPrefence
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            is_coming_from_reset_fragment = it.getBoolean("is_coming_from_reset_fragment")
            if (!is_coming_from_reset_fragment!!) {
                username = it.getString("username")
                password = it.getString("password")
            }
            verificationID = it.getString("verificationID")
            type = it.getString("type")
            phone_number = it.getString("phone_number")
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
        mylocation = FindCurrentLocation(context)
        prefence = MyPrefence(context)

        clickedVerifyProceedButton(view)
        setPhoneNumber(view)
        clickedResendOTP(view)
        countdownTimer(view)
        clickedbackFromVerification(view)

        return view
    }

    private fun clickedbackFromVerification(view: View) {
        view.ic_backFromVerification.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_verificationFragment_to_registerFragment)
        }
    }

    private fun clickedVerifyProceedButton(view: View) {
        view.button_verifyProceed.setOnClickListener {
            view.rel_progress_verification.visibility = View.VISIBLE
            if (is_coming_from_reset_fragment!!) {
                val credential = PhoneAuthProvider.getCredential(
                    verificationID!!,
                    view.etxt_pin_entry.text.toString()
                )
                verifyPhoneNumber(view, credential)

            } else {
                verifyPhoneNumberWithCode(
                    view,
                    verificationID,
                    view.etxt_pin_entry.text.toString()
                ) // coming from sign up page
            }
        }
    }

    private fun signInWithPhoneAuthCredential(view: View, credential: PhoneAuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val id = task.result?.user!!.uid

                    prefence.setString("user_id", id) // user_id store the internal storage

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
                                data_setclients()
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Error is ${it.message}")
                            }

                    } else {

                        firebaseFirestore.collection(getString(R.string.couriers))
                            .document(id)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Task is successful")
                                data_setcouriers()
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "signInWithPhoneAuthCredential: Error is ${it.message}")
                            }
                    }


                    view.rel_progress_verification.visibility = View.INVISIBLE

                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        view.rel_progress_verification.visibility = View.INVISIBLE
                        Toast.makeText(context, "verification code is invalid", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun data_setclients() {
        val client_id = prefence.getString("user_id")
        mylocation.fetch(context)
        val client_latitude = prefence.getString("latitude")?.toDouble()
        val client_longitude = prefence.getString("longitude")?.toDouble()
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {

            ordered_food_id = ""

            val clients = ClientDetail(
                client_id,
                it.result.token,
                client_latitude,
                client_longitude,
                ordered_food_id
            )
            data_ref.child("Clients").child(client_id.toString()).setValue(clients)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully set")
                        startActivity(Intent(requireContext(), ClientActivity::class.java))
                    } else {
                        Log.d(TAG, task.exception.message.toString())
                    }
                }

        }

    }


    private fun data_setcouriers() {

        val courier_id = prefence.getString("user_id")
        courier_latitude = prefence.getString("latitude")?.toDouble()
        courier_longitude = prefence.getString("longitude")?.toDouble()
        food_id = ""
        destination_latitude = 0.0
        destination_longitude = 0.0
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (courier_latitude != null && courier_longitude != null) {
                val couriers = CourierDetail(
                    courier_id,
                    it.result.token,
                    courier_latitude,
                    courier_longitude,
                    busy,
                    food_id,
                    destination_latitude,
                    destination_longitude
                )
                data_ref.child("Couriers").child(courier_id.toString()).setValue(couriers)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "Successfully set")
                            startActivity(Intent(requireContext(), CourierActivity::class.java))
                        } else {
                            Log.d("TAG", task.exception.message.toString())
                        }

                    }
            }
        }
    }


    private fun verifyPhoneNumber(view: View, credential: PhoneAuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    view.rel_progress_verification.visibility = View.INVISIBLE

                    val bundle = Bundle()
                    bundle.putString("phone_number", phone_number)
                    bundle.putString("type", type)
                    bundle.putBoolean("is_coming_from_verification_fragment", true)
                    Navigation.findNavController(view)
                        .navigate(R.id.action_verificationFragment_to_resetPasswordFragment, bundle)

                } else {

                    view.rel_progress_verification.visibility = View.INVISIBLE

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context, "Verification code is invalid", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            }
            .addOnFailureListener {
                view.rel_progress_verification.visibility = View.INVISIBLE
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

    private fun countdownTimer(view: View) {
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
                    view.txt_resend_otp.text =
                        getString(R.string.bt_resend) // if counter time is finished then text is set

                    view.txt_resend_otp.typeface = Typeface.DEFAULT // changed again to default
                }

                delay(ms)

            }

        }
    }

    private fun verifyPhoneNumberWithCode(view: View, verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(view, credential)
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