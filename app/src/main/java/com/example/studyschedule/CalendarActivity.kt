package com.example.studyschedule

import android.app.Activity
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); title = "Lịch học"
        val db = Db(this); val sid = db.studentByAccount(Session.id(this))?.id ?: -1
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val root = vbox()
        val rg = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        listOf("Ngày", "Tuần", "Tháng").forEachIndexed { i, t -> rg.addView(RadioButton(this).apply { text = t; id = i + 1 }) }
        rg.check(1)
        val cal = CalendarView(this); val list = ListView(this)
        var sel = Calendar.getInstance()

        fun refresh() {
            val a = sel.clone() as Calendar; val z = sel.clone() as Calendar
            when (rg.checkedRadioButtonId) {
                2 -> { a.firstDayOfWeek = Calendar.MONDAY; a.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); z.time = a.time; z.add(Calendar.DAY_OF_MONTH, 6) }
                3 -> { a.set(Calendar.DAY_OF_MONTH, 1); z.time = a.time; z.set(Calendar.DAY_OF_MONTH, z.getActualMaximum(Calendar.DAY_OF_MONTH)) }
            }
            val r = db.between(sid, f.format(a.time), f.format(z.time))
            val rows = if (r.isEmpty()) listOf("Không có lịch học (${f.format(a.time)} → ${f.format(z.time)})")
            else r.map { "${it.studyDate} ${it.startTime}-${it.endTime} | ${it.subjectName}\n${it.location} [${it.status}]" }
            list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rows)
        }
        cal.setOnDateChangeListener { _, y, m, d -> sel = Calendar.getInstance().apply { set(y, m, d) }; refresh() }
        rg.setOnCheckedChangeListener { _, _ -> refresh() }
        root.addView(rg)
        root.addView(cal, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root); refresh()
    }
}
