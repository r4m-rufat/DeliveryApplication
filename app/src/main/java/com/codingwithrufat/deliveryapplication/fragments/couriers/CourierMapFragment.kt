package com.codingwithrufat.deliveryapplication.fragments.couriers

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.models.users_detail.ClientDetail
import com.codingwithrufat.deliveryapplication.utils.constants.MAP_REQUEST_CODE
import com.codingwithrufat.deliveryapplication.utils.constants.TAG
import com.codingwithrufat.deliveryapplication.utils.prefence.MyPrefence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_courier_map.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CourierMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var currentLocation: Location
    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var reference = firebaseDatabase.reference
    private lateinit var preferenceManager: MyPrefence
    private lateinit var googleMap: GoogleMap
    private var client_id: String? = null
    private var courier_id: String? = null
    private var food_id: String? = null
    private var client_latitude: Double? = null
    private var client_longitude: Double? = null
    private var courier_latitude: Double? = null
    private var courier_longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = MyPrefence(requireContext())
        client_id = requireActivity().intent.getStringExtra("client_id")
        food_id = requireActivity().intent.getStringExtra("food_id")
        courier_id = preferenceManager.getString("user_id")
        if (!client_id.isNullOrEmpty()){
            preferenceManager.setString("temporary_client_id", client_id!!)
        } else{
            preferenceManager.setString("temporary_client_id", "")
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_courier_map, container, false)

        val fusedLocationProvider =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.client_map) as SupportMapFragment


        if (isLocationPermissionGranted()) {
            val locationTask = fusedLocationProvider.lastLocation
            locationTask.addOnSuccessListener { location ->

                if (location != null) {

                    currentLocation = location
                    mapFragment.getMapAsync(this)

                }

            }

            CoroutineScope(Main).launch {

                delay(5000L)

                repeat(100_000) {

                    locationTask.addOnSuccessListener { location ->
                        if (location != null) {

                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(location.latitude, location.longitude))
                                    .title("Client")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_arrow))
                                    .rotation(location.bearing) // rotate icon to the destination
                            )

                        }
                    }

                    delay(3000L)

                }
            }

        }

        visibilityAndEventOfAcceptButton(view)

        return view

    }


    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MAP_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    private fun getClientLatLongFromDB(onComingClientData: (clientDetail: ClientDetail) -> Unit) {

        if (client_id != null) {
            reference.child("Clients")
                .child(client_id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            onComingClientData(snapshot.getValue(ClientDetail::class.java)!!)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

    }

    override fun onMapReady(map: GoogleMap) {
        map.isIndoorEnabled = false
        map.isBuildingsEnabled = false
        map.isTrafficEnabled = true

        googleMap = map

        courier_latitude = currentLocation.latitude
        courier_longitude = currentLocation.longitude

        getClientLatLongFromDB { client ->

            client_latitude = client.client_latitude!!
            client_longitude = client.client_longitude!!

            map.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            client.client_latitude!!.toDouble(),
                            client.client_longitude!!.toDouble()
                        )
                    )
                    .title("Client")
            )

        }

        map.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Courier")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_arrow))
                .rotation(currentLocation.bearing) // rotate icon to the destination
        )

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentLocation.latitude,
                    currentLocation.longitude
                ), 15f
            )
        )
    }

    private fun visibilityAndEventOfAcceptButton(view: View){

        if (!preferenceManager.getString("temporary_client_id").isNullOrEmpty()){
            if (!preferenceManager.getBoolen("accept_visibility")){
                view.button_accept_changes.text = "Delivered"
            }
        }else{
            view.button_accept_changes.visibility = GONE
        }

        view.button_accept_changes.setOnClickListener {
            clickedAcceptButtonChanges(view)
        }

    }

    private fun clickedAcceptButtonChanges(view: View) {

        if (view.button_accept_changes.text == "Accept Order"){
            updateCourierFieldsInDBWhenAccept(view)
            updateFoodFieldsInDB()
            preferenceManager.setBoolen("accept_visibility", false)
        }else{
            Toast.makeText(requireContext(), "basildi", Toast.LENGTH_SHORT).show()
            updateCourierFieldsInDBWhenDelivered(view)
            client_id = null // order is delivered then client_id must be null
            view.button_accept_changes.visibility = GONE
        }

    }

    private fun updateCourierFieldsInDBWhenAccept(view: View) {
        var updateChildrenHM = HashMap<String, Any>()
        updateChildrenHM["food_id"] = food_id!!
        updateChildrenHM["busy"] = true
        getClientLatLongFromDB { client ->
            updateChildrenHM["destination_latitude"] = client.client_latitude!!
            updateChildrenHM["destination_longitude"] = client.client_longitude!!
            reference.child("Couriers").child(courier_id!!).updateChildren(updateChildrenHM)
                .addOnSuccessListener {
                    view.button_accept_changes.text = "Delivered"
                }
        }

    }

    private fun updateFoodFieldsInDB() {

        var updateChildrenHM = HashMap<String, Any>()
        updateChildrenHM["source_latitude"] = courier_latitude!!
        updateChildrenHM["source_longitude"] = courier_longitude!!
        updateChildrenHM["order_time"] = getCurrentTime()

        reference.child("Foods").child(food_id!!).updateChildren(updateChildrenHM)
            .addOnSuccessListener {
                Log.d(TAG, "updateFoodFieldsInDB: Successfully updated all food fields")
            }.addOnFailureListener {
                Log.d(TAG, "updateFoodFieldsInDB: Error -> ${it.message}")
            }

    }

    private fun updateCourierFieldsInDBWhenDelivered(view: View) {
        var updateChildrenHM = HashMap<String, Any>()
        updateChildrenHM["food_id"] = ""
        updateChildrenHM["busy"] = false

        updateChildrenHM["destination_latitude"] = 0.0f
        updateChildrenHM["destination_longitude"] = 0.0f
        reference.child("Couriers").child(courier_id!!).updateChildren(updateChildrenHM)
            .addOnSuccessListener {
                view.button_accept_changes.text = "Accept Order"
            }
    }

    private fun getCurrentTime(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return simpleDateFormat.format(Date())
    }

}