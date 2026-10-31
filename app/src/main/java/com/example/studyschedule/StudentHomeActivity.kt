package com.example.studyschedule

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudentHomeActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); title = "Sinh viên"
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        val db = Db(this)
        val me = db.studentByAccount(Session.id(this))
        val box = vbox()
        box.addView(TextView(this).apply { text = "Xin chào, ${me?.fullname ?: Session.user(this@StudentHomeActivity)}"; textSize = 20f })
        box.addView(btn("Lịch học của tôi (đặt / sửa / xóa)") { startActivity(Intent(this, ScheduleActivity::class.java)) })
        box.addView(btn("Xem lịch (ngày / tuần / tháng)") { startActivity(Intent(this, CalendarActivity::class.java)) })
        box.addView(btn("Đăng xuất") { Session.clear(this); startActivity(Intent(this, LoginActivity::class.java)); finish() })
        setContentView(box)

        // Thông báo các buổi học hôm nay / ngày mai
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val up = db.between(me?.id ?: -1, f.format(Date()), f.format(Date(System.currentTimeMillis() + 86400000))).filter { it.status != "Cancelled" }
        if (up.isNotEmpty()) Notifier.show(this, "Lịch học sắp tới", "Bạn có ${up.size} buổi học hôm nay/ngày mai: ${up.first().subjectName}...", 1)
    }
}
