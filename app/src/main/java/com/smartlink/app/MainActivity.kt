package com.smartlink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smartlink.app.ui.SmartLinkApp
import com.smartlink.app.ui.theme.SmartLinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartLinkTheme {
                SmartLinkApp()
            }
        }
    }
}