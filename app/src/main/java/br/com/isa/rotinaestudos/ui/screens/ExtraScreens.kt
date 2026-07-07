package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.data.model.UserProfile
import br.com.isa.rotinaestudos.ui.IsaUiState
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.components.IsaEmptyState
import br.com.isa.rotinaestudos.ui.components.IsaPrimaryButton
import br.com.isa.rotinaestudos.ui.components.IsaSectionCard
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2
import br.com.isa.rotinaestudos.ui.theme.IsaRed
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

data class IsaPopupData(val icon: String, val title: String, val message: String)

@Composable
fun IsaCelebrationPopup(data: IsaPopupData?, onDismiss: () -> Unit) {
    if (data == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(data.icon, fontSize = 36.sp) },
        title = { Text(data.title, fontWeight = FontWeight.Black) },
        text = { Text(data.message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) } }
    )
}

@Composable
fun WarningsPopup(warnings: List<Map<String, String>>, onDismiss: () -> Unit) {
    if (warnings.isEmpty()) return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", fontSize = 32.sp) },
        title = { Text("Avisos da moderação", fontWeight = FontWeight.Black) },
        text = {
            Column {
                warnings.take(3).forEach { w ->
                    Text("• ${w["message"] ?: ""}", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendi") } }
    )
}

@Composable
fun FullAdminScreen(state: IsaUiState, vm: IsaViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    Column {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Usuários", fontSize = 10.sp) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Moderação", fontSize = 10.sp) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Comunicados", fontSize = 10.sp) })
            Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Eventos", fontSize = 10.sp) })
            Tab(selected = tab == 4, onClick = { tab = 4 }, text = { Text("SOEP", fontSize = 10.sp) })
            Tab(selected = tab == 5, onClick = { tab = 5; vm.refreshAdminChats() }, text = { Text("Chats", fontSize = 10.sp) })
        }
        when (tab) {
            0 -> AdminUsersTab(vm)
            1 -> AdminModerationTab(state, vm)
            2 -> AdminComunicadosTab(state, vm)
            3 -> AdminScreen(state, vm)
            4 -> AdminSoepTab(state, vm)
            5 -> AdminChatsTab(state, vm)
        }
    }
}

@Composable
private fun AdminUsersTab(vm: IsaViewModel) {
    var email by remember { mutableStateOf("") }
    var coins by remember { mutableStateOf("") }
    var streak by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail do usuário") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = coins, onValueChange = { coins = it }, label = { Text("Moedas") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = streak, onValueChange = { streak = it }, label = { Text("Sequência") }, modifier = Modifier.fillMaxWidth())
        IsaPrimaryButton(
            text = "Salvar usuário",
            onClick = { vm.adminSaveUser(email, coins.toIntOrNull(), streak.toIntOrNull()) }
        )
    }
}

@Composable
private fun AdminModerationTab(state: IsaUiState, vm: IsaViewModel) {
    var email by remember { mutableStateOf("") }
    var banReason by remember { mutableStateOf("") }
    var warnMsg by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = { vm.adminFetchStatus(email) { status = it } }, label = { Text("Consultar") })
            FilterChip(selected = false, onClick = { vm.adminBanUser(email, banReason) }, label = { Text("Banir") })
            FilterChip(selected = false, onClick = { vm.adminUnbanUser(email) }, label = { Text("Desbanir") })
        }
        if (status.isNotBlank()) Text(status, fontSize = 12.sp, color = IsaG2, modifier = Modifier.padding(vertical = 8.dp))
        OutlinedTextField(value = banReason, onValueChange = { banReason = it }, label = { Text("Motivo do ban") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = warnMsg, onValueChange = { warnMsg = it }, label = { Text("Mensagem de aviso") }, modifier = Modifier.fillMaxWidth())
        IsaPrimaryButton(text = "Enviar aviso", onClick = { vm.adminSendWarning(email, warnMsg) })
    }
}

@Composable
private fun AdminComunicadosTab(state: IsaUiState, vm: IsaViewModel) {
    var msg by remember { mutableStateOf("") }
    Column(Modifier.padding(8.dp)) {
        OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Comunicado") }, modifier = Modifier.fillMaxWidth())
        IsaPrimaryButton(text = "Publicar comunicado", onClick = { vm.adminSendAnnouncement(msg); msg = "" })
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(200.dp)) {
            items(state.announcements) { a ->
                Card { Text(a.message, Modifier.padding(10.dp), fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun AdminSoepTab(state: IsaUiState, vm: IsaViewModel) {
    var email by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
        Text("E-mails das contas SOEP", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IsaG1)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Novo e-mail SOEP") }, modifier = Modifier.fillMaxWidth())
        IsaPrimaryButton(text = "Adicionar e-mail", onClick = { vm.adminAddSoepEmail(email); email = "" })
        Spacer(Modifier.height(12.dp))
        state.soepEmails.forEachIndexed { i, e ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(e, modifier = Modifier.weight(1f), fontSize = 12.sp)
                TextButton(onClick = { vm.adminRemoveSoepEmail(i) }) { Text("🗑") }
            }
        }
    }
}

@Composable
private fun AdminChatsTab(state: IsaUiState, vm: IsaViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
        if (state.adminChats.isEmpty()) {
            item { IsaEmptyState("💬", "Sem chats", "Nenhuma conversa registrada.") }
        } else {
            items(state.adminChats) { chat ->
                Card(colors = CardDefaults.cardColors(containerColor = if (chat.hasPending) IsaRed.copy(0.08f) else MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(chat.participantNames.values.joinToString(" ↔ "), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(chat.lastMessage.ifBlank { "Sem mensagens" }, fontSize = 12.sp, maxLines = 2)
                        Text(
                            if (chat.chatApproved) "✅ Aprovado" else if (chat.hasPending) "⏳ Pendente" else "Chat",
                            fontSize = 10.sp,
                            color = if (chat.chatApproved) IsaG2 else IsaRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(state: IsaUiState) {
    Column(Modifier.padding(8.dp)) {
        IsaSectionCard("💬 Chat") {
            Text(
                "Converse com amigos e com a SOEP pelo site. No app você pode ver avisos de moderação em ⋯ Mais → Ocorrências.",
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            if (!state.isGuest) {
                Spacer(Modifier.height(8.dp))
                Text("Amigos: ${state.profile?.friends?.size ?: 0}", fontSize = 12.sp, color = IsaG2, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChangePasswordSheet(onDone: (String) -> Unit) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.padding(8.dp)) {
        OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Senha atual") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Nova senha") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirmar") }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = IsaRed, fontSize = 12.sp) }
        IsaPrimaryButton(
            text = "Alterar senha",
            onClick = {
                when {
                    current.isBlank() || newPass.isBlank() -> error = "Preencha todos os campos."
                    newPass.length < 6 -> error = "Mínimo 6 caracteres."
                    newPass != confirm -> error = "Confirmação não confere."
                    else -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user?.email == null) { error = "Conta Google — use o site."; return@IsaPrimaryButton }
                        val cred = EmailAuthProvider.getCredential(user.email!!, current)
                        user.reauthenticate(cred).addOnSuccessListener {
                            user.updatePassword(newPass).addOnSuccessListener { onDone("Senha alterada!") }
                                .addOnFailureListener { error = "Erro ao alterar senha." }
                        }.addOnFailureListener { error = "Senha atual incorreta." }
                    }
                }
            }
        )
    }
}

@Composable
fun AvisosFullScreen(state: IsaUiState) {
    var tab by remember { mutableIntStateOf(0) }
    val warnings = state.profile?.warnings ?: emptyList()
    Column {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Comunicados") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Meus avisos (${warnings.size})") })
        }
        if (tab == 0) AvisosScreen(state) else WarningsList(warnings)
    }
}

@Composable
private fun WarningsList(warnings: List<Map<String, String>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (warnings.isEmpty()) {
            item { IsaEmptyState("📋", "Nenhum aviso", "Você não recebeu avisos da moderação.") }
        } else {
            items(warnings) { w ->
                Card(colors = CardDefaults.cardColors(containerColor = IsaRed.copy(0.08f))) {
                    Column(Modifier.padding(14.dp)) {
                        Text(w["adminName"] ?: "Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(w["createdAt"]?.take(10)?.replace("-", "/") ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(w["message"] ?: "", fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}
