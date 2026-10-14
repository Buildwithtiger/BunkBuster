# 🚫🎒 BunkBuster.  Work in process project 12/10/2026

### An Anti-Bunk Attendance System for Educational Institutions

> **"Do Bunk, Catch Bunk!"** 😎

BunkBuster ek **smart attendance system** hai jo **geofencing, live location, OTP verification, aur parent notification** ke through proxy attendance rokta hai.

---

## ✨ Features

- 🔐 **Login** — Barcode + Password (no OTP)
- 🔒 **Logout** — OTP required (teacher se lena padega)
- 📍 **Geofencing** — College ke andar hi present
- ⏰ **Auto-Absent** — Time khatam pe automatic absent
- 📱 **Parent SMS** — Present/absent + live location
- 📝 **Activity** — Assignment + deadline + verify
- 🏆 **Leaderboard** — Graphical, month-wise
- ⚠️ **At-Risk & Top Performers** — Teacher ko pata chale
- 📅 **Timetable** — Auto sequence by time
- 🤖 **Chatbot** — Student apni info poochh sake
- 📚 **Notes App** — Teacher upload, student download
- 🎉 **Weekly Top 3** — Home page pe 1 din ke liye

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x |
| Frontend | HTML, CSS, JS, Thymeleaf |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA |
| Security | Spring Security |
| Barcode | ZXing |
| SMS | Fast2SMS API |
| Charts | Chart.js |
| Excel/PDF | Apache POI, iText |
| Build | Maven |

---

## 🚀 Setup

### 1. Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- IntelliJ IDEA

### 2. Clone
```bash
git clone https://github.com/your-username/BunkBuster.git
cd BunkBuster
```

### 3. Configure `application.properties`
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bunkbuster_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# SMS (Fast2SMS)
sms.api.key=YOUR_API_KEY

# Geofence (College location)
geofence.default.lat=23.0225
geofence.default.lng=72.5714
geofence.default.radius=100

# OTP
otp.expiry.minutes=3
```

### 4. Run
```bash
mvn spring-boot:run
```
Ya IntelliJ mein `BunkBusterApplication.java` run karo.

### 5. Open
```
http://localhost:8080
```

---

## 📦 Modules (24 Modules, 192 Functions)

| # | Module | Functions |
|---|--------|-----------|
| 1 | Authentication | 6 |
| 2 | Teacher Side | 14 |
| 3 | Student Side | 12 |
| 4 | Location & Geofencing | 6 |
| 5 | Time Rules | 5 |
| 6 | OTP System | 4 |
| 7 | Parent Notification | 7 |
| 8 | Reports & Leaderboard | 6 |
| 9 | Notice | 3 |
| 10 | Anti-Proxy Security | 5 |
| 11 | Activity / Assignment | 16 |
| 12 | Personal Notification | 6 |
| 13 | Graphical Leaderboard | 8 |
| 14 | Home Integration | 6 |
| 15 | Performance Monitoring | 10 |
| 16 | Funny Quotes | 6 |
| 17 | Live Attendance Count | 6 |
| 18 | Historical Data | 8 |
| 19 | Auto Timetable | 10 |
| 20 | Student Timetable | 6 |
| 21 | Classmate Leaderboard | 6 |
| 22 | Student Chatbot | 10 |
| 23 | Weekly Topper | 8 |
| 24 | Notes App | 18 |
| | **TOTAL** | **192** |

---

## 🔌 Main API Endpoints

### Auth
| Method | Endpoint |
|--------|----------|
| POST | `/login` |
| POST | `/teacher/login` |
| POST | `/logout/request-otp` |
| POST | `/logout/verify-otp` |

### Teacher
| Method | Endpoint |
|--------|----------|
| POST | `/teacher/add-student` |
| POST | `/teacher/lecture` |
| POST | `/teacher/geofence` |
| POST | `/teacher/notice` |
| POST | `/teacher/activity` |
| GET | `/teacher/at-risk` |
| GET | `/teacher/top-performers` |

### Student
| Method | Endpoint |
|--------|----------|
| GET | `/student/home` |
| POST | `/student/present` |
| GET | `/student/profile` |
| GET | `/student/leaderboard` |
| GET | `/student/timetable` |
| GET | `/student/notes` |

---

## 📁 Project Structure

```
BunkBuster/
├── src/main/java/com/bunkbuster/
│   ├── BunkBusterApplication.java
│   ├── config/
│   ├── controller/
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── scheduler/
│   └── util/
├── src/main/resources/
│   ├── application.properties
│   ├── templates/
│   └── static/
├── pom.xml
└── README.md
```

---

## 🚀 Future Scope

- Face Recognition for attendance
- WhatsApp notifications
- Mobile app (Android + iOS)
- AI-based absence prediction
- Offline mode with sync
- Parent login portal
- Multi-college support
- Blockchain-based certificates


## 👨‍💻 Author

**Your Name**
- GitHub: [@Buildwithtiger](https://github.com/Buildwithtiger)
- Email: onkarsawant009@gmail.com
- Linkdin: [onkar-sawant] (https://www.linkedin.com/in/onkar-sawant-20264a396/)

---

<div align="center">

**🚫🎒 BunkBuster**

**"Bunk Karo, Pakde Jao!"** 😎

</div>
activity test Wed Oct 14 23:23:26 IST 2026
