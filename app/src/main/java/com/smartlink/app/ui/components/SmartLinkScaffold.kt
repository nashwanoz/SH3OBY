package com.smartlink.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.smartlink.app.ui.AppRoute

@Composable
fun SmartLinkShell(
    navController: NavHostController,
    currentRoute: String,
    title: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val primaryRoutes = listOf(
        Triple(AppRoute.Dashboard, "الرئيسية", Icons.Filled.SpaceDashboard),
        Triple(AppRoute.Invoices, "الفواتير", Icons.Filled.ReceiptLong),
        Triple(AppRoute.Inventory, "المخزون", Icons.Filled.Storefront),
        Triple(AppRoute.Customers, "العملاء", Icons.Filled.People),
        Triple(AppRoute.More, "المزيد", Icons.Filled.Assessment)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (currentRoute != AppRoute.Dashboard) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "رجوع")
                        }
                    }
                },
                actions = {
                    if (currentRoute == AppRoute.Invoices || currentRoute == AppRoute.Inventory) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                primaryRoutes.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        content = content
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        if (actionLabel != null) {
            androidx.compose.material3.TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}