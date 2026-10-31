package com.example.studyschedule

data class Account(val id: Int, val username: String, val password: String, val role: String)
data class Student(val id: Int, val studentCode: String, val fullname: String, val email: String, val phone: String, val accountId: Int)
data class Subject(val id: Int, val subjectCode: String, val subjectName: String, val description: String, val credits: Int) {
    override fun toString() = "$subjectCode - $subjectName ($credits TC)"
}
data class Schedule(
    val id: Int, val studentId: Int, val subjectId: Int, val studyDate: String, val startTime: String,
    val endTime: String, val location: String, val status: String, val subjectName: String = ""
) {
    fun text() = "$subjectName\n$studyDate  $startTime - $endTime  @ $location\n[$status]"
}
val STATUSES = listOf("Pending", "Confirmed", "Cancelled")
