package com.khamrnet.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.khamrnet.app.ui.screens.*
import com.khamrnet.app.vm.AuthViewModel
import com.khamrnet.app.vm.POSViewModel

@Composable
fun NavGraph(authVm: AuthViewModel, posVm: POSViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(vm = authVm, onLoginSuccess = { role ->
                if (role == "ADMIN") navController.navigate("admin") else navController.navigate("cashier")
            })
        }
        composable("admin") {
            AdminDashboardScreen(
                onNavigatePOS = { navController.navigate("pos") },
                onUsers = { navController.navigate("users") },
                onStockTransfer = { navController.navigate("stock_transfer") },
                onSettlement = { navController.navigate("settlement_admin") },
                vm = authVm
            )
        }
        composable("cashier") {
            CashierDashboardScreen(
                onNavigatePOS = { navController.navigate("pos") },
                onCustomers = { navController.navigate("customers") },
                onBonds = { navController.navigate("bonds") },
                onSettlement = { navController.navigate("settlement_cashier") },
                vm = authVm
            )
        }
        composable("pos") {
            POSFastScreen(vm = posVm, onBack = { navController.popBackStack() })
        }
        composable("invoice/{invoiceId}", arguments = listOf(navArgument("invoiceId"){ type = NavType.LongType })) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceScreen(invoiceId = invoiceId, vm = posVm)
        }
        composable("customers") {
            CustomerScreen(vm = posVm)
        }
        composable("bonds") {
            BondScreen(vm = posVm)
        }
        composable("settlement_admin") {
            SettlementScreen(vm = posVm, isAdmin = true)
        }
        composable("settlement_cashier") {
            SettlementScreen(vm = posVm, isAdmin = false)
        }
        composable("users") {
            UserManagementScreen(vm = posVm)
        }
        composable("stock_transfer") {
            StockTransferScreen(vm = posVm)
        }
    }
}
