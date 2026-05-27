package com.simats.hospiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.hospiq.navigation.*
import com.simats.hospiq.network.RetrofitInstance
import com.simats.hospiq.ui.screens.appointments.PatientAppointmentsScreen
import com.simats.hospiq.ui.screens.auth.DoctorRegisterScreen
import com.simats.hospiq.ui.screens.auth.PatientLoginScreen
import com.simats.hospiq.ui.screens.auth.PatientSignUpScreen
import com.simats.hospiq.ui.screens.booking.AppointmentConfirmScreen
import com.simats.hospiq.ui.screens.booking.DoctorProfileScreen
import com.simats.hospiq.ui.screens.doctor.*
import com.simats.hospiq.ui.screens.home.PatientHomeScreen
import com.simats.hospiq.ui.screens.hospital.HospitalDetailScreen
import com.simats.hospiq.ui.screens.notifications.PatientNotificationsScreen
import com.simats.hospiq.ui.screens.onboarding.OnboardingScreen
import com.simats.hospiq.ui.screens.profile.PatientProfileScreen
import com.simats.hospiq.ui.screens.search.SearchScreen
import com.simats.hospiq.ui.screens.splash.SplashScreen
import com.simats.hospiq.ui.theme.HospiQTheme
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentViewModel
import com.simats.hospiq.viewmodels.AuthViewModel
import com.simats.hospiq.viewmodels.DoctorViewModel
import com.simats.hospiq.viewmodels.HospitalViewModel
import com.simats.hospiq.viewmodels.NotificationViewModel

import com.simats.hospiq.utils.NotificationService
import com.simats.hospiq.utils.AppointmentReminderWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)
        // Wire token into Retrofit so every request is authenticated
        RetrofitInstance.tokenProvider = { sessionManager.getToken() }

        // Initialize Notification Channels and schedule periodic background reminder worker
        NotificationService.createChannels(this)
        AppointmentReminderWorker.schedule(this)

        setContent {
            HospiQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    HospiQNavHost(
                        navController = navController,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}

@Composable
fun HospiQNavHost(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val hospitalViewModel: HospitalViewModel = viewModel()
    val doctorViewModel: DoctorViewModel = viewModel()
    val appointmentViewModel: AppointmentViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        // ── Splash ──────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                sessionManager = sessionManager,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPatientHome = {
                    navController.navigate(Screen.PatientHome.route) { popUpTo(0) }
                },
                onNavigateToDoctorDashboard = {
                    navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) }
                }
            )
        }

        // ── Onboarding ──────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToPatientSignUp = { navController.navigate(Screen.PatientSignUp.route) },
                onNavigateToDoctorRegister = { navController.navigate(Screen.DoctorRegister.route) },
                onNavigateToLogin = { navController.navigate(Screen.PatientLogin.route) }
            )
        }

        // ── Auth ────────────────────────────────────────────────────────────
        composable(Screen.PatientSignUp.route) {
            PatientSignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = { token, userId, role, name, hospitalId, phone, profilePhoto, doctorId ->
                    sessionManager.saveSession(token, userId, role, name, hospitalId, phone, profilePhoto, doctorId)
                    navController.navigate(Screen.PatientHome.route) { popUpTo(0) }
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.PatientLogin.route) }
            )
        }

        composable(Screen.PatientLogin.route) {
            PatientLoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { token, userId, role, name, hospitalId, phone, profilePhoto, doctorId ->
                    sessionManager.saveSession(token, userId, role, name, hospitalId, phone, profilePhoto, doctorId)
                    // ✅ Route based on actual role from server
                    if (role == "doctor") {
                        navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) }
                    } else {
                        navController.navigate(Screen.PatientHome.route) { popUpTo(0) }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToSignUp = { navController.navigate(Screen.PatientSignUp.route) }
            )
        }

        composable(Screen.DoctorRegister.route) {
            DoctorRegisterScreen(
                authViewModel = authViewModel,
                onRegistrationComplete = { token, userId, role, name, hospitalId, phone, profilePhoto, doctorId ->
                    sessionManager.saveSession(token, userId, role, name, hospitalId, phone, profilePhoto, doctorId)
                    navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Patient Home ─────────────────────────────────────────────────────
        composable(Screen.PatientHome.route) {
            PatientHomeScreen(
                sessionManager = sessionManager,
                hospitalViewModel = hospitalViewModel,
                appointmentViewModel = appointmentViewModel,
                onNavigateToHospitalDetail = { id -> navController.navigate(hospitalDetailRoute(id)) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToAppointments = { navController.navigate(Screen.PatientAppointments.route) },
                onNavigateToNotifications = { navController.navigate(Screen.PatientNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) }
            )
        }

        // ── Hospital Detail ──────────────────────────────────────────────────
        composable(
            route = Screen.HospitalDetail.route,
            arguments = listOf(navArgument("hospitalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getInt("hospitalId") ?: 1
            HospitalDetailScreen(
                hospitalId = hospitalId,
                hospitalViewModel = hospitalViewModel,
                onDoctorClick = { doctorId -> navController.navigate(doctorProfileRoute(doctorId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Doctor Profile / Booking ─────────────────────────────────────────
        composable(
            route = Screen.DoctorProfile.route,
            arguments = listOf(navArgument("doctorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 1
            DoctorProfileScreen(
                doctorId = doctorId,
                doctorViewModel = doctorViewModel,
                appointmentViewModel = appointmentViewModel,
                sessionManager = sessionManager,
                onBackClick = { navController.popBackStack() },
                onConfirmAppointment = { _, _, appointmentId ->
                    navController.navigate(appointmentConfirmRoute(appointmentId))
                }
            )
        }

        // ── Appointment Confirm ──────────────────────────────────────────────
        composable(
            route = Screen.AppointmentConfirm.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getInt("appointmentId") ?: 1
            AppointmentConfirmScreen(
                appointmentId = appointmentId,
                appointmentViewModel = appointmentViewModel,
                sessionManager = sessionManager,
                onViewAppointments = {
                    navController.navigate(Screen.PatientAppointments.route) {
                        popUpTo(Screen.PatientHome.route)
                    }
                },
                onGoHome = {
                    navController.navigate(Screen.PatientHome.route) { popUpTo(0) }
                }
            )
        }

        // ── Patient Appointments ─────────────────────────────────────────────
        composable(Screen.PatientAppointments.route) {
            PatientAppointmentsScreen(
                sessionManager = sessionManager,
                appointmentViewModel = appointmentViewModel,
                onNavigateToHome = { navController.navigate(Screen.PatientHome.route) { popUpTo(0) } },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToNotifications = { navController.navigate(Screen.PatientNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) }
            )
        }

        // ── Search ───────────────────────────────────────────────────────────
        composable(Screen.Search.route) {
            SearchScreen(
                hospitalViewModel = hospitalViewModel,
                onHospitalClick = { id -> navController.navigate(hospitalDetailRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.PatientHome.route) { popUpTo(0) } },
                onNavigateToAppointments = { navController.navigate(Screen.PatientAppointments.route) },
                onNavigateToNotifications = { navController.navigate(Screen.PatientNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) }
            )
        }

        // ── Patient Notifications ────────────────────────────────────────────
        composable(Screen.PatientNotifications.route) {
            PatientNotificationsScreen(
                sessionManager = sessionManager,
                notificationViewModel = notificationViewModel,
                onNavigateToHome = { navController.navigate(Screen.PatientHome.route) { popUpTo(0) } },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToAppointments = { navController.navigate(Screen.PatientAppointments.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) }
            )
        }

        // ── Patient Profile ──────────────────────────────────────────────────
        composable(Screen.PatientProfile.route) {
            PatientProfileScreen(
                sessionManager = sessionManager,
                authViewModel = authViewModel,
                appointmentViewModel = appointmentViewModel,
                doctorViewModel = doctorViewModel,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Onboarding.route) { popUpTo(0) }
                },
                onNavigateToHome = {
                    if (sessionManager.getRole() == "doctor") {
                        navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) }
                    } else {
                        navController.navigate(Screen.PatientHome.route) { popUpTo(0) }
                    }
                },
                onNavigateToSearch = {
                    if (sessionManager.getRole() == "doctor") {
                        navController.navigate(Screen.DoctorHospital.route)
                    } else {
                        navController.navigate(Screen.Search.route)
                    }
                },
                onNavigateToAppointments = {
                    if (sessionManager.getRole() == "doctor") {
                        navController.navigate(Screen.DoctorAppointments.route)
                    } else {
                        navController.navigate(Screen.PatientAppointments.route)
                    }
                },
                onNavigateToNotifications = {
                    if (sessionManager.getRole() == "doctor") {
                        navController.navigate(Screen.DoctorNotifications.route)
                    } else {
                        navController.navigate(Screen.PatientNotifications.route)
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.NotificationSettings.route)
                }
            )
        }

        // ── Notification Settings ───────────────────────────────────────────
        composable(Screen.NotificationSettings.route) {
            com.simats.hospiq.ui.screens.settings.NotificationSettingsScreen(
                sessionManager = sessionManager,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Doctor Dashboard ─────────────────────────────────────────────────
        composable(Screen.DoctorDashboard.route) {
            DoctorDashboardScreen(
                sessionManager = sessionManager,
                appointmentViewModel = appointmentViewModel,
                doctorViewModel = doctorViewModel,
                onNavigateToHospital = { navController.navigate(Screen.DoctorHospital.route) },
                onNavigateToAppointments = { navController.navigate(Screen.DoctorAppointments.route) },
                onNavigateToNotifications = { navController.navigate(Screen.DoctorNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) }
            )
        }

        // ── Doctor Appointments ──────────────────────────────────────────────
        composable(Screen.DoctorAppointments.route) {
            DoctorAppointmentsScreen(
                sessionManager = sessionManager,
                appointmentViewModel = appointmentViewModel,
                onNavigateToHome = { navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) } },
                onNavigateToNotifications = { navController.navigate(Screen.DoctorNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) },
                onNavigateToHospital = { navController.navigate(Screen.DoctorHospital.route) }
            )
        }

        // ── Doctor Hospital ──────────────────────────────────────────────────
        composable(Screen.DoctorHospital.route) {
            DoctorHospitalScreen(
                sessionManager = sessionManager,
                hospitalViewModel = hospitalViewModel,
                onNavigateToHome = { navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) } },
                onNavigateToAppointments = { navController.navigate(Screen.DoctorAppointments.route) },
                onNavigateToNotifications = { navController.navigate(Screen.DoctorNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) },
                onNavigateToDoctorProfile = { doctorId -> navController.navigate(doctorProfileRoute(doctorId)) }
            )
        }

        // ── Doctor Notifications ─────────────────────────────────────────────
        composable(Screen.DoctorNotifications.route) {
            DoctorNotificationsScreen(
                sessionManager = sessionManager,
                notificationViewModel = notificationViewModel,
                onNavigateToHome = { navController.navigate(Screen.DoctorDashboard.route) { popUpTo(0) } },
                onNavigateToAppointments = { navController.navigate(Screen.DoctorAppointments.route) },
                onNavigateToProfile = { navController.navigate(Screen.PatientProfile.route) },
                onNavigateToHospital = { navController.navigate(Screen.DoctorHospital.route) }
            )
        }
    }
}
