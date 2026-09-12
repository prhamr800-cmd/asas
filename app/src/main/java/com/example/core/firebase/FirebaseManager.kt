package com.example.core.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.domain.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseManager"
        const val DB_ID = "ai-studio-secureflowmessen-8dc52f32-3725-41c4-a5e1-bf509c29a117"
        const val WEB_CLIENT_ID = "448288858100-nqmr9atvjfk1lrfcjj93m74d2r7smd8m.apps.googleusercontent.com"
    }

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FirebaseApp: ${e.message}", e)
        }
    }

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), DB_ID)
        } catch (e: Exception) {
            Log.w(TAG, "Could not get custom database ID '$DB_ID', falling back to default: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    private val credentialManager by lazy {
        CredentialManager.create(context)
    }

    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    // Google Sign-In with Credential Manager
    suspend fun signInWithGoogle(): Result<User> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val fbUser = authResult.user ?: throw Exception("کاربر گوگل دریافت نشد.")

                val domainUser = User(
                    id = fbUser.uid,
                    username = fbUser.email?.substringBefore("@") ?: fbUser.uid.take(8),
                    nickname = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "کاربر گوگل",
                    bio = "کاربر احراز هویت شده گوگل در پریوو",
                    role = "user",
                    subscriptionTier = "free",
                    avatarColor = "bg-blue-600",
                    avatarEmoji = "🌟",
                    isOnline = true
                )

                // Sync user profile to Firestore
                saveUserProfileToFirestore(domainUser)
                Result.success(domainUser)
            } else {
                Result.failure(Exception("نوع اطلاعات اعتبارسنجی نامعتبر است."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Email / Password Authentication via Firebase
    suspend fun signInWithEmailPassword(email: String, pass: String): Result<User> {
        return try {
            val formattedEmail = if (!email.contains("@")) "$email@privo.app" else email
            val authResult = auth.signInWithEmailAndPassword(formattedEmail, pass).await()
            val fbUser = authResult.user ?: throw Exception("کاربر دریافت نشد.")

            // Fetch profile from Firestore
            val user = fetchUserProfile(fbUser.uid) ?: User(
                id = fbUser.uid,
                username = formattedEmail.substringBefore("@"),
                nickname = formattedEmail.substringBefore("@"),
                bio = "کاربر پریوو",
                role = "user",
                subscriptionTier = "free",
                avatarColor = "bg-indigo-600",
                avatarEmoji = "👤",
                isOnline = true
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmailPassword(email: String, pass: String, nickname: String, bio: String): Result<User> {
        return try {
            val formattedEmail = if (!email.contains("@")) "$email@privo.app" else email
            val authResult = auth.createUserWithEmailAndPassword(formattedEmail, pass).await()
            val fbUser = authResult.user ?: throw Exception("کاربر ایجاد نشد.")

            val user = User(
                id = fbUser.uid,
                username = formattedEmail.substringBefore("@"),
                nickname = nickname.ifBlank { formattedEmail.substringBefore("@") },
                bio = bio,
                role = "user",
                subscriptionTier = "free",
                avatarColor = "bg-indigo-600",
                avatarEmoji = "👤",
                isOnline = true
            )
            saveUserProfileToFirestore(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<User> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val fbUser = authResult.user ?: throw Exception("کاربر مهمان دریافت نشد.")
            val user = User(
                id = fbUser.uid,
                username = "guest_" + fbUser.uid.take(5),
                nickname = "کاربر مهمان",
                bio = "کاربر مهمان فایربیس",
                role = "user",
                subscriptionTier = "free",
                avatarColor = "bg-purple-600",
                avatarEmoji = "👤",
                isOnline = true
            )
            saveUserProfileToFirestore(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore Users Collection
    suspend fun saveUserProfileToFirestore(user: User) {
        try {
            val userMap = hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "nickname" to user.nickname,
                "bio" to user.bio,
                "role" to user.role,
                "subscriptionTier" to user.subscriptionTier,
                "avatarColor" to user.avatarColor,
                "avatarEmoji" to user.avatarEmoji,
                "isOnline" to user.isOnline,
                "lastSeen" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            )
            firestore.collection("users").document(user.id)
                .set(userMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore: ${e.message}", e)
        }
    }

    suspend fun fetchUserProfile(userId: String): User? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                User(
                    id = doc.getString("id") ?: userId,
                    username = doc.getString("username") ?: "user",
                    nickname = doc.getString("nickname") ?: "کاربر",
                    bio = doc.getString("bio") ?: "",
                    role = doc.getString("role") ?: "user",
                    subscriptionTier = doc.getString("subscriptionTier") ?: "free",
                    avatarColor = doc.getString("avatarColor") ?: "bg-indigo-600",
                    avatarEmoji = doc.getString("avatarEmoji") ?: "👤",
                    isOnline = doc.getBoolean("isOnline") ?: true
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user from Firestore: ${e.message}", e)
            null
        }
    }

    // Firestore Real-Time Chats Flow
    fun listenToChats(): Flow<List<Conversation>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("chats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Chats listen error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chats = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: "گفتگو"
                            val type = doc.getString("type") ?: "group"
                            val creatorId = doc.getString("creatorId") ?: ""
                            @Suppress("UNCHECKED_CAST")
                            val members = doc.get("members") as? List<String> ?: emptyList()
                            val lastMessageText = doc.getString("lastMessageText") ?: ""
                            val lastMessageTime = doc.getString("lastMessageTime") ?: ""
                            val avatarEmoji = doc.getString("avatarEmoji") ?: "💬"
                            val avatarColor = doc.getString("avatarColor") ?: "bg-indigo-600"
                            val description = doc.getString("description") ?: ""

                            Conversation(
                                id = id,
                                name = name,
                                type = type,
                                creatorId = creatorId,
                                members = members,
                                lastMessageText = lastMessageText,
                                lastMessageTime = lastMessageTime,
                                unreadCount = 0,
                                isPinned = false,
                                avatarEmoji = avatarEmoji,
                                avatarColor = avatarColor,
                                description = description
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(chats)
                }
            }
        awaitClose { listener.remove() }
    }

    // Firestore Real-Time Messages Flow
    fun listenToMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestampEpoch", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Messages listen error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val senderId = doc.getString("senderId") ?: ""
                            val senderNickname = doc.getString("senderNickname") ?: ""
                            val content = doc.getString("content") ?: ""
                            val timestamp = doc.getString("timestamp") ?: ""
                            val status = doc.getString("status") ?: "delivered"
                            val type = doc.getString("type") ?: "text"
                            val fileUrl = doc.getString("fileUrl")
                            val fileName = doc.getString("fileName")
                            val fileSize = doc.getLong("fileSize") ?: 0L

                            Message(
                                id = id,
                                chatId = chatId,
                                senderId = senderId,
                                senderNickname = senderNickname,
                                content = content,
                                timestamp = timestamp,
                                status = status,
                                type = type,
                                fileUrl = fileUrl,
                                fileName = fileName,
                                fileSize = fileSize
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(msgs)
                }
            }
        awaitClose { listener.remove() }
    }

    // Send Message to Firestore
    suspend fun sendMessageToFirestore(message: Message) {
        try {
            val msgMap = hashMapOf(
                "id" to message.id,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "senderNickname" to message.senderNickname,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "timestampEpoch" to System.currentTimeMillis(),
                "status" to "delivered",
                "type" to message.type,
                "fileUrl" to message.fileUrl,
                "fileName" to message.fileName,
                "fileSize" to message.fileSize
            )

            // Add message to subcollection
            firestore.collection("chats")
                .document(message.chatId)
                .collection("messages")
                .document(message.id)
                .set(msgMap)
                .await()

            // Update lastMessage on parent chat doc
            val chatUpdate = hashMapOf(
                "lastMessageText" to message.content,
                "lastMessageTime" to message.timestamp
            )
            firestore.collection("chats")
                .document(message.chatId)
                .set(chatUpdate, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error writing message to Firestore: ${e.message}", e)
        }
    }

    // Create Chat in Firestore
    suspend fun createChatInFirestore(chat: Conversation) {
        try {
            val chatMap = hashMapOf(
                "id" to chat.id,
                "name" to chat.name,
                "type" to chat.type,
                "creatorId" to chat.creatorId,
                "members" to chat.members,
                "description" to chat.description,
                "avatarEmoji" to chat.avatarEmoji,
                "avatarColor" to chat.avatarColor,
                "lastMessageText" to chat.lastMessageText,
                "lastMessageTime" to chat.lastMessageTime,
                "createdAtEpoch" to System.currentTimeMillis()
            )
            firestore.collection("chats")
                .document(chat.id)
                .set(chatMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat in Firestore: ${e.message}", e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
