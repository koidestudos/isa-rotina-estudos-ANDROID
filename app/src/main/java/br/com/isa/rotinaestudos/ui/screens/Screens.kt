package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.AppConstants
import br.com.isa.rotinaestudos.data.model.ScheduleBlock
import br.com.isa.rotinaestudos.data.model.SubjectDifficulty
import br.com.isa.rotinaestudos.data.model.TimeSlot
import br.com.isa.rotinaestudos.domain.QuizData
import br.com.isa.rotinaestudos.domain.SchoolCalendar2026
import br.com.isa.rotinaestudos.ui.IsaUiState
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.theme.IsaGreen
import br.com.isa.rotinaestudos.ui.theme.IsaGreenDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AuthScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IsaGreenDark),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌿 ISA", fontSize = 32.sp, fontWeight = FontWeight.Black, color = IsaGreen)
                Text("Rotina de Estudos", fontWeight = FontWeight.Bold, color = IsaGreenDark)
                Spacer(Modifier.height(16.dp))
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Entrar") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Registrar") })
                }
                Spacer(Modifier.height(16.dp))
                if (tab == 1) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Senha") }, modifier = Modifier.fillMaxWidth())
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Color.Red, fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (tab == 0) onLogin(email, pass) else onRegister(name, email, pass)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IsaGreen),
                    enabled = !loading
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Text(if (tab == 0) "Entrar" else "Criar conta")
                }
            }
        }
    }
}

@Composable
fun QuizScreen(state: IsaUiState, vm: IsaViewModel) {
    if (state.generating) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = IsaGreen)
                Spacer(Modifier.height(12.dp))
                Text("Gerando sua rotina personalizada...")
            }
        }
        return
    }
    val q = QuizData.QUESTIONS[state.quizIndex]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("${state.quizIndex + 1}/${QuizData.TOTAL_Q}", color = IsaGreen, fontWeight = FontWeight.Bold)
        Text(q.cat, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(8.dp))
        Text(q.q, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        when (q.special) {
            "horarios" -> {
                state.quizHorarios.forEachIndexed { i, slot ->
                    HorarioEditor(slot) { vm.updateHorario(i, it) }
                    Spacer(Modifier.height(8.dp))
                }
                TextButton(onClick = { vm.addHorario() }) { Text("+ Horário") }
            }
            "materias" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuizData.TEMPLATES.keys.forEach { key ->
                        TextButton(onClick = { vm.applyTemplate(key) }) { Text(key.take(8), fontSize = 11.sp) }
                    }
                }
                state.quizMaterias.forEachIndexed { i, mat ->
                    MateriaEditor(mat) { vm.updateMateria(i, it) }
                    Spacer(Modifier.height(8.dp))
                }
                TextButton(onClick = { vm.addMateria() }) { Text("+ Matéria") }
            }
            else -> {
                q.opts?.forEachIndexed { i, opt ->
                    val selected = state.selectedQuizOption == i
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { vm.selectQuizOption(i) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) IsaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, IsaGreen) else null
                    ) {
                        Text("${('A' + i)}) $opt", Modifier.padding(14.dp), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.nextQuizQuestion() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = IsaGreen),
            enabled = q.special != null || state.selectedQuizOption != null
        ) { Text(if (state.quizIndex == QuizData.TOTAL_Q - 1) "Gerar rotina" else "Próxima") }
    }
}

@Composable
private fun HorarioEditor(slot: TimeSlot, onChange: (TimeSlot) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(value = slot.dias, onValueChange = { onChange(slot.copy(dias = it.uppercase())) }, label = { Text("Dia (SEG, TER...)") }, modifier = Modifier.fillMaxWidth())
            Row {
                OutlinedTextField(value = slot.ini, onValueChange = { onChange(slot.copy(ini = it)) }, label = { Text("Início") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = slot.fim, onValueChange = { onChange(slot.copy(fim = it)) }, label = { Text("Fim") }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MateriaEditor(mat: SubjectDifficulty, onChange: (SubjectDifficulty) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(value = mat.nome, onValueChange = { onChange(mat.copy(nome = it)) }, label = { Text("Matéria") }, modifier = Modifier.fillMaxWidth())
            Text("Dificuldade: ${mat.pct}%")
            androidx.compose.material3.Slider(
                value = mat.pct.toFloat(),
                onValueChange = { onChange(mat.copy(pct = it.toInt())) },
                valueRange = 0f..100f
            )
        }
    }
}

@Composable
fun RoutineScreen(state: IsaUiState, onCompleteDay: () -> Unit) {
    val schedule = state.profile?.savedSchedule ?: emptyMap()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(state.profile?.routineName ?: "Sua Rotina", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        if (state.routineSubtitle.isNotBlank()) Text(state.routineSubtitle, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🪙 ${state.profile?.coins ?: 0}")
            Text("🔥 ${state.profile?.streak ?: 0} dias")
        }
        val today = IsaViewModel.todayKey()
        val done = state.profile?.lastComplete == today
        Button(onClick = onCompleteDay, enabled = !done, colors = ButtonDefaults.buttonColors(containerColor = IsaGreen), modifier = Modifier.fillMaxWidth()) {
            Text(if (done) "✅ Estudos concluídos hoje!" else "✅ Estudo de hoje completo!")
        }
        Spacer(Modifier.height(16.dp))
        Text("📆 Cronograma Semanal", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        AppConstants.DAYS.filter { schedule[it]?.isNotEmpty() == true }.forEach { day ->
            Text(day, fontWeight = FontWeight.Bold, color = IsaGreen, modifier = Modifier.padding(top = 8.dp))
            schedule[day]?.forEach { block -> ScheduleBlockCard(block) }
        }
    }
}

@Composable
fun ScheduleBlockCard(block: ScheduleBlock) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = parseHexColor(block.cor).copy(alpha = 0.15f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("${block.ini} – ${block.fim}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(block.mat, fontWeight = FontWeight.Black)
            Text(block.tipo, fontSize = 12.sp)
        }
    }
}

@Composable
fun TextListScreen(title: String, items: List<String>, empty: String = "Nada aqui ainda.") {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) Text(empty, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        else items.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(stripHtml(item), Modifier.padding(14.dp), lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun CalendarScreen(state: IsaUiState, vm: IsaViewModel) {
    var calTab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { vm.calNav(-1) }) { Text("‹") }
            Text(
                if (state.calViewMonths) state.calMonth.year.toString()
                else "${AppConstants.CAL_MES_CURTO[state.calMonth.monthValue - 1]} ${state.calMonth.year}",
                fontWeight = FontWeight.Black,
                color = IsaGreen,
                modifier = Modifier.clickable { vm.toggleCalMonthView() }
            )
            TextButton(onClick = { vm.calNav(1) }) { Text("›") }
        }
        if (!state.calViewMonths) {
            CalendarGridFixed(state, vm)
            Spacer(Modifier.height(12.dp))
            state.calSelectedDay?.let { day ->
                Text(formatDayLabel(day), fontWeight = FontWeight.Bold, color = IsaGreenDark)
                TabRow(selectedTabIndex = calTab) {
                    Tab(selected = calTab == 0, onClick = { calTab = 0 }, text = { Text("Eventos", fontSize = 12.sp) })
                    Tab(selected = calTab == 1, onClick = { calTab = 1 }, text = { Text("Anotações", fontSize = 12.sp) })
                }
                when (calTab) {
                    0 -> {
                        val events = vm.eventsForDay(day)
                        if (events.isEmpty()) Text("Nenhum evento neste dia.", Modifier.padding(8.dp))
                        else events.forEach { ev ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(ev.title, fontWeight = FontWeight.Bold)
                                    Text(AppConstants.CAL_EVENT_LABELS[ev.type] ?: "Evento", fontSize = 12.sp)
                                    if (ev.message.isNotBlank() && ev.message != ev.title) Text(ev.message, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        OutlinedTextField(
                            value = state.calNoteDraft,
                            onValueChange = { vm.updateCalNoteDraft(it) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            label = { Text("Anotações do dia") }
                        )
                        Button(onClick = { vm.saveCalNote(state.calNoteDraft) }, colors = ButtonDefaults.buttonColors(containerColor = IsaGreen), modifier = Modifier.fillMaxWidth()) {
                            Text("Salvar anotação")
                        }
                    }
                }
            } ?: Text("Selecione um dia no calendário.", Modifier.padding(8.dp))
        } else {
            MonthPicker(state, vm)
        }
    }
}

@Composable
private fun CalendarGridFixed(state: IsaUiState, vm: IsaViewModel) {
    val month = state.calMonth
    val first = month.withDayOfMonth(1)
    val pad = first.dayOfWeek.value % 7
    val dim = month.lengthOfMonth()
    val today = LocalDate.now()
    Row(Modifier.fillMaxWidth()) {
        listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach {
            Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
    val cells = pad + dim
    val rows = (cells + 6) / 7
    for (r in 0 until rows) {
        Row(Modifier.fillMaxWidth()) {
            for (c in 0 until 7) {
                val idx = r * 7 + c
                val dayNum = idx - pad + 1
                if (dayNum in 1..dim) {
                    val key = "${month.year}-${month.monthValue.toString().padStart(2, '0')}-${dayNum.toString().padStart(2, '0')}"
                    val isToday = month.year == today.year && month.month == today.month && dayNum == today.dayOfMonth
                    val selected = state.calSelectedDay == key
                    val evType = vm.eventTypeForDay(key)
                    Box(
                        modifier = Modifier.weight(1f).padding(2.dp).height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    selected -> IsaGreen
                                    isToday -> IsaGreen.copy(0.7f)
                                    evType != null -> eventColor(evType).copy(0.25f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .clickable { vm.selectCalDay(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            dayNum.toString(),
                            color = if (selected || isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Spacer(Modifier.weight(1f).height(44.dp))
                }
            }
        }
    }
}

@Composable
private fun MonthPicker(state: IsaUiState, vm: IsaViewModel) {
    for (i in 0 until 12) {
        val selected = state.calMonth.monthValue - 1 == i
        Card(
            Modifier.fillMaxWidth().padding(4.dp).clickable {
                vm.selectCalMonth(i)
            },
            colors = CardDefaults.cardColors(containerColor = if (selected) IsaGreen else MaterialTheme.colorScheme.surface)
        ) {
            Text(AppConstants.CAL_MES_CURTO[i], Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}

private fun eventColor(type: String): Color = when (type) {
    "ferias" -> Color(0xFF3949AB)
    "feriado" -> Color(0xFFEF5350)
    "recuperacao" -> Color(0xFFFFB74D)
    "av_recuperacao" -> Color(0xFFCE93D8)
    "avaliacao" -> Color(0xFF29B6F6)
    else -> IsaGreen
}

private fun formatDayLabel(key: String): String {
    val p = key.split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else key
}

private fun stripHtml(html: String): String = html
    .replace(Regex("<[^>]+>"), "")
    .replace("&nbsp;", " ")

@Composable
fun AvisosScreen(state: IsaUiState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("📢 Avisos Gerais", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        Spacer(Modifier.height(12.dp))
        if (state.announcements.isEmpty()) Text("Nenhum comunicado.", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        state.announcements.forEach { a ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("${a.adminName} · ${formatTs(a.createdAt)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text(a.message)
                }
            }
        }
    }
}

@Composable
fun RankingScreen(state: IsaUiState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🏆 Ranking", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        Spacer(Modifier.height(12.dp))
        state.ranking.forEachIndexed { i, u ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${i + 1}", fontWeight = FontWeight.Black, color = IsaGreen, modifier = Modifier.width(36.dp))
                    Column(Modifier.weight(1f)) {
                        Text(u.name, fontWeight = FontWeight.Bold)
                        Text("🔥 ${u.streak} · 🪙 ${u.coins}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(state: IsaUiState, vm: IsaViewModel) {
    var bio by remember(state.profile?.bio) { mutableStateOf(state.profile?.bio ?: "") }
    var series by remember(state.profile?.userSeries) { mutableStateOf(state.profile?.userSeries ?: "") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("👤 Meu Perfil", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        Spacer(Modifier.height(12.dp))
        Text(state.profile?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(state.profile?.email ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        Spacer(Modifier.height(12.dp))
        Text("🪙 ${state.profile?.coins ?: 0} moedas · 🔥 ${state.profile?.streak ?: 0} dias")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text("Série escolar", fontWeight = FontWeight.Bold)
        AppConstants.SERIES_OPTIONS.forEach { opt ->
            Row(Modifier.fillMaxWidth().clickable { series = opt }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (series == opt) "●" else "○", color = IsaGreen)
                Spacer(Modifier.width(8.dp))
                Text(opt)
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.updateBio(bio); vm.updateSeries(series) }, colors = ButtonDefaults.buttonColors(containerColor = IsaGreen), modifier = Modifier.fillMaxWidth()) {
            Text("Salvar perfil")
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { vm.logout() }) { Text("Sair da conta", color = Color.Red) }
    }
}

@Composable
fun FlashcardsScreen(state: IsaUiState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🃏 Flashcards", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        Spacer(Modifier.height(12.dp))
        val sets = state.profile?.flashcardSets ?: emptyList()
        if (sets.isEmpty()) Text("Nenhum conjunto ainda. Crie flashcards na versão web ou em breve aqui.", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        sets.forEach { set ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(set.subject, fontWeight = FontWeight.Bold)
                    Text("${set.cards.size} cartões", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MaisScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("⋯ Mais", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth().clickable { /* ClassApp */ }) {
            Text("📱 Acessar ClassApp", Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = IsaGreen)
        }
        Spacer(Modifier.height(8.dp))
        Text("App nativo ISA Rotina de Estudos para Android.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

@Composable
fun AdminScreen(state: IsaUiState, vm: IsaViewModel) {
    var date by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🛡️ Painel Admin", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IsaGreenDark)
        if (state.adminMessage != null) Text(state.adminMessage!!, color = IsaGreen, modifier = Modifier.padding(8.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Data (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (ferias, feriado...)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.saveAdminEvent(date, title, message, type, null, false) }, colors = ButtonDefaults.buttonColors(containerColor = IsaGreen), modifier = Modifier.fillMaxWidth()) {
            Text("Adicionar evento admin")
        }
        Spacer(Modifier.height(16.dp))
        Text("Calendário escolar 2026", fontWeight = FontWeight.Bold)
        SchoolCalendar2026.build().entries.sortedBy { it.key }.take(50).forEach { (d, ev) ->
            Card(Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                date = d; title = ev.title; type = ev.type; message = ""
            }) {
                Row(Modifier.padding(10.dp)) {
                    Text(d, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                    Text(ev.title, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.saveAdminEvent(d, title.ifBlank { ev.title }, message, type.ifBlank { ev.type }, null, true) }) {
                        Text("Editar", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    IsaGreen
}

private fun formatTs(ms: Long): String =
    if (ms == 0L) "" else java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR")).format(java.util.Date(ms))

