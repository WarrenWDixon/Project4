package com.udacity.warrendproject4.locationreminders.savereminder.selectreminderlocation


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.warrendproject4.R
import com.udacity.warrendproject4.base.BaseFragment
import com.udacity.warrendproject4.databinding.FragmentSelectLocationBinding
import com.udacity.warrendproject4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.warrendproject4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions
import com.google.android.gms.common.api.ResolvableApiException



import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        Log.d("WWD", "in SelectLocationFragment")


        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
        Log.d("WWD", "set mapFragment")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        Log.d("WWD", "call getMapAsync")
        mapFragment.getMapAsync(this)


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("WWD", "in on MapReady")
        map = googleMap
        val zoomLevel = 15f
        Log.d("WWD", "in on MapReady before setMapLongClick")
        Log.d("WWD", "in on MapReady before enableMyLocation")
        enableMyLocation()
        Log.d("WWD", "now call check DeviceLocation")
        setMapLongClick(map)
    }
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            Log.d("WWD", "in long click lat: " + latLng.latitude + "   long: " + latLng.longitude)
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            Log.d("WWD", "calling add Marker " + snippet)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )

        }
    }


}
