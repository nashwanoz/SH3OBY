package com.smartlink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.smartlink.app.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    LoginScreen(onLogin = { username ->
                        // For demo: navigate to Invoice screen by using simple setContent swap
                        setContent { com.smartlink.app.ui.InvoiceScreen(defaultCountryCode = "967") }
                    })
                }
            }
        }
    }
}