# Student Study Schedule Management & Booking (Android, Kotlin, SQLite)
Mở thư mục bằng Android Studio (Koala+), Sync Gradle, chạy trên máy ảo API 26+.
Tài khoản mẫu: **admin / admin123** (Admin), **SV001 / 123456** (Student).

## Phân công
| TV | Vai trò | File |
|----|---------|------|
| 1 (Leader) | DB, Auth, Git | Models.kt, Db.kt, Session.kt, LoginActivity.kt, build.gradle, Manifest |
| 2 | Chức năng Admin | Ui.kt (CrudActivity), AdminActivity.kt, StudentActivity.kt, SubjectActivity.kt |
| 3 | Chức năng Student | ScheduleActivity.kt, CalendarActivity.kt, Notifier.kt, StudentHomeActivity.kt |

## Git flow
main (ổn định) <- develop <- feature/db-auth, feature/admin, feature/student.
Commit nhỏ, PR vào develop, leader review & merge. Đặt tên commit: `feat:`, `fix:`, `docs:`.
