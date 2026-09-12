package com.example.core.security

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("privo_session_prefs", Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<User?>(loadUser())
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    fun saveSession(sessionId: String, user: User) {
        prefs.edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_NICKNAME, user.nickname)
            .putString(KEY_BIO, user.bio)
            .putString(KEY_ROLE, user.role)
            .putString(KEY_TIER, user.subscriptionTier)
            .putString(KEY_AVATAR_COLOR, user.avatarColor)
            .putString(KEY_AVATAR_EMOJI, user.avatarEmoji)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        _currentUserFlow.value = user
    }

    fun getSessionId(): String? = prefs.getString(KEY_SESSION_ID, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getSessionId() != null

    fun loadUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val nickname = prefs.getString(KEY_NICKNAME, username) ?: username
        val bio = prefs.getString(KEY_BIO, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "user") ?: "user"
        val tier = prefs.getString(KEY_TIER, "free") ?: "free"
        val avatarColor = prefs.getString(KEY_AVATAR_COLOR, "bg-indigo-600") ?: "bg-indigo-600"
        val avatarEmoji = prefs.getString(KEY_AVATAR_EMOJI, "👤") ?: "👤"

        return User(
            id = id,
            username = username,
            nickname = nickname,
            bio = bio,
            role = role,
            subscriptionTier = tier,
            avatarColor = avatarColor,
            avatarEmoji = avatarEmoji,
            isOnline = true
        )
    }

    fun updateProfile(nickname: String, bio: String, avatarEmoji: String, avatarColor: String) {
        prefs.edit()
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_BIO, bio)
            .putString(KEY_AVATAR_EMOJI, avatarEmoji)
            .putString(KEY_AVATAR_COLOR, avatarColor)
            .apply()
        _currentUserFlow.value = loadUser()
    }

    fun updateSubscriptionTier(tier: String) {
        prefs.edit().putString(KEY_TIER, tier).apply()
        _currentUserFlow.value = loadUser()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_ID)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_NICKNAME)
            .remove(KEY_BIO)
            .remove(KEY_ROLE)
            .remove(KEY_TIER)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _currentUserFlow.value = null
    }

    // App Preferences
    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "fa") ?: "fa"
    fun setLanguage(lang: String) = prefs.edit().putString(KEY_LANGUAGE, lang).apply()

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK, false)
    fun setAppLockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()

    fun isTwoFactorEnabled(): Boolean = prefs.getBoolean(KEY_2FA, false)
    fun setTwoFactorEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_2FA, enabled).apply()

    companion object {
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_BIO = "bio"
        private const val KEY_ROLE = "role"
        private const val KEY_TIER = "subscription_tier"
        private const val KEY_AVATAR_COLOR = "avatar_color"
        private const val KEY_AVATAR_EMOJI = "avatar_emoji"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_APP_LOCK = "app_lock"
        private const val KEY_2FA = "two_factor_auth"
    }
}
