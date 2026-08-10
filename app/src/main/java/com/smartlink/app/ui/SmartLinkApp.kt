package com.smartlink.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartlink.app.ui.screens.CustomersScreen
import com.smartlink.app.ui.screens.DashboardScreen
import com.smartlink.app.ui.screens.FinanceScreen
import com.smartlink.app.ui.screens.InventoryScreen
import com.smartlink.app.ui.screens.InvoiceScreen
import com.smartlink.app.ui.screens.LoginScreen
import com.smartlink.app.ui.screens.MoreScreen
import com.smartlink.app.ui.screens.ReportsScreen
import com.smartlink.app.ui.screens.SyncScreen
import com.smartlink.app.ui.components.SmartLinkShell

object AppRoute {
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val Inventory = "inventory"
    const val Invoices = "invoices"
    const val Customers = "customers"
    const val Finance = "finance"
    const val Reports = "reports"
    const val Sync = "sync"
    const val More = "more"
}

@Composable
fun SmartLinkApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val store = remember(context) { SmartLinkStore(context.applicationContext) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Dashboard,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppRoute.Login) {
                LoginScreen(onLogin = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo(AppRoute.Login) { inclusive = true }
                    }
                })
            }
            composable(AppRoute.Dashboard) {
                AppShell(navController, AppRoute.Dashboard) { DashboardScreen(store) }
            }
            composable(AppRoute.Inventory) {
                AppShell(navController, AppRoute.Inventory) { InventoryScreen(store) }
            }
            composable(AppRoute.Invoices) {
                AppShell(navController, AppRoute.Invoices) { InvoiceScreen(store) }
            }
            composable(AppRoute.Customers) {
                AppShell(navController, AppRoute.Customers) { CustomersScreen(store) }
            }
            composable(AppRoute.Finance) {
                AppShell(navController, AppRoute.Finance) { FinanceScreen(store) }
            }
            composable(AppRoute.Reports) {
                AppShell(navController, AppRoute.Reports) { ReportsScreen() }
            }
            composable(AppRoute.Sync) {
                AppShell(navController, AppRoute.Sync) { SyncScreen() }
            }
            composable(AppRoute.More) {
                AppShell(navController, AppRoute.More) { MoreScreen(navController) }
            }
        }
    }
}

@Composable
private fun AppShell(
    navController: androidx.navigation.NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val title = when (currentRoute) {
        AppRoute.Dashboard -> "الرئيسية"
        AppRoute.Inventory -> "المخزون"
        AppRoute.Invoices -> "الفواتير"
        AppRoute.Customers -> "العملاء"
        AppRoute.Finance -> "الحركات المالية"
        AppRoute.Reports -> "التقارير"
        AppRoute.Sync -> "المزامنة والرسائل"
        else -> "المزيد"
    }
    SmartLinkShell(
        navController = navController,
        currentRoute = currentRoute,
        title = title
    ) { content() }
}