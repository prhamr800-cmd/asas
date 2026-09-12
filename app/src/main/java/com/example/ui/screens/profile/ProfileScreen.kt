package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.PrivoAppContainer
import com.example.ui.components.PrivoAvatar
import com.example.ui.theme.PrivoAmberWarning
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoRoseAccent
import com.example.ui.theme.PrivoVioletPrimary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    container: PrivoAppContainer,
    onNavigateToSubscription: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit
) {
    val currentUser by container.sessionManager.currentUserFlow.collectAsState()
    val isAppLockEnabled by container.sessionManager.appLockFlow.collectAsState()
    val isTwoFactorEnabled by container.sessionManager.twoFactorFlow.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val user = currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "تنظیمات و حساب کاربری",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    PrivoAvatar(
                        emoji = user?.avatarEmoji ?: "👤",
                        colorName = user?.avatarColor ?: "bg-indigo-600",
                        size = 84.dp,
                        showOnlineBadge = true,
                        isOnline = true
                    )
                    IconButton(
                        onClick = {
                            editNickname = user?.nickname ?: ""
                            editBio = user?.bio ?: ""
                            showEditDialog = true
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrivoVioletPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش پروفایل", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user?.nickname?.ifBlank { user.username } ?: "کاربر",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (user?.role == "owner" || user?.subscriptionTier == "plus") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Star, contentDescription = "ویژه", tint = PrivoAmberWarning, modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    text = "@${user?.username ?: "user"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!user?.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user!!.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subscription Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (user?.subscriptionTier == "plus") PrivoAmberWarning.copy(alpha = 0.2f)
                            else PrivoVioletPrimary.copy(alpha = 0.15f)
                        )
                        .clickable { onNavigateToSubscription() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (user?.subscriptionTier == "plus") PrivoAmberWarning else PrivoVioletPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user?.subscriptionTier == "plus") "اشتراک پلاس فعال است 👑" else "طرح رایگان (ارتقا به پلاس)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (user?.subscriptionTier == "plus") PrivoAmberWarning else PrivoVioletPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Firebase Cloud Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrivoEmeraldSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = PrivoEmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "اتصال ابری Firebase & Firestore",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "پایگاه داده ابری Firestore و احراز هویت متصل است",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Owner / Admin Dashboard Option
        if (user?.role == "owner" || user?.role == "admin" || user?.username.equals("parham", ignoreCase = true)) {
            SettingItem(
                icon = Icons.Default.AdminPanelSettings,
                iconTint = PrivoAmberWarning,
                title = "پنل مدیریت کل سیستم (پرهام)",
                subtitle = "آمار زنده کاربران، پیام‌ها و ارسال اطلاعیه",
                onClick = onNavigateToAdminDashboard
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Security Options Section
        Text(
            text = "امنیت و حریم خصوصی",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SettingToggleItem(
            icon = Icons.Default.Lock,
            title = "قفل برنامه با اثرانگشت / پین",
            checked = isAppLockEnabled,
            onCheckedChange = { container.sessionManager.setAppLockEnabled(it) }
        )

        SettingToggleItem(
            icon = Icons.Default.Fingerprint,
            title = "تأیید هویت دومرحله‌ای (2FA)",
            checked = isTwoFactorEnabled,
            onCheckedChange = { container.sessionManager.setTwoFactorEnabled(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preferences Section
        Text(
            text = "تنظیمات عمومی",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SettingItem(
            icon = Icons.Default.Settings,
            iconTint = PrivoCyanAccent,
            title = "تنظیمات پیشرفته پریوو",
            subtitle = "حریم خصوصی، امنیت، اعلان‌ها، نشست‌ها و حافظه",
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingItem(
            icon = Icons.Default.WorkspacePremium,
            iconTint = PrivoVioletPrimary,
            title = "خرید و ارتقای اشتراک پریوو پلاس",
            subtitle = "امکانات نامحدود AI، تماس‌های HD و نشان طلایی",
            onClick = onNavigateToSubscription
        )

        SettingItem(
            icon = Icons.Default.Language,
            iconTint = PrivoCyanAccent,
            title = "زبان برنامه",
            subtitle = "فارسی (پیش‌فرض) / English",
            onClick = { /* Toggle language */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = {
                container.authRepository.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("profile_logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrivoRoseAccent.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = PrivoRoseAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "خروج از حساب کاربری",
                color = PrivoRoseAccent,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("ویرایش مشخصات پروفایل") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        label = { Text("نام نمایشی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_edit_nickname_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("درباره شما (بیو)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("profile_edit_bio_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            container.authRepository.updateProfile(
                                nickname = editNickname.trim(),
                                bio = editBio.trim(),
                                avatarEmoji = user?.avatarEmoji ?: "👤",
                                avatarColor = user?.avatarColor ?: "bg-indigo-600"
                            )
                            showEditDialog = false
                        }
                    },
                    modifier = Modifier.testTag("profile_edit_save_button")
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrivoVioletPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PrivoVioletPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
