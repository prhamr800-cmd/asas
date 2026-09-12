package com.example.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoPurplePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onLogout: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("فارسی (Persian)") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var callsVibrateEnabled by remember { mutableStateOf(true) }
    var readReceiptsEnabled by remember { mutableStateOf(true) }
    var onlineStatusVisible by remember { mutableStateOf(true) }
    var lastSeenOption by remember { mutableStateOf("همه (Everyone)") }
    var aiSuggestionsEnabled by remember { mutableStateOf(true) }

    var showLastSeenDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showActiveSessionsDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var cacheSizeMb by remember { mutableStateOf(42.5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تنظیمات پریوو",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Privacy & Security
            SettingsSectionHeader(title = "حریم خصوصی و امنیت (Privacy & Security)")

            SettingsCard {
                SettingsClickableRow(
                    icon = Icons.Default.Visibility,
                    title = "آخرین بازدید (Last Seen)",
                    subtitle = lastSeenOption,
                    onClick = { showLastSeenDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = Icons.Default.Person,
                    title = "نمایش وضعیت آنلاین",
                    checked = onlineStatusVisible,
                    onCheckedChange = { onlineStatusVisible = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = Icons.Default.Check,
                    title = "رسید خوانده‌شدن پیام‌ها (Read Receipts)",
                    checked = readReceiptsEnabled,
                    onCheckedChange = { readReceiptsEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableRow(
                    icon = Icons.Default.Block,
                    title = "کاربران مسدود شده (Blocked Users)",
                    subtitle = "۰ کاربر",
                    onClick = { showBlockedUsersDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableRow(
                    icon = Icons.Default.Devices,
                    title = "نشست‌های فعال (Active Sessions)",
                    subtitle = "دستگاه فعلی: Android Native",
                    onClick = { showActiveSessionsDialog = true }
                )
            }

            // Section: Notifications
            SettingsSectionHeader(title = "اعلان‌ها و صداها (Notifications)")

            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "اعلان پیام‌های جدید",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "لرزش هنگام تماس",
                    checked = callsVibrateEnabled,
                    onCheckedChange = { callsVibrateEnabled = it }
                )
            }

            // Section: AI Settings
            SettingsSectionHeader(title = "تنظیمات هوش مصنوعی (AI Assistant)")

            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Default.Psychology,
                    title = "پیشنهاد هوشمند پاسخ‌ها (Smart Replies)",
                    checked = aiSuggestionsEnabled,
                    onCheckedChange = { aiSuggestionsEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableRow(
                    icon = Icons.Default.Psychology,
                    title = "ارتقای پلن هوش مصنوعی",
                    subtitle = "پریوو پلاس با دسترسی نامحدود به Gemini",
                    onClick = onNavigateToSubscription
                )
            }

            // Section: Appearance & Language
            SettingsSectionHeader(title = "ظاهر و زبان (Appearance & Language)")

            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Default.DarkMode,
                    title = "حالت تیره (Dark Mode)",
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableRow(
                    icon = Icons.Default.Language,
                    title = "زبان برنامه (Language)",
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
            }

            // Section: Storage & Data
            SettingsSectionHeader(title = "داده‌ها و حافظه (Data & Storage)")

            SettingsCard {
                SettingsClickableRow(
                    icon = Icons.Default.Storage,
                    title = "پاک‌سازی حافظه پنهان (Cache)",
                    subtitle = "${String.format("%.1f", cacheSizeMb)} مگابایت اشغال شده",
                    onClick = { showClearCacheDialog = true }
                )
            }

            // Section: About & Danger Zone
            SettingsSectionHeader(title = "درباره و حساب کاربری")

            SettingsCard {
                SettingsClickableRow(
                    icon = Icons.Default.Info,
                    title = "درباره پریوو (Privo)",
                    subtitle = "نسخه ۲.۴.۰ نیتیو اندروید - با رمزنگاری سرتاسری",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsClickableRow(
                    icon = Icons.Default.DeleteForever,
                    title = "حذف کامل حساب کاربری",
                    subtitle = "حذف دائمی تمام پیام‌ها و اطلاعات",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteAccountDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog: Last Seen Visibility
    if (showLastSeenDialog) {
        AlertDialog(
            onDismissRequest = { showLastSeenDialog = false },
            title = { Text("چه کسانی می‌توانند آخرین بازدید شما را ببینند؟") },
            text = {
                Column {
                    listOf("همه (Everyone)", "مخاطبین من (My Contacts)", "هیچ‌کس (Nobody)").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    lastSeenOption = option
                                    showLastSeenDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (lastSeenOption == option),
                                onClick = {
                                    lastSeenOption = option
                                    showLastSeenDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLastSeenDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }

    // Dialog: Language Selection
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("انتخاب زبان (Select Language)") },
            text = {
                Column {
                    listOf("فارسی (Persian)", "English (انگلیسی)").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedLanguage == lang),
                                onClick = {
                                    selectedLanguage = lang
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }

    // Dialog: Clear Cache
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("پاک‌سازی حافظه موقت") },
            text = { Text("آیا مایل به حذف تمام فایل‌های موقت و تصاویر کش شده هستید؟ این کار به آزاد شدن فضای گوشی کمک می‌کند.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        cacheSizeMb = 0.0
                        showClearCacheDialog = false
                    }
                ) {
                    Text("پاک کردن", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Dialog: Active Sessions
    if (showActiveSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showActiveSessionsDialog = false },
            title = { Text("دستگاه‌ها و نشست‌های فعال") },
            text = {
                Column {
                    Text(
                        "این نشست (فعال):",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = PrivoEmeraldSuccess
                    )
                    Text("Samsung Galaxy / Pixel - Android 14", style = MaterialTheme.typography.bodyMedium)
                    Text("IP: 192.168.1.104 • تهران، ایران", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "سایر نشست‌ها:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text("Privo Desktop Web (Chrome)", style = MaterialTheme.typography.bodyMedium)
                    Text("آخرین فعالیت: ۲ ساعت پیش", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showActiveSessionsDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }

    // Dialog: Blocked Users
    if (showBlockedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedUsersDialog = false },
            title = { Text("کاربران مسدود شده") },
            text = { Text("لیست مسدودی‌های شما در حال حاضر خالی است.") },
            confirmButton = {
                TextButton(onClick = { showBlockedUsersDialog = false }) {
                    Text("تأیید")
                }
            }
        )
    }

    // Dialog: Delete Account
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("حذف کامل حساب کاربری پریوو", color = MaterialTheme.colorScheme.error) },
            text = { Text("هشدار: تمام پیام‌ها، رسانه‌ها و مخاطبین شما برای همیشه حذف خواهند شد و این عملیات غیرقابل بازگشت است.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onLogout()
                    }
                ) {
                    Text("حذف حساب", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = PrivoPurplePrimary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrivoPurplePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrivoPurplePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrivoPurplePrimary
            )
        )
    }
}
