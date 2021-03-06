package com.udacity.warrendproject4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.udacity.warrendproject4.locationreminders.data.dto.ReminderDTO
import com.udacity.warrendproject4.locationreminders.data.dto.Result
import com.udacity.warrendproject4.locationreminders.data.local.LocalDB
import com.udacity.warrendproject4.locationreminders.data.local.RemindersDao
import com.udacity.warrendproject4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.warrendproject4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.warrendproject4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            Log.d("WWD", "in top level enqueue work")
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification
        Log.d("WWD", "in onHandle work")
        val geofenceId = intent.getStringExtra("GEOFENCE_ID")
        Log.d("WWD", "the fence id is $geofenceId")
        val serviceScope = CoroutineScope(Dispatchers.Default)
        serviceScope.launch {
            getReminderFromId(geofenceId ?: "")
        }


    }

    suspend fun getReminderFromId(geofenceId: String) {
        val reminderDao = LocalDB.createRemindersDao(applicationContext)
        val repository = RemindersLocalRepository(reminderDao)
        withContext(Dispatchers.IO) {
            var result = repository.getReminder(geofenceId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(requestId : String ) {
        val requestId = ""

        //Get the local repository instance
        val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}