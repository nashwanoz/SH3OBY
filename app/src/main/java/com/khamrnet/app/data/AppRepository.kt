*** Begin Patch
*** Update File: app/src/main/java/com/khamrnet/app/data/AppRepository.kt
@@
     private suspend fun nextDocumentId(userId: Long, documentType: Int): String {
         val user = db.users().find(userId) ?: error("المستخدم غير موجود")
-        val year = SimpleDateFormat("yy", Locale.US).format(Date())
-        val prefix = "$year${user.userCode}$documentType"
-        val key = "document_sequence_${year}_${user.userCode}_$documentType"
-        val sequence = preferences.getLong(key, 0L) + 1
-        preferences.edit().putLong(key, sequence).apply()
-        return "$prefix${sequence.toString().padStart(4, '0')}"
+        // Custom invoice numbering: YY + userCode + 5-digit chronological serial
+        val year = SimpleDateFormat("yy", Locale.US).format(Date())
+        val userCode = user.userCode
+        // Sequence stored per year and user; use existing preference-based sequence but ensure 5 digits
+        val key = "invoice_seq_${year}_${userCode}"
+        val sequence = preferences.getLong(key, 0L) + 1
+        preferences.edit().putLong(key, sequence).apply()
+        return "$year${userCode}${sequence.toString().padStart(5, '0')}"
     }
*** End Patch