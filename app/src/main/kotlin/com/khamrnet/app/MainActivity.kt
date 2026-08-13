package com.khamrnet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.khamrnet.app.ui.nav.NavGraph
import com.khamrnet.app.ui.theme.KhamrNetTheme
import com.khamrnet.app.vm.AuthViewModel
import com.khamrnet.app.vm.POSViewModel

class MainActivity : ComponentActivity() {
    private val appRepo by lazy { (application as App).repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KhamrNetTheme {
                // Force Arabic RTL across the app
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // Provide ViewModels via factories that accept repository
                    val authFactory = AuthViewModel.provideFactory(appRepo)
                    val posFactory = POSViewModel.provideFactory(appRepo)
                    val authVm: AuthViewModel = viewModel(factory = authFactory)
                    val posVm: POSViewModel = viewModel(factory = posFactory)
                    NavGraph(authVm = authVm, posVm = posVm)
                }
            }
        }
    }
}
