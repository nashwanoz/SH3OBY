package com.khamrnet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.khamrnet.app.ui.AppViewModel
import com.khamrnet.app.ui.KhamrApp
import com.khamrnet.app.ui.KhamrTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                KhamrTheme { KhamrApp(viewModel) }
            }
        }
    }
}

enum class AppSection(
    val title: String,
    val icon: ImageVector
) {
    HOME("الرئيسية", Icons.Default.Assessment),
    POS("فاتورة جديدة", Icons.Default.PointOfSale),
    INVOICES("فواتير المبيعات", Icons.Default.ReceiptLong),
    REPORTS("التقارير", Icons.Default.Assessment),
    PRODUCTS("بيانات الأصناف", Icons.Default.Inventory),
    USERS("المستخدمون", Icons.Default.People),
    TRANSFERS("تحويل مخزون", Icons.Default.SwapHoriz),
    CUSTOMERS("العملاء", Icons.Default.People),
    BONDS("السندات", Icons.Default.ReceiptLong),
    SETTLEMENTS("التصفية", Icons.Default.AccountBalance)
}