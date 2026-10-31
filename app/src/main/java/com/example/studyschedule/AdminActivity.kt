package com.example.studyschedule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView

class AdminActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); title = "Quản trị viên"
        val box = vbox()
        box.addView(TextView(this).apply { text = "Xin chào, ${Session.user(this@AdminActivity)}"; textSize = 20f })
        box.addView(btn("Quản lý sinh viên") { startActivity(Intent(this, StudentActivity::class.java)) })
        box.addView(btn("Quản lý môn học") { startActivity(Intent(this, SubjectActivity::class.java)) })
        box.addView(btn("Đăng xuất") {
            Session.clear(this); startActivity(Intent(this, LoginActivity::class.java)); finish()
        })
        setContentView(box)
    }
}
