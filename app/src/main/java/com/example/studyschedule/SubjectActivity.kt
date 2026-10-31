package com.example.studyschedule

import android.text.InputType

class SubjectActivity : CrudActivity<Subject>() {
    override val screenTitle = "Quản lý môn học"
    override fun load(q: String) = db.subjects(q)
    override fun label(t: Subject) = "${t.subjectCode} - ${t.subjectName} (${t.credits} TC)\n${t.description}"
    override fun remove(t: Subject) = db.deleteSubject(t.id)

    override fun edit(t: Subject?) {
        val code = field("Mã môn", t?.subjectCode ?: "")
        val name = field("Tên môn", t?.subjectName ?: "")
        val desc = field("Mô tả", t?.description ?: "")
        val cr = field("Số tín chỉ", t?.credits?.toString() ?: "", InputType.TYPE_CLASS_NUMBER)
        form(if (t == null) "Thêm môn học" else "Cập nhật môn học", listOf(code, name, desc, cr)) {
            val credits = cr.text.toString().toIntOrNull()
            if (code.text.isBlank() || name.text.isBlank() || credits == null || credits <= 0) { toast("Nhập đủ mã, tên, tín chỉ > 0"); return@form false }
            val s = Subject(t?.id ?: 0, code.text.toString().trim(), name.text.toString().trim(), desc.text.toString().trim(), credits)
            if (!db.saveSubject(s)) { toast("Lưu thất bại (trùng mã môn?)"); return@form false }
            refresh(); true
        }
    }
}
