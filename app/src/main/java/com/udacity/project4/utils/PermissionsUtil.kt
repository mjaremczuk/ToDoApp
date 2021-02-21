package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionsUtil {

    companion object {
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 3
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        private val runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    }

    var onPermissionGranted: () -> Unit = {}
    var onPermissionDenied: () -> Unit = {}

    fun requestPermissions(fragment: Fragment) {
        if (foregroundAndBackgroundLocationPermissionApproved(fragment.requireContext())) {
            onPermissionGranted()
//            map.isMyLocationEnabled = true
//            animateToCurrentLocation()
        } else {
            requestForegroundAndBackgroundLocationPermissions(fragment)
//            fragment.requestPermissions(
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
        }
    }


    private fun isPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            // Permission denied.
            onPermissionDenied()
        } else {
            onPermissionGranted()
        }
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                onPermissionGranted()
//            }
//        }
    }


    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions(fragment: Fragment) {
        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        fragment.requestPermissions(
            permissionsArray,
            resultCode
        )
    }
}