package com.smartlink.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.smartlink.app.ui.AppRoute

private data class MoreAction(val title: String, val route: String, val icon: ImageVector)

@Composable
fun MoreScreen(navController: NavHostController) {
    val actions = listOf(
        MoreAction("الحركات المالية", AppRoute.Finance, Icons.Default.Payments),
        MoreAction("التقارير", AppRoute.Reports, Icons.Default.Assessment),
        MoreAction("المزامنة والرسائل", AppRoute.Sync, Icons.Default.CloudSync)
    )
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            Card(
                onClick = {
                    if (action.route != AppRoute.More) {
                        navController.navigate(action.route)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(action.icon, contentDescription = null)
                    Text(action.title)
                }
            }
        }
    }
}