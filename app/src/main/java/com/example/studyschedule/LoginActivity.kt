package com.example.studyschedule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.TextView

class LoginActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Session.role(this)?.let { go(it); return }   // đã đăng nhập từ trước
        val box = vbox().apply { gravity = Gravity.CENTER }
        val u = field("Tên đăng nhập")
        val p = field("Mật khẩu", type = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        box.addView(TextView(this).apply { text = "QUẢN LÝ LỊCH HỌC"; textSize = 24f; gravity = Gravity.CENTER })
        box.addView(u); box.addView(p)
        box.addView(btn("Đăng nhập") {
            val acc = Db(this).login(u.text.toString().trim(), p.text.toString())
            if (acc == null) toast("Sai tên đăng nhập hoặc mật khẩu")
            else { Session.save(this, acc); go(acc.role) }
        })
        box.addView(TextView(this).apply { text = "Admin: admin/admin123\nSinh viên: SV001/123456"; gravity = Gravity.CENTER })
        setContentView(box)
    }
    private fun go(role: String) {
        startActivity(Intent(this, if (role == "Admin") AdminActivity::class.java else StudentHomeActivity::class.java))
        finish()
    }
}
