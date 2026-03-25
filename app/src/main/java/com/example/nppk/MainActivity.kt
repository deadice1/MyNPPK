package com.example.nppk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Теперь вызываем функцию из файла NppkRoot.kt
            NppkMainContent()
        }
    }
}