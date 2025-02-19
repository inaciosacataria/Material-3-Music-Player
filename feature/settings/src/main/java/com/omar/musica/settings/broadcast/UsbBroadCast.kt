package com.omar.musica.settings.broadcast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast
import android.app.Service
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi


class UsbReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                Log.d("UsbReceiver", "USB Conectado!")
                Toast.makeText(context, "USB Conectado!", Toast.LENGTH_SHORT).show()

                // Inicia o serviço em segundo plano para manter a detecção ativa
                val serviceIntent = Intent(context, UsbService::class.java)
                context?.startForegroundService(serviceIntent)
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Log.d("UsbReceiver", "USB Removido!")
                Toast.makeText(context, "USB Removido!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class UsbService : Service() {

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    Log.d("UsbService", "USB ainda conectado!")
                    Toast.makeText(context, "USB ainda conectado!", Toast.LENGTH_SHORT).show()
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.d("UsbService", "USB foi removido!")
                    Toast.makeText(context, "USB foi removido!", Toast.LENGTH_SHORT).show()
                    stopSelf() // Finaliza o serviço quando o USB é removido
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(usbReceiver, IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification() {
        val channelId = "usb_detection_channel"
        val channelName = "USB Detection Service"

        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Monitorando USB")
            .setContentText("Este serviço mantém a detecção de USB ativa.")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()

        startForeground(1, notification)
    }
}
