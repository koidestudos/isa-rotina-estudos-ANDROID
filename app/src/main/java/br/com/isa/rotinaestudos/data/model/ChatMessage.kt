package br.com.isa.rotinaestudos.data.model

data class ChatMessage(
    val text: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val createdAt: String = "",
    val type: String = ""
)

data class ChatThread(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val messages: List<ChatMessage> = emptyList(),
    val chatApproved: Boolean = false,
    val hasPending: Boolean = false,
    val lastMessage: String = ""
)
