package com.udacity.warrendproject4.locationreminders.savereminder

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import com.udacity.warrendproject4.R
import com.udacity.warrendproject4.base.BaseFragment
import com.udacity.warrendproject4.base.NavigationCommand
import com.udacity.warrendproject4.databinding.FragmentSaveReminderBinding
import com.udacity.warrendproject4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.warrendproject4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

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
            val reminderDataItem = ReminderDataItem(title, description,location, latitude, longitude)
            Log.d("WWD", "title is " + title)
            Log.d("WWD", "description is" + description)
            Log.d("WWD", "location is " + location)
            Log.d("WWD", "latitude is " + latitude)
            Log.d("WWD", "longitude is " + longitude)
            _viewModel.validateAndSaveReminder(reminderDataItem)
            Log.d("WWD", "called save reminder")
            _viewModel.navigationCommand.value  = NavigationCommand.Back
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
