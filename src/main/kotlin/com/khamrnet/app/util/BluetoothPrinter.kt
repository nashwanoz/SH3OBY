package com.khamrnet.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

object BluetoothPrinter {
    private const val TAG = "BluetoothPrinter"
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Return paired devices list
    fun getPairedDevices(): List<BluetoothDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        return adapter.bondedDevices?.toList() ?: emptyList()
    }

    private fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Attempt to print text using selected device; if device is null uses first paired device.
    fun printText(context: Context, text: String, deviceName: String? = null, onComplete: ((Boolean, String?) -> Unit)? = null) {
        if (!hasBluetoothPermission(context)) {
            onComplete?.invoke(false, "مطلوب إذن BLUETOOTH_CONNECT على Android 12+")
            return
        }
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            onComplete?.invoke(false, "جهاز لا يدعم البلوتوث")
            return
        }
        if (!adapter.isEnabled) {
            onComplete?.invoke(false, "البلوتوث غير مفعل")
            return
        }
        val paired = adapter.bondedDevices
        val device = if (!deviceName.isNullOrEmpty()) {
            paired?.firstOrNull { it.name == deviceName }
        } else {
            paired?.firstOrNull()
        }
        if (device == null) {
            onComplete?.invoke(false, "لا يوجد جهاز بلوتوث مربوط")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            var socket: BluetoothSocket? = null
            try {
                // create socket and connect
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                val out: OutputStream = socket.outputStream

                // Convert UTF-8 Arabic text to printer bytes. Many ESC/POS printers require CP864/CP1256 or custom encoding.
                // We'll attempt UTF-8; for devices not supporting it, user must configure printer firmware or use glyph mapping.
                val encoded: ByteArray = try {
                    text.toByteArray(Charset.forName("UTF-8"))
                } catch (e: Exception) {
                    text.toByteArray(Charset.defaultCharset())
                }

                out.write(encoded)
                // feed and cut commands (may vary by printer). We'll feed lines and issue simple cut if supported.
                out.write(byteArrayOf(0x0A, 0x0A, 0x0A))
                out.flush()
                onComplete?.invoke(true, null)
            } catch (e: IOException) {
                Log.e(TAG, "Print error: ${e.message}", e)
                onComplete?.invoke(false, e.message)
            } catch (t: Throwable) {
                Log.e(TAG, "Print throwable: ${t.message}", t)
                onComplete?.invoke(false, t.message)
            } finally {
                try {
                    socket?.close()
                } catch (ignored: Exception) {}
            }
        }
    }

    // Open Bluetooth settings - helper
    fun openBluetoothSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_BLUETOOTH_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
