package com.example.studyschedule

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale

object Notifier {
    private const val CH = "study"

    fun show(c: Context, title: String, text: String, id: Int = (System.currentTimeMillis() % 100000).toInt()) {
        if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH, "Lịch học", NotificationManager.IMPORTANCE_HIGH))
        nm.notify(id, Notification.Builder(c, CH).setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title).setContentText(text).setAutoCancel(true).build())
    }

    private fun pi(c: Context, id: Int, text: String = "") = PendingIntent.getBroadcast(
        c, id, Intent(c, ReminderReceiver::class.java).putExtra("text", text), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    /** Đặt nhắc 15 phút trước giờ học; lịch bị hủy thì xóa nhắc. */
    fun remind(c: Context, s: Schedule) {
        val am = c.getSystemService(AlarmManager::class.java)
        val p = pi(c, s.id, "${s.subjectName} lúc ${s.startTime} tại ${s.location}")
        am.cancel(p)
        if (s.status == "Cancelled") return
        val t = (SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("${s.studyDate} ${s.startTime}")?.time ?: return) - 15 * 60000
        if (t > System.currentTimeMillis()) am.set(AlarmManager.RTC_WAKEUP, t, p)
    }
    fun cancel(c: Context, id: Int) { c.getSystemService(AlarmManager::class.java).cancel(pi(c, id)) }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { Notifier.show(c, "Sắp đến giờ học", i.getStringExtra("text") ?: "") }
}
