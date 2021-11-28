package com.codingwithrufat.deliveryapplication.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.codingwithrufat.deliveryapplication.utils.constants.REQUEST_CODE
import com.codingwithrufat.deliveryapplication.utils.prefence.MyPrefence
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class FindCurrentLocation(context: Context?) {
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    var longitude:Double?=null
    var latitude:Double?=null
    var prefence=MyPrefence(context)

   fun fetch(context: Context?){
        Dexter.withActivity(context as Activity?)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        buildLocationRequest()
                        buildLocationCallback()
                        if (ActivityCompat.checkSelfPermission(
                                context!!,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                context!!,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context!!, arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ),REQUEST_CODE
                            )
                            return
                        }
                        fusedLocationProviderClient =
                            LocationServices.getFusedLocationProviderClient(context!!)
                        fusedLocationProviderClient?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.myLooper()
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                }
            }).check()

    }

 fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult:LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.getLastLocation() != null) {

                    longitude=locationResult.getLastLocation().getLongitude()
                    latitude=locationResult.getLastLocation().getLatitude()
                    prefence.setString("longitude", longitude.toString())
                    prefence.setString("latitude",latitude.toString())

                    Log.d(
                        "Location",
                        locationResult.getLastLocation().getLatitude()
                            .toString() + "/" + locationResult.getLastLocation().getLongitude()
                    )
                }
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }
}