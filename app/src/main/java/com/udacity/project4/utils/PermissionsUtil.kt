package com.udacity.project4.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
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

    enum class Permission(val permission: String) {
        FOREGROUND_LOCATION(Manifest.permission.ACCESS_FINE_LOCATION),
        BACKGROUND_PERMISSION(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    var onPermissionGranted: () -> Unit = {}
    var onPermissionDenied: () -> Unit = {}

    fun requestPermissions(fragment: Fragment, permission: Permission) {
        if (isPermissionGranted(fragment.requireContext(), permission.permission)) {
            onPermissionGranted()
        } else {
            requestPermission(fragment, permission)
        }
    }


    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(fragment: Fragment, permission: Permission) {
        when (permission) {
            Permission.FOREGROUND_LOCATION -> fragment.requestPermissions(
                arrayOf(permission.permission),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
            Permission.BACKGROUND_PERMISSION -> requestBackgroundPermission(fragment)
        }
    }

    fun requestPermission(fragment: Fragment) {
        if (runningQOrLater) {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            )
        } else {
            onPermissionGranted()
        }
    }

    private fun requestBackgroundPermission(fragment: Fragment) {
        if (runningQOrLater) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    fragment.requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                onPermissionDenied()
            } else {
                fragment.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                )
            }
        } else {
            onPermissionGranted()
        }
    }

    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE,
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE -> handlePermissionResult(
                permissions,
                grantResults
            )
        }
    }

    private fun handlePermissionResult(
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            onPermissionDenied()
        } else {
            onPermissionGranted()
        }
    }
}