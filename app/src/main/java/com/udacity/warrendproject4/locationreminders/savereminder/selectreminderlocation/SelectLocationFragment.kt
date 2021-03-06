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
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions
import com.google.android.gms.common.api.ResolvableApiException
import com.udacity.warrendproject4.base.NavigationCommand
import com.udacity.warrendproject4.locationreminders.savereminder.SaveReminderFragmentDirections
import kotlinx.coroutines.selects.select
import java.lang.Exception


import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var selectedLocation : LatLng
    private lateinit var saveButton: Button

    companion object {
        private lateinit var selectedLocation : LatLng
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        Log.d("WWD", "in SelectLocationFragment")
        binding.locationSaveBtn.setOnClickListener {
            Log.d("WWD", "in save on click listener")
            _viewModel.navigationCommand.value  = NavigationCommand.Back
        }

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
        fusedLocationProvider = FusedLocationProviderClient(context!!)
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
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("WWD", "in on MapReady")
        map = googleMap
        val zoomLevel = 15f
        Log.d("WWD", "in on MapReady before enableMyLocation")
        enableMyLocation()
        Log.d("WWD", "now call check DeviceLocation")
        setMapLongClick(map)
        setMapStyle(map)
        setPoiClick(map)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("WWD", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("WWD", "Can't find style. Error: ", e)
        }
    }
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        Log.d("WWD", "select location enable my location")
        if (isPermissionGranted()) {
            Log.d("WWD", "select location permission granted")
            map.setMyLocationEnabled(true)
            try {
                val locationResult = fusedLocationProvider.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) {
                    Log.d("WWD", "select location enableMyLocation complete listener")
                    if (it.isSuccessful) {
                        Log.d("WWD", "select location enableMyLocation complete listener isSuccessful move camera")
                        // Set the map's camera position to the current location of the device.
                        var zoomLocation = it.result
                        var zoomLocationLatLong = LatLng(zoomLocation!!.latitude, zoomLocation.longitude)
                        // making/creating a marker at where the user is
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomLocationLatLong, 15f)) // zooming into their location
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(zoomLocationLatLong)
                                .title("You Are Here")
                        )
                        marker?.showInfoWindow()
                    }
                }
            }
            catch (e: Exception){
                Log.e("WWD", e.message.toString())
            }

        }
        else {
            Log.d("WWD", "select location permission not granted so request permission")
            requestPermissions(
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
        Log.d("WWD", "select location onPermissionsResult")
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            Log.d("WWD", "select location onPermissionsResult request code good")
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("WWD", "select location onPermissionsResult permission granted call enableMyLocation")
                enableMyLocation()
            }
        }
    }

    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            selectedLocation = latLng
            _viewModel.latitude.value = latLng.latitude
            _viewModel.longitude.value = latLng.longitude
            _viewModel.reminderSelectedLocationStr.value = "Selected Location"


            // A Snippet is Additional text that's displayed below the title.
            Log.d("WWD", "in long click lat: " + latLng.latitude + "   long: " + latLng.longitude)
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            Log.d("WWD", "calling add Marker " + snippet)
            val marker = map.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.reminder_location))
                    .snippet(snippet))
            marker?.showInfoWindow()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            Log.d("WWD", "in setOnPoiClickListener")
            selectedLocation = poi.latLng
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            if (poiMarker != null) {
                poiMarker.showInfoWindow()
            }
            _viewModel.latitude.value = selectedLocation.latitude
            _viewModel.longitude.value = selectedLocation.longitude
            _viewModel.reminderSelectedLocationStr.value = poi.name
            Log.d("WWD", "in setPoiClick location is " + poi.name)
        }
    }



}
