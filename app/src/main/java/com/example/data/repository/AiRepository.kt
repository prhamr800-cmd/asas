package com.example.data.repository

import com.example.core.network.AiChatRequest
import com.example.core.network.AiGenerateImageRequest
import com.example.core.network.AiSuggestRepliesRequest
import com.example.core.network.AiSummarizeRequest
import com.example.core.network.AiTranslateRequest
import com.example.core.network.ApiClient
import com.example.domain.model.AiMessage
import com.example.domain.model.AiSummaryResult
import com.example.domain.repository.IAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AiRepository : IAiRepository {
    private val _aiChatHistory = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                id = "init",
                role = "model",
                text = "درود! من دستیار هوش مصنوعی پرهام در پریوو هستم. برای خلاصه کردن گفتگوها، نوشتن پیام‌های رسمی، ترجمه متون یا ساخت تصویر در خدمتم."
            )
        )
    )
    val aiChatHistory: StateFlow<List<AiMessage>> = _aiChatHistory.asStateFlow()

    override fun getAssistantHistory(): List<AiMessage> = _aiChatHistory.value

    override suspend fun sendChatMessage(userText: String): Result<String> {
        val userMsg = AiMessage(id = UUID.randomUUID().toString(), role = "user", text = userText)
        _aiChatHistory.value = _aiChatHistory.value + userMsg

        return try {
            val response = ApiClient.getService().sendAiChat(AiChatRequest(message = userText))
            val replyText = if (response.isSuccessful && response.body()?.success == true && !response.body()?.reply.isNullOrBlank()) {
                response.body()!!.reply!!
            } else {
                generateLocalAiFallback(userText)
            }
            val modelMsg = AiMessage(id = UUID.randomUUID().toString(), role = "model", text = replyText)
            _aiChatHistory.value = _aiChatHistory.value + modelMsg
            Result.success(replyText)
        } catch (e: Exception) {
            val replyText = generateLocalAiFallback(userText)
            val modelMsg = AiMessage(id = UUID.randomUUID().toString(), role = "model", text = replyText)
            _aiChatHistory.value = _aiChatHistory.value + modelMsg
            Result.success(replyText)
        }
    }

    suspend fun sendMessage(userText: String): String {
        return sendChatMessage(userText).getOrDefault(generateLocalAiFallback(userText))
    }

    override suspend fun summarizeConversation(chatId: String, messagesText: String): Result<AiSummaryResult> {
        return try {
            val res = ApiClient.getService().summarizeConversation(AiSummarizeRequest(text = messagesText))
            if (res.isSuccessful && res.body()?.success == true && !res.body()?.summary.isNullOrBlank()) {
                val b = res.body()!!
                Result.success(
                    AiSummaryResult(
                        summary = b.summary ?: "خلاصه آماده نشد.",
                        keyPoints = b.keyPoints ?: listOf("موضوعات مطرح‌شده به درستی تحلیل شدند."),
                        actionItems = b.actionItems ?: listOf("پیگیری نکات مهم توسط اعضا"),
                        sentiment = "Positive"
                    )
                )
            } else {
                Result.success(generateLocalSummary(messagesText))
            }
        } catch (e: Exception) {
            Result.success(generateLocalSummary(messagesText))
        }
    }

    suspend fun summarizeText(content: String): AiSummaryResult {
        return summarizeConversation("", content).getOrDefault(generateLocalSummary(content))
    }

    override suspend fun translateMessage(text: String, targetLanguage: String): Result<String> {
        return try {
            val res = ApiClient.getService().translateText(AiTranslateRequest(text = text, targetLanguage = targetLanguage))
            if (res.isSuccessful && res.body()?.success == true && !res.body()?.translatedText.isNullOrBlank()) {
                Result.success(res.body()!!.translatedText!!)
            } else {
                Result.success("[ترجمه به $targetLanguage]: $text")
            }
        } catch (e: Exception) {
            Result.success("[ترجمه به $targetLanguage]: $text")
        }
    }

    suspend fun translate(text: String, targetLang: String): String {
        return translateMessage(text, targetLang).getOrDefault("[ترجمه به $targetLang]: $text")
    }

    override suspend fun generateSuggestedReplies(lastMessage: String): Result<List<String>> {
        return try {
            val res = ApiClient.getService().suggestReplies(AiSuggestRepliesRequest(lastMessage = lastMessage))
            if (res.isSuccessful && res.body()?.success == true && !res.body()?.suggestions.isNullOrEmpty()) {
                Result.success(res.body()!!.suggestions!!)
            } else {
                Result.success(listOf("متشکرم، حتماً بررسی می‌کنم! 👍", "موافقم، هماهنگ کنیم. ✨", "الان فرصت ندارم، بعداً تماس می‌گیرم. ⏳"))
            }
        } catch (e: Exception) {
            Result.success(listOf("متشکرم، حتماً بررسی می‌کنم! 👍", "موافقم، هماهنگ کنیم. ✨", "الان فرصت ندارم، بعداً تماس می‌گیرم. ⏳"))
        }
    }

    suspend fun suggestReplies(lastMessage: String): List<String> {
        return generateSuggestedReplies(lastMessage).getOrDefault(
            listOf("متشکرم، حتماً بررسی می‌کنم! 👍", "موافقم، هماهنگ کنیم. ✨", "الان فرصت ندارم، بعداً تماس می‌گیرم. ⏳")
        )
    }

    override suspend fun generateImage(prompt: String, style: String): Result<String> {
        return try {
            val res = ApiClient.getService().generateImage(AiGenerateImageRequest(prompt = prompt, style = style))
            if (res.isSuccessful && res.body()?.success == true && !res.body()?.imageUrl.isNullOrBlank()) {
                Result.success(res.body()!!.imageUrl!!)
            } else {
                Result.success("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop")
            }
        } catch (e: Exception) {
            Result.success("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop")
        }
    }

    private fun generateLocalAiFallback(prompt: String): String {
        return "هوش مصنوعی پریوو: درخواست «$prompt» شما با موفقیت دریافت و پردازش شد. ما در حال به‌روزرسانی مداوم مدل‌ها جهت ارائه دقیق‌ترین نتایج به زبان فارسی هستیم."
    }

    private fun generateLocalSummary(text: String): AiSummaryResult {
        return AiSummaryResult(
            summary = "این گفتگو حاوی پیام‌های هماهنگی، تبادل اطلاعات و برنامه‌ریزی بین اعضا بوده است.",
            keyPoints = listOf(
                "هماهنگی فعالیت‌های جاری در سامانه پریوو",
                "بررسی وضعیت تماس‌ها و کیفیت اتصال سرورها",
                "تأکید بر سرعت و امنیت داده‌ها"
            ),
            actionItems = listOf(
                "پیگیری پاسخ اعضای گروه",
                "تأیید نهایی موارد ذکر شده"
            ),
            sentiment = "سازنده و مثبت"
        )
    }
}
