package com.khamrnet.app

import android.app.Application
import androidx.work.*
import com.khamrnet.app.data.db.KhamrDatabase
import com.khamrnet.app.repository.AppRepository
import com.khamrnet.app.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class App : Application() {
    lateinit var database: KhamrDatabase
    lateinit var repository: AppRepository
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = KhamrDatabase.getDatabase(this)
        repository = AppRepository(database)

        // Ensure default admin exists at first run
        applicationScope.launch {
            val users = repository.getAllUsersImmediate()
            if (users.isEmpty()) {
                repository.insertUserImmediate(
                    com.khamrnet.app.data.entities.User(
                        username = "1",
                        password = "1",
                        displayName = "المدير الافتراضي",
                        role = com.khamrnet.app.data.entities.Role.ADMIN
                    )
                )
            }
            // Seed base products if needed
            repository.seedDefaultProductsIfEmpty()
        }

        // Schedule periodic sync worker every 15 minutes (minimum allowed by WorkManager)
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "KhamrSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
