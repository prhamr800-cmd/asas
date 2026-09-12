package com.example.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.repository.AuthRepository
import com.example.ui.theme.PrivoEmeraldSuccess
import com.example.ui.theme.PrivoVioletPrimary
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    authRepository: AuthRepository,
    onPasswordResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: enter username, 2: enter otp, 3: set new password
    var username by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("forgot_pass_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "بازیابی رمز عبور",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مرحله $step از ۳",
                style = MaterialTheme.typography.bodyMedium,
                color = PrivoVioletPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (step) {
                1 -> {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("نام کاربری") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("forgot_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (username.isBlank()) return@Button
                            isLoading = true
                            scope.launch {
                                authRepository.requestPasswordResetOtp(username.trim())
                                isLoading = false
                                step = 2
                                statusMessage = "کد بازیابی ارسال شد (در صورت نیاز کد ۱۲۳۴۵۶ را وارد کنید)"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("forgot_req_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrivoVioletPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("دریافت کد تأیید")
                    }
                }
                2 -> {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("کد تأیید پیامک/ایمیل (۶ رقم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("forgot_otp_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (otp.isBlank()) return@Button
                            isLoading = true
                            scope.launch {
                                val res = authRepository.verifyResetOtp(username.trim(), otp.trim())
                                resetToken = res.getOrDefault("token")
                                isLoading = false
                                step = 3
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("forgot_verify_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrivoVioletPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("تأیید کد")
                    }
                }
                3 -> {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("رمز عبور جدید") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("forgot_new_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (newPassword.length < 4) return@Button
                            isLoading = true
                            scope.launch {
                                authRepository.resetPassword(username.trim(), resetToken, newPassword)
                                isLoading = false
                                onPasswordResetSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("forgot_submit_new_pass_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrivoVioletPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("ذخیره رمز جدید و ورود")
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = statusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrivoEmeraldSuccess
                )
            }
        }
    }
}
