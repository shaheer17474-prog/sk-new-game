package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AviatorViewModel
import com.example.ui.AviatorViewModelFactory
import com.example.ui.MainAppContent
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao())
        
        // Instantiate the ViewModel using the Custom Factory
        val viewModelFactory = AviatorViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AviatorViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}
