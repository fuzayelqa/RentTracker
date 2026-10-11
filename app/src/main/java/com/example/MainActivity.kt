package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.RentTrackerViewModel
import com.example.ui.navigation.RentTrackerApp
import com.example.ui.theme.RentTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RentTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RentTrackerTheme {
                RentTrackerApp(viewModel = viewModel)
            }
        }
    }
}
