package vn.edu.usth.uihealthcare

import StepsSensorService
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val automaticStepCountingObserver = Observer { isEnabled: Boolean ->
        when(isEnabled) {
            true -> application.startService(Intent(application, StepsSensorService::class.java))
            false -> application.stopService(Intent(application, StepsSensorService::class.java))
        }
    }

    init {
        Log.d("MM_StepsSensorService1", "MAIN VIEWMODEL INIT")
        automaticStepCountingObserver.onChanged(true)
    }
}