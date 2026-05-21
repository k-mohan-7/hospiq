# MedConnect — Master Agent Development Prompt
### Full-Stack Android App: Kotlin (Frontend) + PHP (Backend) + MySQL via XAMPP

---

## YOUR ROLE

You are a senior full-stack mobile developer. You will build the complete **MedConnect** Android application from scratch — pixel-perfect UI in Kotlin/Jetpack Compose, a PHP REST API backend, and a MySQL database — following every instruction in this document exactly. Do not improvise. Do not skip steps. Do not change folder paths. Read and implement everything in the order given.

---

## TECH STACK (FIXED — DO NOT CHANGE)

| Layer | Technology |
|---|---|
| Android UI | Kotlin + Jetpack Compose + Material 3 |
| Android HTTP | Retrofit 2 + OkHttp + Gson Converter |
| Android Images | Coil (image loading) |
| Android Nav | Jetpack Navigation Compose |
| Backend Language | PHP 8.x |
| Backend Server | XAMPP (Apache + MySQL) |
| Database | MySQL (via XAMPP phpMyAdmin) |
| API Style | REST JSON API |
| Auth | JWT tokens (PHP side) + stored in Android SharedPreferences |

---

## CRITICAL PATH CONFIGURATION

There is **one single place** in the entire Android project where the server IP address is defined. Every Retrofit call routes through this. To switch between local/production, the developer changes only this one constant.

**File:** `app/src/main/java/com/medconnect/network/ApiConfig.kt`

```kotlin
object ApiConfig {
    // ← ONLY EDIT THIS LINE to change the server IP
    private const val BASE_IP = "192.168.1.100"
    const val BASE_URL = "http://$BASE_IP/hospiq/"
    const val TIMEOUT_SECONDS = 30L
}
```

Every Retrofit instance, every API endpoint, every image URL must reference `ApiConfig.BASE_URL`. Never hardcode an IP anywhere else in the project.

---

## FOLDER PATHS (FIXED — DO NOT CHANGE)

### Android Project
Create the Android project at a standard Android Studio location. Package name: `com.medconnect`

### PHP Backend
**All PHP files must be created at:**
```
C:\xampp\htdocs\hospiq\
```
Structure inside this folder:
```
C:\xampp\htdocs\hospiq\
├── config\
│   └── db.php              ← single DB connection file
├── auth\
│   ├── register_patient.php
│   ├── register_doctor.php
│   └── login.php
├── hospitals\
│   ├── get_all.php
│   ├── get_by_id.php
│   ├── get_nearby.php
│   ├── create.php
│   └── rate.php
├── doctors\
│   ├── get_by_hospital.php
│   ├── get_profile.php
│   ├── update_profile.php
│   └── update_status.php
├── appointments\
│   ├── book.php
│   ├── get_patient.php
│   ├── get_doctor.php
│   ├── accept.php
│   ├── reject.php
│   └── reschedule.php
├── slots\
│   └── get_available.php
├── notifications\
│   └── get.php
└── uploads\
    ├── hospitals\          ← hospital images stored here
    └── doctors\            ← doctor profile photos stored here
```

### UI Design Images (READ-ONLY SOURCE)
**All Figma-exported screen images are located at:**
```
C:\Users\HP\Documents\Application_developement\Doctor_patient\figmaresources\
```
When implementing any screen, read the corresponding image from this path first using the terminal, then implement the Jetpack Compose code to match it exactly — colors, spacing, layout, component shapes, and typography. Treat these images as the source of truth for all UI decisions.

To list available images run:
```bash
dir "C:\Users\HP\Documents\Application_developement\Doctor_patient\figmaresources\"
```

---

## DATABASE SETUP

**Database name:** `hospiq`
**MySQL server:** localhost via XAMPP (port 3306)
**Access via:** http://localhost/phpmyadmin

Run the following SQL in MySQL terminal or phpMyAdmin to create the entire schema:

```sql
CREATE DATABASE IF NOT EXISTS hospiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospiq;

-- Users (both patients and doctors share this table)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('patient', 'doctor') NOT NULL,
    profile_photo VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hospitals
CREATE TABLE hospitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    type ENUM('Government', 'Private', 'Clinic', 'Multispecialty') NOT NULL,
    photo VARCHAR(255) DEFAULT NULL,
    latitude DECIMAL(10, 8) DEFAULT NULL,
    longitude DECIMAL(11, 8) DEFAULT NULL,
    opening_hours VARCHAR(100) DEFAULT '9:00 AM - 8:00 PM',
    avg_rating DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Hospital facilities (ICU, Emergency, etc.)
CREATE TABLE hospital_facilities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_id INT NOT NULL,
    facility_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE
);

-- Doctor profiles (extends users where role = 'doctor')
CREATE TABLE doctor_profiles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    hospital_id INT NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) NOT NULL,
    years_experience INT DEFAULT 0,
    bio TEXT,
    status ENUM('available', 'busy', 'in_surgery') DEFAULT 'available',
    rating DECIMAL(3, 2) DEFAULT 0.00,
    total_patients INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
);

-- Doctor availability slots
CREATE TABLE availability_slots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    slot_date DATE NOT NULL,
    slot_time TIME NOT NULL,
    is_booked TINYINT(1) DEFAULT 0,
    FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(id) ON DELETE CASCADE
);

-- Appointments
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    hospital_id INT NOT NULL,
    slot_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    consultation_type ENUM('in_person', 'video_call') DEFAULT 'in_person',
    status ENUM('pending', 'accepted', 'rejected', 'rescheduled', 'completed', 'cancelled') DEFAULT 'pending',
    reschedule_reason TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(id),
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    FOREIGN KEY (slot_id) REFERENCES availability_slots(id)
);

-- Hospital ratings
CREATE TABLE hospital_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_id INT NOT NULL,
    patient_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    FOREIGN KEY (patient_id) REFERENCES users(id)
);

-- Notifications
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    type ENUM('appointment', 'general', 'rating', 'status') DEFAULT 'general',
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- JWT tokens (for invalidation/logout)
CREATE TABLE auth_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

After running this SQL, verify every table was created with:
```sql
SHOW TABLES FROM hospiq;
```

---

## PHP BACKEND IMPLEMENTATION

### db.php (C:\xampp\htdocs\hospiq\config\db.php)
```php
<?php
define('DB_HOST', 'localhost');
define('DB_USER', 'root');
define('DB_PASS', '');
define('DB_NAME', 'hospiq');

$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database connection failed']);
    exit();
}
$conn->set_charset('utf8mb4');

function respond($success, $message, $data = null) {
    header('Content-Type: application/json');
    $response = ['success' => $success, 'message' => $message];
    if ($data !== null) $response['data'] = $data;
    echo json_encode($response);
    exit();
}
?>
```

### Standard API Response Format
Every PHP endpoint must return JSON in this exact format:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... }
}
```
On error:
```json
{
  "success": false,
  "message": "Error description"
}
```

### All PHP files must include at the top:
```php
<?php
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Content-Type: application/json');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit(); }
require_once '../config/db.php';
```

### Implement every PHP file listed in the folder structure above. Key endpoints:

**POST /auth/register_patient.php** — fields: full_name, email, phone, password → hashed with password_hash()
**POST /auth/register_doctor.php** — fields: full_name, email, phone, password, specialization, license_number, years_experience, bio, hospital_id (or creates hospital if new)
**POST /auth/login.php** — email + password → returns JWT token + user role + user id
**GET /hospitals/get_all.php** — returns all hospitals with avg_rating, doctor count, facilities
**GET /hospitals/get_nearby.php?lat=X&lng=Y&radius=10** — returns hospitals sorted by distance
**GET /hospitals/get_by_id.php?id=X** — full hospital detail with doctors list
**POST /hospitals/create.php** — creates hospital with multipart/form-data (includes photo upload)
**POST /hospitals/rate.php** — patient rates a hospital
**GET /doctors/get_by_hospital.php?hospital_id=X** — list of doctors at a hospital
**GET /doctors/get_profile.php?doctor_id=X** — full doctor profile
**POST /doctors/update_status.php** — doctor updates status (available/busy/in_surgery)
**GET /slots/get_available.php?doctor_id=X&date=YYYY-MM-DD** — available time slots
**POST /appointments/book.php** — patient books an appointment
**GET /appointments/get_patient.php?patient_id=X** — all appointments for a patient
**GET /appointments/get_doctor.php?doctor_id=X** — all appointments for a doctor
**POST /appointments/accept.php** — doctor accepts appointment
**POST /appointments/reject.php** — doctor rejects appointment
**POST /appointments/reschedule.php** — doctor reschedules with new date/time + reason
**GET /notifications/get.php?user_id=X** — all notifications for a user

---

## ANDROID PROJECT STRUCTURE

```
com.medconnect/
├── network/
│   ├── ApiConfig.kt          ← SINGLE IP CONFIG — edit only here
│   ├── ApiService.kt         ← all Retrofit endpoints
│   ├── RetrofitInstance.kt   ← singleton Retrofit builder using ApiConfig
│   └── models/               ← all data classes (User, Hospital, Doctor, Appointment, etc.)
├── ui/
│   ├── theme/
│   │   ├── Color.kt          ← all color constants
│   │   ├── Type.kt           ← typography
│   │   └── Theme.kt          ← MaterialTheme setup
│   ├── screens/
│   │   ├── splash/
│   │   ├── auth/
│   │   │   ├── PatientLoginScreen.kt
│   │   │   ├── PatientSignUpScreen.kt
│   │   │   └── DoctorRegisterScreen.kt
│   │   ├── patient/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HospitalDetailScreen.kt
│   │   │   ├── DoctorProfileScreen.kt
│   │   │   ├── BookingScreen.kt
│   │   │   ├── AppointmentConfirmScreen.kt
│   │   │   ├── AppointmentsScreen.kt
│   │   │   ├── NotificationsScreen.kt
│   │   │   ├── SearchScreen.kt
│   │   │   └── PatientProfileScreen.kt
│   │   └── doctor/
│   │       ├── DoctorDashboardScreen.kt
│   │       ├── DoctorAppointmentsScreen.kt
│   │       ├── DoctorHospitalScreen.kt
│   │       ├── DoctorNotificationsScreen.kt
│   │       └── DoctorProfileScreen.kt
│   └── components/
│       ├── HospitalCard.kt
│       ├── DoctorCard.kt
│       ├── AppointmentCard.kt
│       ├── SpecialtyChip.kt
│       ├── StatusBadge.kt
│       ├── RatingBar.kt
│       ├── BottomNavBar.kt
│       ├── ShimmerLoader.kt
│       └── EmptyState.kt
├── viewmodels/
│   ├── AuthViewModel.kt
│   ├── HospitalViewModel.kt
│   ├── DoctorViewModel.kt
│   ├── AppointmentViewModel.kt
│   └── NotificationViewModel.kt
├── utils/
│   ├── SessionManager.kt     ← SharedPreferences for JWT + user info
│   ├── LocationHelper.kt     ← GPS/location utilities
│   └── Extensions.kt
└── navigation/
    └── AppNavigation.kt      ← all routes defined here
```

---

## COLOR TOKENS (Color.kt)

```kotlin
val DeepTeal = Color(0xFF0B6E6E)
val SoftTeal = Color(0xFFE0F4F4)
val CoralOrange = Color(0xFFF26D50)
val CoralWash = Color(0xFFFEF0ED)
val MintGreen = Color(0xFF27AE7A)
val MintWash = Color(0xFFE6F7F1)
val AppBackground = Color(0xFFF7F9FC)
val SurfaceWhite = Color(0xFFFFFFFF)
val CharcoalText = Color(0xFF1A1A2E)
val SlateGray = Color(0xFF6B7280)
val BorderGray = Color(0xFFE5E7EB)
val DisabledGray = Color(0xFFD1D5DB)
val AmberStar = Color(0xFFF59E0B)
val IndigoDoctor = Color(0xFF4F46E5)
val IndigoLight = Color(0xFFEEF2FF)
```

---

## ANDROID SCREENS — IMPLEMENTATION INSTRUCTIONS

For every screen listed below, follow this exact process:

**Step 1:** Read the matching image from `C:\Users\HP\Documents\Application_developement\Doctor_patient\figmaresources\` using the terminal.

**Step 2:** Analyze the image — extract exact colors, layout structure, component sizes, spacing, and typography from the design.

**Step 3:** Implement the screen in Jetpack Compose matching the design pixel-perfectly.

**Step 4:** Wire the screen to its ViewModel and Retrofit API call.

**Step 5:** Handle all 3 states — loading (shimmer), success (content), error (empty state with retry button).

### Screens to implement in order:

#### 1. SplashScreen
- Teal background, app logo, app name "MedConnect", tagline
- After 2 seconds: check SessionManager for existing JWT → if valid navigate to correct dashboard (patient/doctor), else navigate to Onboarding
- Onboarding: 2-slide pager with dot indicators, "Sign up as Patient" filled button, "Sign up as Doctor" outlined button, "Log in" text link

#### 2. PatientSignUpScreen + PatientLoginScreen
- Sign up: full_name, email, phone, password, confirm_password fields
- Validation: email format, password min 8 chars, phone 10 digits, passwords match
- On submit: POST to `/auth/register_patient.php` → on success navigate to PatientHomeScreen
- Login: email + password → POST to `/auth/login.php` → store JWT + user_id + role in SessionManager → navigate accordingly
- Show inline field error messages in Coral Orange below each invalid field

#### 3. DoctorRegisterScreen
- 3-step form with progress indicator (step 1 / 2 / 3 shown as filled dots)
- Step 1: name, email, phone, password
- Step 2: license, specialization dropdown, years experience stepper, profile photo upload, bio
- Step 3: radio tiles — "Join existing hospital" vs "Create new hospital"
  - Join: search field → GET `/hospitals/get_all.php` → show autocomplete list
  - Create: hospital name, address, city, type dropdown, hospital photo upload
- On complete: POST to `/auth/register_doctor.php` with multipart form data

#### 4. PatientHomeScreen
- Top bar: avatar + greeting + bell icon with notification badge count
- Location pill: fetch device GPS via LocationHelper → display city name
- "Near you" horizontal scroll: GET `/hospitals/get_nearby.php?lat=X&lng=Y`
- "Browse by specialty" grid: 8 specialty chips (static data)
- "Top rated hospitals" vertical list: GET `/hospitals/get_all.php` sorted by rating
- Tapping hospital card → navigate to HospitalDetailScreen(hospitalId)
- Tapping specialty chip → navigate to SearchScreen with specialty filter pre-applied

#### 5. HospitalDetailScreen
- Receives hospitalId as nav argument
- GET `/hospitals/get_by_id.php?id=X`
- Hero image loaded with Coil from `ApiConfig.BASE_URL + "uploads/hospitals/" + filename`
- Displays: name, address, specialty tags, rating breakdown, open hours, about text (expandable), facilities horizontal scroll chips
- "Doctors at this hospital" section: horizontal scroll → GET `/doctors/get_by_hospital.php?hospital_id=X`
- Fixed bottom CTA: "Book an appointment" → navigate to DoctorListScreen for that hospital

#### 6. DoctorProfileScreen + BookingScreen
- Receives doctorId as nav argument
- GET `/doctors/get_profile.php?doctor_id=X`
- 3 tabs: About, Schedule, Reviews
- Schedule tab: 7-day horizontal date strip → on date tap GET `/slots/get_available.php?doctor_id=X&date=YYYY-MM-DD` → show time slot grid
- Available slot: teal outlined pill; Booked: gray strikethrough; Selected: teal filled
- Consultation type toggle: In-person / Video call
- "Confirm appointment" CTA: POST to `/appointments/book.php` with patient_id (from SessionManager), doctor_id, slot_id, consultation_type
- On success → navigate to AppointmentConfirmScreen

#### 7. AppointmentConfirmScreen
- Animated tick icon (use AnimatedVisibility with scale animation)
- Appointment summary card: doctor, hospital, date, time, type, "Confirmed" badge
- "View my appointments" → AppointmentsScreen; "Go home" → PatientHomeScreen

#### 8. PatientAppointmentsScreen
- Filter chips: All | Upcoming | Completed | Cancelled
- GET `/appointments/get_patient.php?patient_id=X`
- Filter locally by status
- Appointment card: left color bar (teal=upcoming, green=completed, red=cancelled), doctor info, date/time, status badge
- Upcoming cards: "Reschedule" ghost button (navigates to reschedule flow) + "Cancel" text link

#### 9. PatientNotificationsScreen + DoctorNotificationsScreen
- GET `/notifications/get.php?user_id=X`
- Unread items: light teal left border; read: plain white
- Mark as read on tap (update local state)
- Notification types: appointment (calendar icon), general (bell icon), rating (star icon)

#### 10. SearchScreen
- Auto-focused search bar on open
- Default state: recent searches (stored in SharedPreferences), popular specialties grid
- On type: GET `/hospitals/get_all.php` filtered client-side by name match
- Filter bottom sheet: distance slider, specialty multi-select, rating minimum stars, hospital type pills
- "Apply filters" → filter results and dismiss sheet

#### 11. PatientProfileScreen
- Display user info from SessionManager
- Menu list: My Appointments, Saved Hospitals, Notifications Settings, Help, Privacy Policy, Log Out
- Log out: clear SessionManager → navigate to SplashScreen

#### 12. DoctorDashboardScreen
- Greeting + today's date
- Status toggle card: Available | Busy | In Surgery — POST `/doctors/update_status.php` on change
- When "In Surgery" selected: overlay entire screen content with semi-transparent gray + banner "Currently in surgery — availability paused"
- Today's appointments: GET `/appointments/get_doctor.php?doctor_id=X` filtered to today + pending status
- Pending appointment cards: Accept button (POST `/appointments/accept.php`) | Reject button (POST `/appointments/reject.php`)
- Stats row: today's count + pending count (computed from API response)

#### 13. DoctorAppointmentsScreen
- Filter chips: All | Pending | Accepted | Rescheduled | Completed
- GET `/appointments/get_doctor.php?doctor_id=X`
- Accepted cards: "Reschedule" button → shows bottom sheet with date picker + time slot picker + reason field → POST `/appointments/reschedule.php`
- Accepted cards: "Mark complete" button → POST to update status = completed
- Bottom sheet: drag handle bar, date picker, slot picker grid, reason text field, "Confirm reschedule" CTA

#### 14. DoctorHospitalScreen
- GET `/hospitals/get_by_id.php?id=X` (doctor's hospital_id from SessionManager)
- Stats row: total doctors, average rating, today's appointments
- Rating breakdown: 5-bar horizontal bar chart using Canvas composable
- Registered doctors list: each row has avatar, name, specialty chip, status dot, today's appointment count

---

## NAVIGATION SETUP (AppNavigation.kt)

```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object PatientLogin : Screen("patient_login")
    object PatientSignUp : Screen("patient_signup")
    object DoctorRegister : Screen("doctor_register")
    object PatientHome : Screen("patient_home")
    object HospitalDetail : Screen("hospital_detail/{hospitalId}")
    object DoctorProfile : Screen("doctor_profile/{doctorId}")
    object Booking : Screen("booking/{doctorId}")
    object AppointmentConfirm : Screen("appointment_confirm/{appointmentId}")
    object PatientAppointments : Screen("patient_appointments")
    object PatientNotifications : Screen("patient_notifications")
    object Search : Screen("search")
    object PatientProfile : Screen("patient_profile")
    object DoctorDashboard : Screen("doctor_dashboard")
    object DoctorAppointments : Screen("doctor_appointments")
    object DoctorHospital : Screen("doctor_hospital")
    object DoctorNotifications : Screen("doctor_notifications")
    object DoctorProfileEdit : Screen("doctor_profile_edit")
}
```

After login, check `SessionManager.getRole()`:
- `"patient"` → navigate to `Screen.PatientHome`, pop all back stack
- `"doctor"` → navigate to `Screen.DoctorDashboard`, pop all back stack

---

## SESSIONMANAGER (utils/SessionManager.kt)

```kotlin
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("medconnect_prefs", Context.MODE_PRIVATE)

    fun saveSession(token: String, userId: Int, role: String, name: String) {
        prefs.edit()
            .putString("jwt_token", token)
            .putInt("user_id", userId)
            .putString("user_role", role)
            .putString("user_name", name)
            .apply()
    }

    fun getToken(): String? = prefs.getString("jwt_token", null)
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getRole(): String? = prefs.getString("user_role", null)
    fun getName(): String? = prefs.getString("user_name", null)
    fun isLoggedIn(): Boolean = getToken() != null
    fun clearSession() = prefs.edit().clear().apply()
}
```

---

## RETROFIT SETUP

### RetrofitInstance.kt
```kotlin
object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build()
            )
            .build()
            .create(ApiService::class.java)
    }
}
```

### ApiService.kt (define all endpoints as interface functions)
```kotlin
interface ApiService {
    @POST("auth/register_patient.php")
    suspend fun registerPatient(@Body body: RegisterPatientRequest): Response<AuthResponse>

    @POST("auth/login.php")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("hospitals/get_all.php")
    suspend fun getAllHospitals(): Response<HospitalListResponse>

    @GET("hospitals/get_nearby.php")
    suspend fun getNearbyHospitals(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 10
    ): Response<HospitalListResponse>

    @GET("hospitals/get_by_id.php")
    suspend fun getHospitalById(@Query("id") id: Int): Response<HospitalDetailResponse>

    @GET("doctors/get_by_hospital.php")
    suspend fun getDoctorsByHospital(@Query("hospital_id") hospitalId: Int): Response<DoctorListResponse>

    @GET("slots/get_available.php")
    suspend fun getAvailableSlots(
        @Query("doctor_id") doctorId: Int,
        @Query("date") date: String
    ): Response<SlotListResponse>

    @POST("appointments/book.php")
    suspend fun bookAppointment(@Body body: BookAppointmentRequest): Response<AppointmentResponse>

    @GET("appointments/get_patient.php")
    suspend fun getPatientAppointments(@Query("patient_id") patientId: Int): Response<AppointmentListResponse>

    @GET("appointments/get_doctor.php")
    suspend fun getDoctorAppointments(@Query("doctor_id") doctorId: Int): Response<AppointmentListResponse>

    @POST("appointments/accept.php")
    suspend fun acceptAppointment(@Body body: AppointmentActionRequest): Response<BaseResponse>

    @POST("appointments/reject.php")
    suspend fun rejectAppointment(@Body body: AppointmentActionRequest): Response<BaseResponse>

    @POST("appointments/reschedule.php")
    suspend fun rescheduleAppointment(@Body body: RescheduleRequest): Response<BaseResponse>

    @POST("doctors/update_status.php")
    suspend fun updateDoctorStatus(@Body body: StatusUpdateRequest): Response<BaseResponse>

    @GET("notifications/get.php")
    suspend fun getNotifications(@Query("user_id") userId: Int): Response<NotificationListResponse>

    @Multipart
    @POST("hospitals/create.php")
    suspend fun createHospital(
        @Part("name") name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("type") type: RequestBody,
        @Part photo: MultipartBody.Part?,
        @Part("created_by") createdBy: RequestBody
    ): Response<HospitalCreateResponse>
}
```

---

## COMPONENT BEHAVIOR SPECIFICATIONS

### ShimmerLoader
Implement a shimmer effect using an animated `InfiniteTransition` with a gradient sweep animation. Use it in every screen while API data is loading. Show 3 skeleton cards of the same shape as the real cards.

### EmptyState
Props: icon (ImageVector), title (String), subtitle (String), buttonLabel (String?), onButtonClick (() -> Unit)?
Design: centered illustration area (use a simple Composable drawing), title in CharcoalText H3, subtitle in SlateGray body, optional teal button.

### BottomNavBar
Patient tabs: Home, Search, Appointments, Notifications, Profile
Doctor tabs: Dashboard, Schedule, Hospital, Notifications, Profile
Active tab: icon + label in DeepTeal; inactive: DisabledGray. Height: 64dp. White background with top BorderGray 0.5dp divider.

### StatusBadge
```kotlin
@Composable
fun StatusBadge(status: String) {
    val (bg, text, label) = when (status) {
        "pending" -> Triple(SoftTeal, DeepTeal, "Pending")
        "accepted" -> Triple(MintWash, MintGreen, "Accepted")
        "rejected" -> Triple(CoralWash, CoralOrange, "Rejected")
        "completed" -> Triple(MintWash, MintGreen, "Completed")
        "rescheduled" -> Triple(Color(0xFFFFF8E1), Color(0xFFF59E0B), "Rescheduled")
        "cancelled" -> Triple(CoralWash, CoralOrange, "Cancelled")
        else -> Triple(BorderGray, SlateGray, status)
    }
    // render pill with bg fill, text color, 20dp corner radius
}
```

---

## GRADLE DEPENDENCIES (app/build.gradle)

```gradle
dependencies {
    // Jetpack Compose BOM
    implementation platform('androidx.compose:compose-bom:2024.04.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.activity:activity-compose:1.9.0'

    // Navigation
    implementation 'androidx.navigation:navigation-compose:2.7.7'

    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

    // Retrofit + OkHttp
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // Coil (image loading)
    implementation 'io.coil-kt:coil-compose:2.6.0'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0'

    // DataStore / SharedPreferences
    implementation 'androidx.datastore:datastore-preferences:1.1.1'

    // Location
    implementation 'com.google.android.gms:play-services-location:21.2.0'
}
```

---

## AndroidManifest.xml PERMISSIONS

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

Add `android:usesCleartextTraffic="true"` to the `<application>` tag to allow HTTP connections to the local XAMPP server.

---

## IMPLEMENTATION ORDER

Execute in this exact order. Do not start the next step until the current one is verified working.

1. **Create MySQL database and all tables** using the SQL above. Verify in phpMyAdmin.
2. **Create all PHP files** in `C:\xampp\htdocs\hospiq\` starting with `config/db.php`, then auth files, then hospitals, doctors, appointments, slots, notifications.
3. **Test each PHP endpoint** using a browser or Postman before wiring Android.
4. **Create Android project** with package `com.medconnect`. Add all Gradle dependencies.
5. **Implement ApiConfig.kt** with your machine's local IP address (run `ipconfig` in Windows CMD to find it — look for IPv4 Address under your Wi-Fi adapter).
6. **Implement all data classes, ApiService, RetrofitInstance, SessionManager**.
7. **Implement screens in this order:**
   - SplashScreen → Onboarding → PatientSignUp → PatientLogin → DoctorRegister
   - PatientHome → HospitalDetail → DoctorProfile → BookingScreen → AppointmentConfirm
   - PatientAppointments → PatientNotifications → SearchScreen → PatientProfile
   - DoctorDashboard → DoctorAppointments → DoctorHospital → DoctorNotifications
8. **Wire navigation** between all screens.
9. **End-to-end test:** Register patient → browse hospitals → book appointment → open doctor dashboard → accept appointment → verify patient sees "Accepted" status.

---

## IMPORTANT RULES FOR THE AGENT

- Never hardcode any IP address outside of `ApiConfig.kt`
- Never hardcode any user_id or token — always read from `SessionManager`
- Every API call must be inside a `viewModelScope.launch` coroutine with try-catch
- Every screen must handle loading / success / error states
- UI design images at `C:\Users\HP\Documents\Application_developement\Doctor_patient\figmaresources\` are the source of truth — read them before implementing each screen
- PHP files go to `C:\xampp\htdocs\hospiq\` only — no other location
- Database name is `hospiq` — do not create or use any other database name
- Do not use any external authentication service — implement JWT manually in PHP
- All image uploads are stored in `C:\xampp\htdocs\hospiq\uploads\` and served via `ApiConfig.BASE_URL + "uploads/..."`