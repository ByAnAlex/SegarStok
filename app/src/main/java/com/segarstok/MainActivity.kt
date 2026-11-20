package com.segarstok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.segarstok.core.design.theme.SegarStokTheme
import com.segarstok.core.navigation.SegarStokAppMain

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SegarStokTheme {
                SegarStokAppMain()
            }
        }
    }
}
