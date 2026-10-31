package com.example.studyschedule

import android.widget.ArrayAdapter
import android.widget.Spinner

class ScheduleActivity : CrudActivity<Schedule>() {
    override val screenTitle = "Lịch học của tôi"
    private val sid by lazy { db.studentByAccount(Session.id(this))?.id ?: -1 }

    override fun load(q: String) = db.schedules(sid, q)
    override fun label(t: Schedule) = t.text()
    override fun remove(t: Schedule) { db.deleteSchedule(t.id); Notifier.cancel(this, t.id); Notifier.show(this, "Lịch học đã xóa", "${t.subjectName} ${t.studyDate}") }

    override fun extra(t: Schedule): List<Pair<String, () -> Unit>> =
        STATUSES.filter { it != t.status }.map { st -> Pair<String, () -> Unit>("Đổi trạng thái: $st", { changeStatus(t, st) }) }

    private fun changeStatus(t: Schedule, st: String) {
        if (!db.setStatus(t, st)) { toast("Không thể: lịch bị trùng"); return }
        Notifier.remind(this, t.copy(status = st))
        Notifier.show(this, "Lịch học thay đổi", "${t.subjectName} ${t.studyDate} → $st")
        refresh()
    }

    override fun edit(t: Schedule?) {
        val subs = db.subjects("")
        if (subs.isEmpty()) { toast("Chưa có môn học nào"); return }
        val sp = Spinner(this).apply {
            adapter = ArrayAdapter(this@ScheduleActivity, android.R.layout.simple_spinner_dropdown_item, subs)
            setSelection(maxOf(0, subs.indexOfFirst { it.id == t?.subjectId }))
        }
        val date = field("Ngày học (chạm để chọn)", t?.studyDate ?: ""); pickDate(date)
        val st = field("Giờ bắt đầu", t?.startTime ?: ""); pickTime(st)
        val en = field("Giờ kết thúc", t?.endTime ?: ""); pickTime(en)
        val loc = field("Địa điểm học", t?.location ?: "")
        val stat = Spinner(this).apply {
            adapter = ArrayAdapter(this@ScheduleActivity, android.R.layout.simple_spinner_dropdown_item, STATUSES)
            setSelection(STATUSES.indexOf(t?.status ?: "Pending"))
        }
        form(if (t == null) "Đặt lịch học" else "Cập nhật lịch học", listOf(sp, date, st, en, loc, stat)) {
            val d = date.text.toString(); val a = st.text.toString(); val e = en.text.toString()
            if (d.isEmpty() || a.isEmpty() || e.isEmpty() || loc.text.isBlank()) { toast("Nhập đủ ngày, giờ, địa điểm"); return@form false }
            if (e <= a) { toast("Giờ kết thúc phải sau giờ bắt đầu"); return@form false }
            val sub = sp.selectedItem as Subject
            val status = stat.selectedItem as String
            if (status != "Cancelled" && db.conflict(sid, d, a, e, t?.id ?: 0)) { toast("Trùng lịch học cùng ngày/giờ!"); return@form false }
            val s = Schedule(t?.id ?: 0, sid, sub.id, d, a, e, loc.text.toString().trim(), status, sub.subjectName)
            val saved = s.copy(id = db.saveSchedule(s))
            Notifier.remind(this, saved)
            Notifier.show(this, if (t == null) "Đã đặt lịch học" else "Lịch học đã thay đổi", "${saved.subjectName} ${saved.studyDate} ${saved.startTime}")
            refresh(); true
        }
    }
}
