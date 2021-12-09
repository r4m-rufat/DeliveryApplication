package com.codingwithrufat.deliveryapplication.fragments.couriers

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.models.users_detail.ClientDetail
import com.codingwithrufat.deliveryapplication.utils.constants.MAP_REQUEST_CODE
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CourierMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var currentLocation: Location
    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var reference = firebaseDatabase.reference
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_courier_map, container, false)

        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.client_map) as SupportMapFragment
        if (isLocationPermissionGranted()){
            val locationTask = fusedLocationProvider.lastLocation
            locationTask.addOnSuccessListener { location ->
                if (location != null){

                    currentLocation = location
                    mapFragment.getMapAsync(this)

                }
            }

            CoroutineScope(Main).launch {

                delay(5000L)

                repeat(100_000) {

                    locationTask.addOnSuccessListener { location ->
                        if (location != null){

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

        return view;

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

    private fun getClientLatLongFromDB(onComingClientData: (clientDetail: ClientDetail) -> Unit){

        reference.child("Clients")
            .child("AC9a0goFM9TQSVStOPHXzKWJAvo1")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){
                        onComingClientData(snapshot.getValue(ClientDetail::class.java)!!)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    override fun onMapReady(map: GoogleMap) {
        map.isIndoorEnabled = false
        map.isBuildingsEnabled = false
        map.isTrafficEnabled = true

        googleMap = map

        getClientLatLongFromDB { client ->

                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(client.client_latitude!!.toDouble(), client.client_longitude!!.toDouble()))
                        .title("Courier")
                )

        }

        map.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Client")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_arrow))
                .rotation(currentLocation.bearing) // rotate icon to the destination
        )

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 15f))
    }

}