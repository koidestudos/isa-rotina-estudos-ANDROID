package br.com.isa.rotinaestudos.data.repository

import br.com.isa.rotinaestudos.data.model.ChatMessage
import br.com.isa.rotinaestudos.data.model.ChatThread
import br.com.isa.rotinaestudos.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore) {

    fun chatIdFor(uid1: String, uid2: String): String =
        listOf(uid1, uid2).sorted().joinToString("_")

    suspend fun loadThread(chatId: String): ChatThread? {
        val doc = db.collection("chats").document(chatId).get().await()
        if (!doc.exists()) return null
        val d = doc.data ?: return null
        @Suppress("UNCHECKED_CAST")
        return ChatThread(
            id = doc.id,
            participants = (d["participants"] as? List<String>) ?: emptyList(),
            participantNames = (d["participantNames"] as? Map<String, String>) ?: emptyMap(),
            messages = parseMessages(d["messages"]),
            chatApproved = d["chatApproved"] as? Boolean ?: false,
            hasPending = d["hasPending"] as? Boolean ?: false,
            lastMessage = d["lastMessage"] as? String ?: ""
        )
    }

    suspend fun ensureChat(myUid: String, myName: String, partnerUid: String, partnerName: String): ChatThread {
        val chatId = chatIdFor(myUid, partnerUid)
        val ref = db.collection("chats").document(chatId)
        val doc = ref.get().await()
        if (!doc.exists()) {
            val data = mapOf(
                "participants" to listOf(myUid, partnerUid),
                "participantNames" to mapOf(myUid to myName, partnerUid to partnerName),
                "messages" to emptyList<Map<String, Any>>(),
                "chatApproved" to false,
                "hasPending" to true,
                "lastMessage" to "",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(data).await()
            return ChatThread(id = chatId, participants = listOf(myUid, partnerUid), participantNames = mapOf(myUid to myName, partnerUid to partnerName))
        }
        return loadThread(chatId) ?: ChatThread(id = chatId)
    }

    suspend fun sendMessage(
        chatId: String,
        myUid: String,
        myName: String,
        partnerUid: String,
        partnerName: String,
        text: String
    ) {
        val ref = db.collection("chats").document(chatId)
        val doc = ref.get().await()
        val msg = mapOf(
            "text" to text,
            "senderUid" to myUid,
            "senderName" to myName,
            "createdAt" to java.time.Instant.now().toString()
        )
        val existing = if (doc.exists()) doc.data else null
        val msgs = ((existing?.get("messages") as? List<*>)?.mapNotNull { it as? Map<String, Any> }?.toMutableList())
            ?: mutableListOf()
        msgs.add(msg)
        if (msgs.size > 200) msgs.subList(0, msgs.size - 200).clear()
        val approved = existing?.get("chatApproved") as? Boolean ?: false
        val names = (existing?.get("participantNames") as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()
        names[myUid] = myName
        if (!names.containsKey(partnerUid)) names[partnerUid] = partnerName
        ref.set(
            mapOf(
                "participants" to listOf(myUid, partnerUid),
                "participantNames" to names,
                "messages" to msgs,
                "lastMessage" to text,
                "chatApproved" to approved,
                "hasPending" to !approved,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun searchUsersByName(query: String, excludeUid: String): List<UserProfile> {
        if (query.isBlank()) return emptyList()
        val snap = db.collection("users").limit(120).get().await()
        val q = query.trim().lowercase()
        return snap.documents.map { FirestoreMapper.fromDocument(it) }
            .filter { !it.banned && it.uid != excludeUid && it.name.lowercase().contains(q) }
            .take(8)
    }

    suspend fun loadSoepUsers(emails: List<String>): List<UserProfile> {
        val users = mutableListOf<UserProfile>()
        for (email in emails) {
            val snap = db.collection("users").whereEqualTo("email", email.trim().lowercase()).limit(1).get().await()
            snap.documents.firstOrNull()?.let { users.add(FirestoreMapper.fromDocument(it)) }
        }
        return users
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMessages(raw: Any?): List<ChatMessage> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map {
            ChatMessage(
                text = it["text"] as? String ?: "",
                senderUid = it["senderUid"] as? String ?: "",
                senderName = it["senderName"] as? String ?: "",
                createdAt = it["createdAt"] as? String ?: "",
                type = it["type"] as? String ?: ""
            )
        }
    }
}
