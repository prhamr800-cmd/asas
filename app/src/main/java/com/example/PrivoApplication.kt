package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class PrivoApplication : Application() {

    companion object {
        const val CHANNEL_MESSAGES = "privo_channel_messages"
        const val CHANNEL_CALLS = "privo_channel_calls"
        const val CHANNEL_SYSTEM = "privo_channel_system"

        lateinit var instance: PrivoApplication
            private set
    }

    lateinit var container: PrivoAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = PrivoAppContainer(applicationContext)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "پیام‌های جدید (Messages)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان پیام‌های دریافتی پریوو"
                enableVibration(true)
            }

            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "تماس‌های صوتی و تصویری (Calls)",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "اعلان تماس‌های ورودی پریوو"
                enableVibration(true)
            }

            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "اعلان‌های سیستم و هوش مصنوعی",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان‌های سیستمی و هشدارهای پریوو"
            }

            notificationManager.createNotificationChannels(
                listOf(messagesChannel, callsChannel, systemChannel)
            )
        }
    }
}
