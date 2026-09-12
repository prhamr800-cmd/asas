package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.ai.AiImageGenScreen
import com.example.ui.screens.ai.AiVoiceScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.calls.ActiveCallScreen
import com.example.ui.screens.conversation.ConversationScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.subscription.SubscriptionScreen
import com.example.ui.theme.PrivoTheme

class MainActivity : ComponentActivity() {

    private lateinit var container: PrivoAppContainer

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        container = PrivoAppContainer(applicationContext)

        // Request audio/notification permissions
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())

        setContent {
            val isDarkTheme by container.sessionManager.darkThemeFlow.collectAsState()

            PrivoTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivoNavApp(container = container)
                }
            }
        }
    }
}

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val CONVERSATION = "conversation/{chatId}/{chatName}/{chatEmoji}/{isGroup}"
    const val ACTIVE_CALL = "active_call"
    const val AI_IMAGE_GEN = "ai_image_gen"
    const val AI_VOICE = "ai_voice"
    const val SUBSCRIPTION = "subscription"
    const val SETTINGS = "settings"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    fun buildConversationRoute(chatId: String, name: String, emoji: String, isGroup: Boolean): String {
        return "conversation/$chatId/${android.net.Uri.encode(name)}/${android.net.Uri.encode(emoji)}/$isGroup"
    }
}

@Composable
fun PrivoNavApp(container: PrivoAppContainer) {
    val navController = rememberNavController()
    val activeCall by container.callManager.activeCall.collectAsState()

    // If an active call starts, navigate to call screen if not already there
    androidx.compose.runtime.LaunchedEffect(activeCall) {
        if (activeCall != null && navController.currentDestination?.route != NavRoutes.ACTIVE_CALL) {
            navController.navigate(NavRoutes.ACTIVE_CALL)
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        enterTransition = { fadeIn(tween(250)) },
        exitTransition = { fadeOut(tween(200)) }
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                isLoggedIn = container.sessionManager.isLoggedIn(),
                onNavigateNext = { loggedIn ->
                    val destination = if (loggedIn) NavRoutes.HOME else NavRoutes.LOGIN
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authRepository = container.authRepository,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.FORGOT_PASSWORD)
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                authRepository = container.authRepository,
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                authRepository = container.authRepository,
                onPasswordResetSuccess = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                container = container,
                onChatClick = { chatId, name, emoji, isGroup ->
                    navController.navigate(NavRoutes.buildConversationRoute(chatId, name, emoji, isGroup))
                },
                onStartCall = { name, emoji, isVideo ->
                    container.callManager.startCall(
                        chatId = "call_${name.hashCode()}",
                        recipientName = name,
                        recipientAvatar = emoji,
                        isVideo = isVideo
                    )
                    navController.navigate(NavRoutes.ACTIVE_CALL)
                },
                onNavigateToImageGen = {
                    navController.navigate(NavRoutes.AI_IMAGE_GEN)
                },
                onNavigateToVoiceAi = {
                    navController.navigate(NavRoutes.AI_VOICE)
                },
                onNavigateToSubscription = {
                    navController.navigate(NavRoutes.SUBSCRIPTION)
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(NavRoutes.ADMIN_DASHBOARD)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onLogout = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.CONVERSATION,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType },
                navArgument("chatEmoji") { type = NavType.StringType },
                navArgument("isGroup") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val rawName = backStackEntry.arguments?.getString("chatName") ?: "چت"
            val rawEmoji = backStackEntry.arguments?.getString("chatEmoji") ?: "💬"
            val isGroup = backStackEntry.arguments?.getBoolean("isGroup") ?: false

            val chatName = android.net.Uri.decode(rawName)
            val chatEmoji = android.net.Uri.decode(rawEmoji)

            ConversationScreen(
                chatId = chatId,
                chatName = chatName,
                chatEmoji = chatEmoji,
                isGroup = isGroup,
                container = container,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartCall = { isVideo ->
                    container.callManager.startCall(
                        chatId = chatId,
                        recipientName = chatName,
                        recipientAvatar = chatEmoji,
                        isVideo = isVideo
                    )
                    navController.navigate(NavRoutes.ACTIVE_CALL)
                }
            )
        }

        composable(NavRoutes.ACTIVE_CALL) {
            ActiveCallScreen(
                container = container,
                onCallEnded = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.AI_IMAGE_GEN) {
            AiImageGenScreen(
                aiRepository = container.aiRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.AI_VOICE) {
            AiVoiceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.SUBSCRIPTION) {
            SubscriptionScreen(
                subscriptionRepository = container.subscriptionRepository,
                currentUserFlow = container.sessionManager.currentUserFlow,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSubscription = {
                    navController.navigate(NavRoutes.SUBSCRIPTION)
                },
                onLogout = {
                    container.authRepository.logout()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                adminRepository = container.adminRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
