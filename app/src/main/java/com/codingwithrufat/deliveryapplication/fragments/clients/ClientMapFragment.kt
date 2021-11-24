package com.codingwithrufat.deliveryapplication.fragments.clients

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.codingwithrufat.deliveryapplication.R
import com.codingwithrufat.deliveryapplication.utils.constants.MAP_REQUEST_CODE
import com.google.android.gms.gcm.Task
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


/**
 * A simple [Fragment] subclass.
 * Use the [ClientMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClientMapFragment : Fragment(), OnMapReadyCallback{

    private lateinit var currentLocation: Location

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

    override fun onMapReady(map: GoogleMap) {
        val baku = LatLng(40.4, 49.8)
        map.addMarker(
            MarkerOptions()
                .position(baku)
                .title("Courier")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_motorcycle))
        )
        map.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Client")
        )
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 12f))
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
}