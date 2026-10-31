package com.example.studyschedule

import android.content.Context

/** Lưu thông tin tài khoản đăng nhập bằng SharedPreferences. */
object Session {
    private fun p(c: Context) = c.getSharedPreferences("session", Context.MODE_PRIVATE)
    fun save(c: Context, a: Account) = p(c).edit().putInt("id", a.id).putString("user", a.username).putString("role", a.role).apply()
    fun id(c: Context) = p(c).getInt("id", -1)
    fun user(c: Context) = p(c).getString("user", "") ?: ""
    fun role(c: Context): String? = p(c).getString("role", null)
    fun clear(c: Context) = p(c).edit().clear().apply()
}
