package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.InvestmentApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as InvestUpApplication
    val factory = MainViewModelFactory(app.repository)
    val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

    setContent {
      MyApplicationTheme {
        InvestmentApp(viewModel = viewModel)
      }
    }
  }
}

