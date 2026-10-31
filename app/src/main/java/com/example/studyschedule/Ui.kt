package com.example.studyschedule

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import java.util.Calendar

fun Context.toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
fun Context.dp(n: Int) = (n * resources.displayMetrics.density).toInt()
fun Context.vbox() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
fun Context.btn(t: String, f: () -> Unit) = Button(this).apply { text = t; setOnClickListener { f() } }
fun Context.field(hint: String, v: String = "", type: Int = InputType.TYPE_CLASS_TEXT) =
    EditText(this).apply { this.hint = hint; setText(v); inputType = type }
fun Context.confirm(msg: String, f: () -> Unit) {
    AlertDialog.Builder(this).setMessage(msg).setPositiveButton("Đồng ý") { _, _ -> f() }.setNegativeButton("Hủy", null).show()
}
fun Context.pickDate(e: EditText) {
    e.isFocusable = false
    e.setOnClickListener {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d -> e.setText("%04d-%02d-%02d".format(y, m + 1, d)) },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }
}
fun Context.pickTime(e: EditText) {
    e.isFocusable = false
    e.setOnClickListener { TimePickerDialog(this, { _, h, m -> e.setText("%02d:%02d".format(h, m)) }, 8, 0, true).show() }
}
/** Hộp thoại form; onOk trả true để đóng, false để giữ lại (khi dữ liệu sai). */
fun Context.form(title: String, views: List<View>, onOk: () -> Boolean) {
    val box = vbox(); views.forEach { box.addView(it) }
    val d = AlertDialog.Builder(this).setTitle(title).setView(ScrollView(this).apply { addView(box) })
        .setPositiveButton("Lưu", null).setNegativeButton("Hủy", null).create()
    d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if (onOk()) d.dismiss() } }
    d.show()
}

/** Màn hình danh sách dùng chung: tìm kiếm + thêm + xem/sửa (chạm) + menu xóa (nhấn giữ). */
abstract class CrudActivity<T> : Activity() {
    abstract val screenTitle: String
    abstract fun load(q: String): List<T>
    abstract fun label(t: T): String
    abstract fun edit(t: T?)
    abstract fun remove(t: T)
    open fun extra(t: T): List<Pair<String, () -> Unit>> = emptyList()

    lateinit var db: Db
    private lateinit var list: ListView
    private var items = listOf<T>()
    private var q = ""

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        db = Db(this); title = screenTitle
        val root = vbox()
        val s = field("Tìm kiếm...")
        s.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(e: Editable?) { q = e.toString().trim(); refresh() }
            override fun beforeTextChanged(a: CharSequence?, b: Int, c: Int, d: Int) {}
            override fun onTextChanged(a: CharSequence?, b: Int, c: Int, d: Int) {}
        })
        list = ListView(this)
        root.addView(s); root.addView(btn("+ Thêm mới") { edit(null) }); root.addView(list)
        setContentView(root)
        list.setOnItemClickListener { _, _, i, _ -> edit(items[i]) }
        list.setOnItemLongClickListener { _, _, i, _ ->
            val t = items[i]
            val ex = extra(t).toMutableList()
            ex.add(Pair<String, () -> Unit>("Xóa", { confirm("Xóa mục này?") { remove(t); refresh() } }))
            AlertDialog.Builder(this).setItems(ex.map { it.first }.toTypedArray()) { _, w -> ex[w].second() }.show()
            true
        }
        refresh()
    }
    fun refresh() {
        items = load(q)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items.map { label(it) })
    }
}
