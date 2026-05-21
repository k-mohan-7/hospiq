package com.simats.hospiq.viewmodels

// HomeViewModel is now superseded by HospitalViewModel.
// PatientHomeScreen should use HospitalViewModel directly.
// Keeping this file for backward compatibility — it delegates to HospitalViewModel logic.
typealias HomeViewModel = HospitalViewModel
typealias HomeUiState = HospitalUiState
