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
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions


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
        val home = LatLng(33.134059572767, -96.78572221255695)
        // Add a marker in Sydney and move the camera
        val zoomLevel = 15f
        map.addMarker(MarkerOptions().position(home).title("Marker at Home"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoomLevel))
        Log.d("WWD", "in on MapReady before setMapLongClick")
       // setMapLongClick(map)
        Log.d("WWD", "in on MapReady before setPoiClick")
        //setPoiClick(map)
        Log.d("WWD", "in on MapReady before setMapStyle")
        //setMapStyle(map)
        Log.d("WWD", "in on MapReady before enableMyLocation")
        //enableMyLocation()
        isPermissionGranted()
    }

    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            Log.d("WWD", "calling add Marker")
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            )

        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            if (poiMarker != null) {
                poiMarker.showInfoWindow()
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    getApplicationContext(),
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
        Log.d("WWD", "isPermissionGranted")
        val flag = ContextCompat.checkSelfPermission(
           requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION)
        Log.d("WWD", " after checkSelfPermission flag is " + flag)
        return (flag === PackageManager.PERMISSION_GRANTED)
    }

    /* private fun enableMyLocation() {
        android.util.Log.d("WWD", "in enableMyLocation")
        var myFlag = isPermissionGranted()
        Log.d("WWD", "return of isPermissionGranted is " + myFlag);
        //if (isPermissionGranted()) {
        if (myFlag) {
            Log.d("WWD", "isPermissionsGranted true block")
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                    android.util.Log.d("WWD", "in checkSelfPermissions block")
                ContextCompat.requestPermissions(
                    getActivity(),
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
                return
            }
            Log.d("WWD", "call map.setMyLocationEnabled")
            map.setMyLocationEnabled(true)
        }
        else {
            Log.d("WWD", "isPermissionsGranted false block")
            ActivityCompat.requestPermissions(
                getApplicationContext(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }
        Log.d("WWD", "end of enableMyLocation")
    } */

    /* private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                getApplicationContext(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            map.isMyLocationEnabled = true
        }
    } */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("WWD", "onRequestPermissionsResult")
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            Log.d("WWD", "requset code match")
           /* if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                android.util.Log.d("WWD", "onRequestPermissionsResult permission granted")
                //enableMyLocation()
            } */
        }
    }


}
