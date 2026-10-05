package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RentTrackerViewModel
import com.example.ui.navigation.RentTrackerApp
import com.example.ui.theme.RentTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RentTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
            // Support darkTheme detection or settings if provided
            RentTrackerTheme {
                RentTrackerApp(viewModel = viewModel)
            }
        }
    }
}
