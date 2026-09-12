package com.example.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PrivoApiService {

    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("/api/verify-login-2fa")
    suspend fun verify2FA(@Body body: Map<String, String>): Response<LoginResponse>

    @POST("/api/request-password-reset-otp")
    suspend fun requestPasswordResetOtp(@Body request: RequestOtpRequest): Response<BaseResponse>

    @POST("/api/verify-password-reset-otp")
    suspend fun verifyPasswordResetOtp(@Body request: VerifyOtpRequest): Response<Map<String, Any>>

    @POST("/api/reset-password-with-otp")
    suspend fun resetPasswordWithOtp(@Body request: ResetPasswordRequest): Response<BaseResponse>

    @POST("/api/update-profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<BaseResponse>

    @GET("/api/v1/chats")
    suspend fun getChats(): Response<ChatsResponse>

    @GET("/api/v1/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String): Response<MessagesResponse>

    @POST("/api/send-message")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<SendMessageResponse>

    @POST("/api/livekit/token")
    suspend fun getLiveKitToken(@Body request: LiveKitTokenRequest): Response<LiveKitTokenResponse>

    @POST("/api/ai/chat")
    suspend fun sendAiChat(@Body request: AiChatRequest): Response<AiChatResponse>

    @POST("/api/ai/summarize")
    suspend fun summarizeConversation(@Body request: AiSummarizeRequest): Response<AiSummarizeResponse>

    @POST("/api/ai/translate")
    suspend fun translateText(@Body request: AiTranslateRequest): Response<AiTranslateResponse>

    @POST("/api/ai/suggest-replies")
    suspend fun suggestReplies(@Body request: AiSuggestRepliesRequest): Response<AiSuggestRepliesResponse>

    @POST("/api/ai/generate-image")
    suspend fun generateImage(@Body request: AiGenerateImageRequest): Response<AiGenerateImageResponse>

    @POST("/api/purchase-subscription")
    suspend fun purchaseSubscription(@Body request: PurchaseSubscriptionRequest): Response<BaseResponse>

    @GET("/api/admin/settings-metrics")
    suspend fun getAdminMetrics(): Response<AdminMetricsResponse>

    @POST("/api/admin/broadcast")
    suspend fun sendAdminBroadcast(@Body request: AdminBroadcastRequest): Response<BaseResponse>
}
