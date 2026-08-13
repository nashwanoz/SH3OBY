package com.khamrnet.app.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.khamrnet.app.data.db.KhamrDatabase
import com.khamrnet.app.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * SyncWorker:
 * - Reads unposted rows from invoices, invoice_lines, bonds, settlements, stock_allocations
 * - Pushes each to Firebase Realtime Database under "khamr_sync/<table>"
 * - On successful push, marks the record posted == 1 via repository
 *
 * This worker is robust and only marks posted after successful push.
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    private val TAG = "SyncWorker"
    private val db = KhamrDatabase.getDatabase(appContext)
    private val repo = AppRepository(db)
    private val rtDatabase = FirebaseDatabase.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // quick check: if Firebase not configured, skip gracefully
            val refRoot = try {
                rtDatabase.reference
            } catch (e: Exception) {
                Log.w(TAG, "Firebase not configured or inaccessible: ${e.message}")
                return@withContext Result.success()
            }

            // helper suspend function to push an object and wait for completion
            suspend fun pushAndAwait(path: String, payload: Any): Boolean =
                suspendCancellableCoroutine { cont ->
                    try {
                        val node = refRoot.child("khamr_sync").child(path).push()
                        node.setValue(payload) { err, _ ->
                            if (err != null) {
                                cont.resume(false)
                            } else {
                                cont.resume(true)
                            }
                        }
                    } catch (ex: Exception) {
                        cont.resumeWithException(ex)
                    }
                }

            // Invoices
            val invoices = repo.getUnpostedInvoices()
            for (inv in invoices) {
                val ok = try { pushAndAwait("invoices", inv) } catch (e: Exception) { false }
                if (ok) {
                    repo.markInvoicePosted(inv.id)
                } else {
                    Log.w(TAG, "Failed to push invoice ${inv.id}, will retry later")
                }
            }

            // Invoice lines
            val lines = repo.getUnpostedInvoiceLines()
            for (ln in lines) {
                val ok = try { pushAndAwait("invoice_lines", ln) } catch (e: Exception) { false }
                if (ok) {
                    repo.markInvoiceLinePosted(ln.id)
                } else {
                    Log.w(TAG, "Failed to push invoice_line ${ln.id}, will retry later")
                }
            }

            // Bonds
            val bonds = repo.getUnpostedBonds()
            for (b in bonds) {
                val ok = try { pushAndAwait("bonds", b) } catch (e: Exception) { false }
                if (ok) {
                    repo.markBondPosted(b.id)
                } else {
                    Log.w(TAG, "Failed to push bond ${b.id}, will retry later")
                }
            }

            // Settlements
            val settles = repo.getUnpostedSettlements()
            for (s in settles) {
                val ok = try { pushAndAwait("settlements", s) } catch (e: Exception) { false }
                if (ok) {
                    repo.markSettlementPosted(s.id)
                } else {
                    Log.w(TAG, "Failed to push settlement ${s.id}, will retry later")
                }
            }

            // Stock allocations
            val allocations = repo.getUnpostedStockAllocations()
            for (a in allocations) {
                val ok = try { pushAndAwait("stock_allocations", a) } catch (e: Exception) { false }
                if (ok) {
                    repo.markStockAllocationPosted(a.id)
                } else {
                    Log.w(TAG, "Failed to push stock allocation ${a.id}, will retry later")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker exception: ${e.message}", e)
            Result.retry()
        }
    }
}
