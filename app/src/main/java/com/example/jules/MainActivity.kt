package com.example.jules

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.jules.network.ApiService
import com.example.jules.ui.list.MyListScreen
import com.example.jules.ui.list.MyViewModel
import com.example.jules.ui.list.MyViewModelFactory
import com.example.jules.ui.theme.JulesTheme // Assuming this is your theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Typically, ApiService instance would come from a dependency injection framework
        val apiService = ApiService.create()
        val viewModelFactory = MyViewModelFactory(apiService)
        val myViewModel: MyViewModel by viewModels { viewModelFactory }

        setContent {
            JulesTheme { // Apply your app's theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyListScreen(viewModel = myViewModel)
                }
            }
        }
    }
}