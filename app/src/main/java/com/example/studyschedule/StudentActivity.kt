package com.example.studyschedule

import android.text.InputType

class StudentActivity : CrudActivity<Student>() {
    override val screenTitle = "Quản lý sinh viên"
    override fun load(q: String) = db.students(q)
    override fun label(t: Student) = "${t.studentCode} - ${t.fullname}\n${t.email} | ${t.phone}"
    override fun remove(t: Student) = db.deleteStudent(t)

    override fun edit(t: Student?) {
        val code = field("Mã sinh viên", t?.studentCode ?: "")
        val name = field("Họ tên", t?.fullname ?: "")
        val mail = field("Email", t?.email ?: "", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val phone = field("Số điện thoại", t?.phone ?: "", InputType.TYPE_CLASS_PHONE)
        val pass = field(if (t == null) "Mật khẩu (tài khoản đăng nhập = mã SV)" else "Mật khẩu mới (bỏ trống nếu giữ nguyên)")
        form(if (t == null) "Thêm sinh viên" else "Cập nhật sinh viên", listOf(code, name, mail, phone, pass)) {
            val c = code.text.toString().trim()
            if (c.isEmpty() || name.text.isBlank()) { toast("Nhập mã và họ tên"); return@form false }
            if (mail.text.isNotBlank() && !mail.text.contains("@")) { toast("Email không hợp lệ"); return@form false }
            if (t == null && pass.text.isBlank()) { toast("Nhập mật khẩu"); return@form false }
            val s = Student(t?.id ?: 0, c, name.text.toString().trim(), mail.text.toString().trim(), phone.text.toString().trim(), t?.accountId ?: 0)
            if (!db.saveStudent(s, pass.text.toString())) { toast("Lưu thất bại (trùng mã sinh viên?)"); return@form false }
            refresh(); true
        }
    }
}
