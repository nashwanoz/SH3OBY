package com.khamrnet.app.data

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(
    private val db: AppDatabase,
    private val preferences: SharedPreferences
) {

    // الدالة الذكية المحدثة والمغلقة بالكامل لحساب تسلسل أرقام الفواتير
    suspend fun generateInvoiceNumber(userId: Long, documentType: Int): String {
        val user = db.users().find(userId) ?: error("المستخدم غير موجود")
        
        // جلب السنة الحالية بصيغة خانتين (مثال: 26)
        val year = SimpleDateFormat("yy", Locale.US).format(Date())
        val userCode = user.userCode
        
        // مفتاح التخزين المخصص لكل مستخدم وسنة لضمان استقلالية الصناديق
        val key = "invoice_seq_${year}_${userCode}"
        
        // زيادة العداد بمقدار 1 وحفظه في الإعدادات المحلية
        val sequence = preferences.getLong(key, 0L) + 1
        preferences.edit().putLong(key, sequence).apply()
        
        // تركيب وإرجاع الرقم النهائي المكون من: السنة + كود الكاشير + العداد المكون من 5 خانات
        return "$year${userCode}${sequence.toString().padStart(5, '0')}"
    }
}
