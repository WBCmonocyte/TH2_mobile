package com.example.studyschedule

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Db(ctx: Context) : SQLiteOpenHelper(ctx, "study.db", null, 1) {
    override fun onConfigure(d: SQLiteDatabase) { super.onConfigure(d); d.setForeignKeyConstraintsEnabled(true) }

    override fun onCreate(d: SQLiteDatabase) {
        d.execSQL("CREATE TABLE Account(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL)")
        d.execSQL("CREATE TABLE Student(id INTEGER PRIMARY KEY AUTOINCREMENT, studentCode TEXT UNIQUE NOT NULL, fullname TEXT, email TEXT, phone TEXT, accountId INTEGER, FOREIGN KEY(accountId) REFERENCES Account(id) ON DELETE CASCADE)")
        d.execSQL("CREATE TABLE Subject(id INTEGER PRIMARY KEY AUTOINCREMENT, subjectCode TEXT UNIQUE NOT NULL, subjectName TEXT NOT NULL, description TEXT, credits INTEGER)")
        d.execSQL("CREATE TABLE StudySchedule(id INTEGER PRIMARY KEY AUTOINCREMENT, studentId INTEGER, subjectId INTEGER, studyDate TEXT, startTime TEXT, endTime TEXT, location TEXT, status TEXT, " +
                "FOREIGN KEY(studentId) REFERENCES Student(id) ON DELETE CASCADE, FOREIGN KEY(subjectId) REFERENCES Subject(id) ON DELETE CASCADE)")
        // Dữ liệu mẫu: admin/admin123 và SV001/123456
        d.execSQL("INSERT INTO Account(username,password,role) VALUES('admin','admin123','Admin')")
        d.execSQL("INSERT INTO Account(username,password,role) VALUES('SV001','123456','Student')")
        d.execSQL("INSERT INTO Student(studentCode,fullname,email,phone,accountId) VALUES('SV001','Nguyen Van A','a@gmail.com','0900000001',2)")
        d.execSQL("INSERT INTO Subject(subjectCode,subjectName,description,credits) VALUES('MOB101','Lap trinh Android','Android co ban',3)")
        d.execSQL("INSERT INTO Subject(subjectCode,subjectName,description,credits) VALUES('DB101','Co so du lieu','SQL & thiet ke CSDL',3)")
    }
    override fun onUpgrade(d: SQLiteDatabase, o: Int, n: Int) {}

    private fun <T> q(sql: String, args: Array<String>, f: (Cursor) -> T): List<T> {
        val l = ArrayList<T>()
        readableDatabase.rawQuery(sql, args).use { c -> while (c.moveToNext()) l.add(f(c)) }
        return l
    }
    private fun like(k: String) = "%$k%"

    // ---- Account ----
    fun login(u: String, p: String) = q("SELECT id,username,password,role FROM Account WHERE username=? AND password=?", arrayOf(u, p)) {
        Account(it.getInt(0), it.getString(1), it.getString(2), it.getString(3))
    }.firstOrNull()

    // ---- Student ----
    private fun st(c: Cursor) = Student(c.getInt(0), c.getString(1), c.getString(2) ?: "", c.getString(3) ?: "", c.getString(4) ?: "", c.getInt(5))
    fun students(k: String) = q("SELECT id,studentCode,fullname,email,phone,accountId FROM Student WHERE studentCode LIKE ? OR fullname LIKE ? OR email LIKE ? ORDER BY studentCode",
        arrayOf(like(k), like(k), like(k))) { st(it) }
    fun studentByAccount(accId: Int) = q("SELECT id,studentCode,fullname,email,phone,accountId FROM Student WHERE accountId=?", arrayOf("$accId")) { st(it) }.firstOrNull()

    fun saveStudent(s: Student, pass: String): Boolean = try {
        val d = writableDatabase
        val cv = ContentValues().apply { put("studentCode", s.studentCode); put("fullname", s.fullname); put("email", s.email); put("phone", s.phone) }
        if (s.id == 0) {
            val a = d.insertOrThrow("Account", null, ContentValues().apply { put("username", s.studentCode); put("password", pass); put("role", "Student") })
            cv.put("accountId", a)
            d.insertOrThrow("Student", null, cv)
        } else {
            d.updateWithOnConflict("Student", cv, "id=?", arrayOf("${s.id}"), SQLiteDatabase.CONFLICT_ABORT)
            val ac = ContentValues().apply { put("username", s.studentCode); if (pass.isNotEmpty()) put("password", pass) }
            d.update("Account", ac, "id=?", arrayOf("${s.accountId}"))
        }
        true
    } catch (e: Exception) { false }
    fun deleteStudent(s: Student) { writableDatabase.delete("Account", "id=?", arrayOf("${s.accountId}")) } // cascade xóa Student + lịch

    // ---- Subject ----
    fun subjects(k: String) = q("SELECT id,subjectCode,subjectName,description,credits FROM Subject WHERE subjectCode LIKE ? OR subjectName LIKE ? ORDER BY subjectCode",
        arrayOf(like(k), like(k))) { Subject(it.getInt(0), it.getString(1), it.getString(2), it.getString(3) ?: "", it.getInt(4)) }
    fun saveSubject(s: Subject): Boolean = try {
        val cv = ContentValues().apply { put("subjectCode", s.subjectCode); put("subjectName", s.subjectName); put("description", s.description); put("credits", s.credits) }
        if (s.id == 0) writableDatabase.insertOrThrow("Subject", null, cv)
        else writableDatabase.updateWithOnConflict("Subject", cv, "id=?", arrayOf("${s.id}"), SQLiteDatabase.CONFLICT_ABORT)
        true
    } catch (e: Exception) { false }
    fun deleteSubject(id: Int) { writableDatabase.delete("Subject", "id=?", arrayOf("$id")) }

    // ---- StudySchedule ----
    private val SEL = "SELECT s.id,s.studentId,s.subjectId,s.studyDate,s.startTime,s.endTime,s.location,s.status,j.subjectName FROM StudySchedule s JOIN Subject j ON j.id=s.subjectId "
    private fun sc(c: Cursor) = Schedule(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6) ?: "", c.getString(7), c.getString(8))
    fun schedules(sid: Int, k: String) = q(SEL + "WHERE s.studentId=? AND (j.subjectName LIKE ? OR s.location LIKE ? OR s.studyDate LIKE ? OR s.status LIKE ?) ORDER BY s.studyDate,s.startTime",
        arrayOf("$sid", like(k), like(k), like(k), like(k))) { sc(it) }
    fun between(sid: Int, a: String, b: String) = q(SEL + "WHERE s.studentId=? AND s.studyDate BETWEEN ? AND ? ORDER BY s.studyDate,s.startTime", arrayOf("$sid", a, b)) { sc(it) }

    /** Trùng lịch: cùng sinh viên, cùng ngày, khoảng giờ giao nhau (bỏ qua lịch đã hủy). */
    fun conflict(sid: Int, date: String, start: String, end: String, excludeId: Int) =
        q("SELECT 1 FROM StudySchedule WHERE studentId=? AND studyDate=? AND status<>'Cancelled' AND id<>? AND startTime<? AND endTime>?",
            arrayOf("$sid", date, "$excludeId", end, start)) { 1 }.isNotEmpty()

    fun saveSchedule(s: Schedule): Int {
        val cv = ContentValues().apply {
            put("studentId", s.studentId); put("subjectId", s.subjectId); put("studyDate", s.studyDate)
            put("startTime", s.startTime); put("endTime", s.endTime); put("location", s.location); put("status", s.status)
        }
        return if (s.id == 0) writableDatabase.insert("StudySchedule", null, cv).toInt()
        else { writableDatabase.update("StudySchedule", cv, "id=?", arrayOf("${s.id}")); s.id }
    }
    fun deleteSchedule(id: Int) { writableDatabase.delete("StudySchedule", "id=?", arrayOf("$id")) }
    fun setStatus(s: Schedule, status: String): Boolean {
        if (status != "Cancelled" && conflict(s.studentId, s.studyDate, s.startTime, s.endTime, s.id)) return false
        writableDatabase.update("StudySchedule", ContentValues().apply { put("status", status) }, "id=?", arrayOf("${s.id}"))
        return true
    }
}
