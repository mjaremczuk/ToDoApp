package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.PermissionsUtil
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    companion object {
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 5
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.todoapp.action.ACTION_GEOFENCE_EVENT"
    }

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    lateinit var geofencingClient: GeofencingClient
    private val permissionUtil = PermissionsUtil()

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        println("GEOFENCE incoming pending intent")
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSaveReminderBinding.inflate(layoutInflater, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        _viewModel.reminderAdded.observe(viewLifecycleOwner, Observer {
            it?.let {
                addGeofence(it)
            }
        })

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            _viewModel.showErrorMessage
            permissionUtil.onPermissionGranted = {
                checkDeviceLocationSettingsAndStartGeofence()
            }
            permissionUtil.requestPermissions(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionUtil.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    private fun getGeofencingRequest(geofence: Geofence) = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
        .addGeofences(listOf(geofence))
        .build()

    private fun createGeofence(reminderDataItem: ReminderDataItem) = Geofence.Builder()
        .setRequestId(reminderDataItem.id)
        .setCircularRegion(
            reminderDataItem.latitude!!,
            reminderDataItem.longitude!!,
            100.0F
        )
        // Set the expiration duration of the geofence. This geofence gets automatically
        // removed after this period of time.
        .setExpirationDuration(-1)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        "SAVE REMIDNER",
                        "Error geting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    binding.container,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                _viewModel.validateAndSaveReminder(getReminder())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderItem: ReminderDataItem) {
        geofencingClient.addGeofences(
            getGeofencingRequest(createGeofence(reminderItem)),
            geofencePendingIntent
        )?.run {
            addOnSuccessListener {
                _viewModel.moveBackToReminderList()
                println("GEOFENCE added")
            }
            addOnFailureListener {
                _viewModel.showErrorMessage.postValue(getString(R.string.geofences_not_added) + System.lineSeparator() + it.message)
                _viewModel.moveBackToReminderList()
                println("GEOFENCE FAILED $it\n ${it.message}")
            }
        }
    }

    private fun getReminder(): ReminderDataItem {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        return ReminderDataItem(
            title,
            description,
            location,
            latitude,
            longitude
        )
    }
}
