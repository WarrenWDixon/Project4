package com.udacity.warrendproject4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.warrendproject4.BuildConfig
import com.udacity.warrendproject4.R
import com.udacity.warrendproject4.base.BaseFragment
import com.udacity.warrendproject4.base.NavigationCommand
import com.udacity.warrendproject4.databinding.FragmentSaveReminderBinding
import com.udacity.warrendproject4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.warrendproject4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.warrendproject4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
const val REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 35
const val GEOFENCE_RADIUS_IN_METERS = 100f
val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    // fragment logic -------------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        Log.d("WWD", "in SaveReminderFragment onCreateView")

        binding.viewModel = _viewModel
        binding.reminderDescription.onFocusChangeListener = View.OnFocusChangeListener { p0, p1->
                Log.d("WWD", "in OnFocusChangeListener p0: " + p0 + "  p1: " + p1)
               hideKeyboard()
        }
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    private fun hideKeyboard() {
        val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Log.d("WWD", "in hideKeyboard")
        if (inputManager.isAcceptingText) {
            Log.d("WWD", "in isAcceptingText, call hideSoftInput")
            inputManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        } else {
            Log.d("WWD", "isAccepting Text else block")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        Log.d("WWD", "in SaveReminderFragment onViewCreated")
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        Log.d("WWD", "set on click listener for save reminder")

        binding.saveReminder.setOnClickListener {
            Log.d("WWD", " in setOnClickListener")
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            reminderDataItem = ReminderDataItem(title, description,location, latitude, longitude)
            Log.d("WWD", "title is " + title)
            Log.d("WWD", "description is" + description)
            Log.d("WWD", "location is " + location)
            Log.d("WWD", "latitude is " + latitude)
            Log.d("WWD", "longitude is " + longitude)
            _viewModel.validateAndSaveReminder(reminderDataItem)
            Log.d("WWD", "called save reminder")
            _viewModel.navigationCommand.value  = NavigationCommand.Back
            if (!checkBackgroundLocationPermissionApproved()){
                Log.d("WWD", "need background permission")
                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                    requestBackgroundPermission()
            }
            else {
                Log.d("WWD", "setOnClickListener got permission call checkDeviceLocationSettingsAndStartGeofence")
                checkDeviceLocationSettingsAndStartGeofence(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    // permission logic -------------------------------------------------------------------------------

    private fun checkBackgroundLocationPermissionApproved() : Boolean {
        Log.d("WWD", "in checkBackgroundLocationPermissionApproved")

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                true
            }
        Log.d("WWD", "in checkBackgroundLocationPermissionApproved return " + backgroundPermissionApproved)
        return backgroundPermissionApproved
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPermission() {
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val requestCode = REQUEST_BACKGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        Log.d("WWD", "requestBackgroundPermission")

        ActivityCompat.requestPermissions(
            requireActivity(),
            permissionsArray,
            requestCode
        )
        Log.d("WWD", "requestBackgroundPermission requested permission")
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("WWD", "in onRequestPermissionResult")

        if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Log.d("WWD", "in onRequestPermissionResult permission denied")
            Snackbar.make(binding.fragmentSave, R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            Log.d("WWD", "onRequestPermissionResult permission good call checkDeviceLocationSettingsAndStartGeofence")
            checkDeviceLocationSettingsAndStartGeofence()
        }
        Log.d("WWD", "end onRequestPermissionResult")
    }

    // geofence logic -------------------------------------------------------------------------------

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.reminder.action.ACTION_GEOFENCE_EVENT"
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        Log.d("WWD", "in geofencePendingIntent")
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        Log.d("WWD", "in checkDeviceLocationSettingsAndStartGeofence")
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            Log.d("WWD", "in checkDeviceLocationSettingsAndStartGeofence failure listener")
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("WWD", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.fragmentSave,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            Log.d("WWD", "in checkDeviceLocationSettingsAndStartGeofence add OnCompleteListener")
            if ( it.isSuccessful ) {
                Log.d("WWD", "in checkDeviceLocationSettingsAndStartGeofence onComplete success call addGeofenceReminder")
                addGeofenceForReminder()
            }
        }
        Log.d("WWD", "end checkDeviceLocationSettingsAndStartGeofence")
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForReminder() {
        Log.d("WWD", "in addGeofenceForReminder")
        val reminderLatitude = reminderDataItem.latitude ?: 0.0
        val reminderLongitude = reminderDataItem.longitude ?: 0.0
        Log.d("WWD", "in addGeofenceForReminder lat: " + reminderLatitude + " long " + reminderLongitude)
        Log.d("WWD", "in addGeofenceForReminder id is " + reminderDataItem.id)

        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(reminderLatitude, reminderLongitude, GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
            Log.d("WWD", "addGeofenceForReminder built geofence")

        Log.d("WWD", "in addGeofenceForReminder declared geofence")
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        Log.d("WWD", "addGeofenceForReminder built geofenceRequest")

       geofencingClient?.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(requireActivity(), R.string.geofences_added,
                    Toast.LENGTH_SHORT)
                    .show()
                Log.d("WWD", "geofence added ")
            }
            addOnFailureListener {
                Log.d("WWD", "in addGeofenceForReminder failed to add geofence")
                Toast.makeText(requireActivity(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.w("WWD", it.message!!)
                }
            }
        }
        Log.d("WWD", "end addGeofenceForReminder")
    }
}
