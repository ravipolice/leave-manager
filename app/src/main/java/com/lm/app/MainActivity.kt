package com.lm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lm.app.auth.GoogleAuthHelper
import com.lm.app.backup.BackupService
import com.lm.app.data.User
import com.lm.app.ui.screens.*
import com.lm.app.ui.viewmodel.LeaveViewModel
import com.lm.app.ui.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lm.app.data.repository.AuthRepository
import com.lm.app.worker.InactivityWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backupService: BackupService

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordActivity()
        scheduleInactivityWorker()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(backupService)
                }
            }
        }
    }

    private fun recordActivity() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().putLong("last_activity_timestamp", System.currentTimeMillis()).apply()
    }

    private fun scheduleInactivityWorker() {
        val workRequest = PeriodicWorkRequestBuilder<InactivityWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "inactivity_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun AppNavigation(backupService: BackupService) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()
    val leaveViewModel: LeaveViewModel = hiltViewModel()

    val currentUser by userViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Sync activity to cloud
            (context as? MainActivity)?.authRepository?.updateLastActive(user.kgid)
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null && navController.currentDestination?.route == "login") {
            // Only auto-navigate if user is already logged in at app start (not after registration)
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            if (previousRoute == null) { // Only at app start, no previous screen
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute !in listOf("login", "register", "forgot_pin"),
        drawerContent = {
            if (currentRoute !in listOf("login", "register", "forgot_pin")) {
                com.lm.app.ui.components.NavigationDrawer(
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope,
                    userViewModel = userViewModel,
                    backupService = backupService,
                    currentRoute = currentRoute,
                    onLogout = {
                        userViewModel.logout(context)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { user ->
                        userViewModel.setUser(user)
                        navController.navigate("dashboard") { 
                            popUpTo("login") { inclusive = true } 
                        } 
                    },
                    onRegisterClick = { email ->
                        val encoded = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
                        navController.navigate("register/$encoded")
                    },
                    onForgotPinClick = {
                        navController.navigate("forgot_pin")
                    }
                )
            }
            composable("forgot_pin") {
                ForgotPinScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPinResetSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "register/{email}",
                arguments = listOf(navArgument("email") { defaultValue = ""; type = NavType.StringType })
            ) { backStackEntry ->
                val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
                val googleEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())
                RegistrationScreen(
                    prefillEmail = googleEmail,
                    onRegistrationSuccess = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                LeaveDashboardScreen(
                    onNavigateToCl = { navController.navigate("entry/CL") },
                    onNavigateToEl = { navController.navigate("entry/EL") },
                    onNavigateToHpl = { navController.navigate("entry/HPL") },
                    onNavigateToWo = { navController.navigate("entry/WO") },
                    onNavigateToCcl = { navController.navigate("entry/CCL") },
                    onNavigateToMcl = { navController.navigate("entry/MCL") },
                    onNavigateToOther = { navController.navigate("entry/LWA") },
                    onNavigateToApplyLeave = { navController.navigate("apply_leave") },
                    onNavigateToReports = { navController.navigate("history") },
                    onNavigateToRules = { navController.navigate("rules") },
                    userViewModel = userViewModel,
                    leaveViewModel = leaveViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(
                route = "entry/{type}",
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "CL"
                LeaveEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToApplyLeave = { type -> navController.navigate("apply_leave/$type") },
                    preselectedType = type,
                    userViewModel = userViewModel,
                    leaveViewModel = leaveViewModel
                )
            }
            composable(
                route = "apply_leave/{type}",
                arguments = listOf(navArgument("type") { defaultValue = "CL"; type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "CL"
                ApplyLeaveScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    userViewModel = userViewModel,
                    leaveViewModel = leaveViewModel,
                    initialType = type
                )
            }
            composable("apply_leave") {
                ApplyLeaveScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    userViewModel = userViewModel,
                    leaveViewModel = leaveViewModel,
                    initialType = "CL"
                )
            }
            composable("history") {
                LeaveHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel,
                    leaveViewModel = leaveViewModel
                )
            }
            composable("rules") {
                LeaveRulesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel
                )
            }
        }
    }
}
