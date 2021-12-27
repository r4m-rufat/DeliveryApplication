package com.codingwithrufat.deliveryapplication.fragments.clients

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
import com.codingwithrufat.deliveryapplication.models.users_detail.CourierDetail
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


/**
 * A simple [Fragment] subclass.
 * Use the [ClientMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClientMapFragment : Fragment(), OnMapReadyCallback{

    private lateinit var currentLocation: Location
    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var reference = firebaseDatabase.reference

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
        val view = inflater.inflate(R.layout.fragment_client_map, container, false)

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
        }

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

    private fun getCourierLatLongFromDB(onComingCourierData: (list: MutableList<CourierDetail>) -> Unit){

        reference.child("Couriers")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){
                        var list = mutableListOf<CourierDetail>()
                        snapshot.children.forEach { singleSnapshot ->
                            list.add(singleSnapshot.getValue(CourierDetail::class.java)!!)
                        }
                        onComingCourierData(list)
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

        getCourierLatLongFromDB { list ->

            list.forEach {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.courier_latitude!!.toDouble(), it.courier_longitude!!.toDouble()))
                        .title("Courier")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motorcycle))
                        .rotation(currentLocation.bearing) // rotate icon to the destination
                )
            }

        }
        map.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Client")
        )
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 15f))
    }




}