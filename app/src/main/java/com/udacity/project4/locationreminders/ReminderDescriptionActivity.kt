package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.SphericalUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import kotlin.math.sqrt


/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private var _binding: ActivityReminderDescriptionBinding? = null

    val binding: ActivityReminderDescriptionBinding
        get() = _binding!!

    val reminderDataItem: ReminderDataItem by lazy {
        intent.getSerializableExtra(
            EXTRA_ReminderDataItem
        ) as ReminderDataItem
    }

    val _viewModel: SaveReminderViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.reminderDataItem = reminderDataItem
        binding.lifecycleOwner = this
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val snackbar = Snackbar.make(
            binding.root,
            "Do you want to remove reminder?",
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction("Remove") {
            _viewModel.removeReminder(reminderDataItem)
            LocationServices.getGeofencingClient(this)
                .removeGeofences(listOf(reminderDataItem.id))
            snackbar.dismiss()
        }
        snackbar.show()
    }

    override fun onMapReady(map: GoogleMap) {
        setMapStyle(map)
        val reminderLocation = LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(reminderLocation, 16F))
        map.setLatLngBoundsForCameraTarget(toBounds(reminderLocation, 100.0))
    }

    private fun toBounds(center: LatLng?, radiusInMeters: Double): LatLngBounds {
        val distanceFromCenterToCorner = radiusInMeters * sqrt(2.0)
        val southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(southwestCorner, northeastCorner)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            println("Can't find style. Error: $e")
        }
    }
}
