package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(username: String, pass: String, twoFactorCode: String? = null): Result<User>
    suspend fun register(username: String, nickname: String, pass: String, bio: String): Result<User>
    suspend fun signInWithGoogle(): Result<User>
    suspend fun signInAnonymously(): Result<User>
    suspend fun requestPasswordResetOtp(username: String): Result<String>
    suspend fun verifyResetOtp(username: String, otp: String): Result<String>
    suspend fun resetPassword(username: String, token: String, newPass: String): Result<Unit>
    suspend fun updateProfile(nickname: String, bio: String, avatarEmoji: String, avatarColor: String): Result<Unit>
    fun logout()
}
