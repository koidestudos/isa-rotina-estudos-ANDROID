package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.data.model.ChatMessage
import br.com.isa.rotinaestudos.data.model.UserProfile
import br.com.isa.rotinaestudos.ui.IsaUiState
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.components.IsaAvatar
import br.com.isa.rotinaestudos.ui.components.IsaEmptyState
import br.com.isa.rotinaestudos.ui.components.IsaPrimaryButton
import br.com.isa.rotinaestudos.ui.components.IsaSectionCard
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2
import br.com.isa.rotinaestudos.ui.theme.IsaPurple

@Composable
fun ChatScreen(state: IsaUiState, vm: IsaViewModel) {
    if (state.isGuest) {
        Column(Modifier.padding(16.dp)) {
            IsaEmptyState("💬", "Chat indisponível", "Entre com sua conta para conversar com amigos e a SOEP.")
        }
        return
    }

    val chatId = state.activeChatId
    if (chatId != null) {
        ChatConversation(state, vm)
    } else {
        ChatHub(state, vm)
    }
}

@Composable
private fun ChatHub(state: IsaUiState, vm: IsaViewModel) {
    var search by remember { mutableStateOf("") }
    val friends = state.profile?.friends ?: emptyList()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IsaSectionCard("🏫 SOEP") {
            if (state.soepUsers.isEmpty()) {
                Text("Carregando contas SOEP…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.soepUsers.forEach { user ->
                    ChatUserRow(user) { vm.openChatWith(user.uid, user.name) }
                }
            }
        }

        IsaSectionCard("👥 Amigos") {
            if (friends.isEmpty()) {
                Text("Adicione amigos pela busca abaixo.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                friends.forEach { f ->
                    ChatUserRow(
                        UserProfile(uid = f.uid, name = f.name),
                        subtitle = "Amigo"
                    ) { vm.openChatWith(f.uid, f.name) }
                }
            }
        }

        IsaSectionCard("🔍 Buscar aluno") {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    vm.searchChatUsers(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do aluno") },
                shape = RoundedCornerShape(14.dp)
            )
            state.userSearchResults.forEach { user ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IsaAvatar(user.name, user.photoURL, size = 40.dp)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Column(Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (user.userSeries.isNotBlank()) {
                            Text(user.userSeries, fontSize = 11.sp, color = IsaG2)
                        }
                    }
                    TextButton(onClick = { vm.addFriend(user.uid, user.name) }) {
                        Text("+ Amigo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { vm.openChatWith(user.uid, user.name) }) {
                        Text("💬", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatUserRow(
    user: UserProfile,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IsaG2.copy(0.08f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IsaAvatar(user.name, user.photoURL, size = 44.dp)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Column(Modifier.padding(start = 8.dp)) {
                Text(user.name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = IsaG1)
                Text(subtitle ?: "Conversar", fontSize = 11.sp, color = IsaG2)
            }
        }
    }
}

@Composable
private fun ChatConversation(state: IsaUiState, vm: IsaViewModel) {
    var draft by remember { mutableStateOf("") }
    val me = state.authUser?.uid
    val listState = rememberLazyListState()

    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.lastIndex)
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = vm::closeChatConversation) { Text("← Voltar") }
            Text(state.chatPartnerName, fontWeight = FontWeight.Black, fontSize = 16.sp, color = IsaG1)
        }

        if (!state.chatApproved) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IsaPurple.copy(0.12f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Text(
                    "⏳ Chat aguardando aprovação da moderação. Você ainda pode enviar mensagens.",
                    Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.chatMessages.isEmpty()) {
                item {
                    IsaEmptyState("💬", "Sem mensagens", "Envie a primeira mensagem!")
                }
            } else {
                items(state.chatMessages) { msg ->
                    ChatBubble(msg, isMine = msg.senderUid == me)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mensagem…") },
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            IsaPrimaryButton(
                text = "➤",
                onClick = {
                    vm.sendChatMessage(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank()
            )
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage, isMine: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = if (isMine) IsaG2 else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isMine) {
                    Text(msg.senderName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IsaG2)
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    msg.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
