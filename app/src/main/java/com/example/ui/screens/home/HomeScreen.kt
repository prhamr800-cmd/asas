package com.example.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.PrivoAppContainer
import com.example.ui.screens.ai.AiHubScreen
import com.example.ui.screens.calls.CallsListScreen
import com.example.ui.screens.chats.ChatsListScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoVioletPrimary

data class NavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun HomeScreen(
    container: PrivoAppContainer,
    onChatClick: (chatId: String, name: String, emoji: String, isGroup: Boolean) -> Unit,
    onStartCall: (name: String, emoji: String, isVideo: Boolean) -> Unit,
    onNavigateToImageGen: () -> Unit,
    onNavigateToVoiceAi: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        NavTab("گفتگوها", Icons.Default.ChatBubble, "nav_chats_tab"),
        NavTab("تماس‌ها", Icons.Default.Call, "nav_calls_tab"),
        NavTab("هوش مصنوعی", Icons.Default.AutoAwesome, "nav_ai_tab"),
        NavTab("پروفایل", Icons.Default.Person, "nav_profile_tab")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) {
                                    if (tab.icon == Icons.Default.AutoAwesome) PrivoCyanAccent else PrivoVioletPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PrivoVioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = PrivoVioletPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ChatsListScreen(
                    chatRepository = container.chatRepository,
                    onChatClick = onChatClick
                )
                1 -> CallsListScreen(
                    onStartCall = onStartCall
                )
                2 -> AiHubScreen(
                    aiRepository = container.aiRepository,
                    onNavigateToImageGen = onNavigateToImageGen,
                    onNavigateToVoiceAi = onNavigateToVoiceAi
                )
                3 -> ProfileScreen(
                    container = container,
                    onNavigateToSubscription = onNavigateToSubscription,
                    onNavigateToAdminDashboard = onNavigateToAdminDashboard,
                    onLogout = onLogout
                )
            }
        }
    }
}
