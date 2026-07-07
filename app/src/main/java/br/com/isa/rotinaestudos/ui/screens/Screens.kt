package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.AppConstants
import br.com.isa.rotinaestudos.data.model.ScheduleBlock
import br.com.isa.rotinaestudos.data.model.SubjectDifficulty
import br.com.isa.rotinaestudos.data.model.TimeSlot
import br.com.isa.rotinaestudos.auth.GoogleAuth
import coil.compose.AsyncImage
import br.com.isa.rotinaestudos.domain.QuizData
import br.com.isa.rotinaestudos.domain.SchoolCalendar2026
import br.com.isa.rotinaestudos.ui.IsaUiState
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.components.IsaEmptyState
import br.com.isa.rotinaestudos.ui.components.IsaGoogleButton
import br.com.isa.rotinaestudos.ui.components.IsaGoldButton
import br.com.isa.rotinaestudos.ui.components.IsaHeroBackground
import br.com.isa.rotinaestudos.ui.components.IsaOptionCard
import br.com.isa.rotinaestudos.ui.components.IsaPrimaryButton
import br.com.isa.rotinaestudos.ui.components.IsaQuizProgress
import br.com.isa.rotinaestudos.ui.components.isaRankingMedal
import br.com.isa.rotinaestudos.ui.components.IsaSectionCard
import br.com.isa.rotinaestudos.ui.theme.IsaCardShape
import br.com.isa.rotinaestudos.ui.theme.IsaCoin
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2
import br.com.isa.rotinaestudos.ui.theme.IsaG4
import br.com.isa.rotinaestudos.ui.theme.IsaGreen
import br.com.isa.rotinaestudos.ui.theme.IsaGreenDark
import br.com.isa.rotinaestudos.ui.theme.IsaPurple
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── AUTH ───────────────────────────────────────────────────────────────────

@Composable
fun AuthScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onGoogleError: (String) -> Unit,
    onGuest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var googleBusy by remember { mutableStateOf(false) }

    IsaHeroBackground {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IsaCardShape,
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = RoundedCornerShape(14.dp), color = IsaG2, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("ISA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("ISA Estudos", style = MaterialTheme.typography.headlineMedium, color = IsaG1)
                    Text("Rotina de Estudos Personalizada", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))

                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                        indicator = { tabPositions ->
                            if (tab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[tab]),
                                    color = IsaG2
                                )
                            }
                        }
                    ) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Entrar", fontWeight = FontWeight.Bold) })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Criar Conta", fontWeight = FontWeight.Bold) })
                    }
                    Spacer(Modifier.height(20.dp))

                    if (tab == 1) {
                        IsaTextField(value = name, onValueChange = { name = it }, label = "Nome")
                        Spacer(Modifier.height(10.dp))
                    }
                    IsaTextField(value = email, onValueChange = { email = it }, label = "E-mail")
                    Spacer(Modifier.height(10.dp))
                    IsaTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = "Senha",
                        trailing = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        obscure = !showPass
                    )

                    AnimatedVisibility(visible = error != null) {
                        Text(error ?: "", color = Color(0xFFE74C3C), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                    IsaPrimaryButton(
                        text = if (tab == 0) "Entrar →" else "Criar conta →",
                        onClick = { if (tab == 0) onLogin(email, pass) else onRegister(name, email, pass) },
                        enabled = email.isNotBlank() && pass.length >= 6,
                        loading = loading
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(0.4f)))
                        Text("  ou  ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(0.4f)))
                    }
                    Spacer(Modifier.height(16.dp))
                    IsaGoogleButton(
                        onClick = {
                            scope.launch {
                                googleBusy = true
                                GoogleAuth.signIn(context)
                                    .onSuccess { token -> onGoogleSignIn(token) }
                                    .onFailure { e ->
                                        val msg = GoogleAuth.errorMessage(e)
                                        if (msg.isNotBlank()) onGoogleError(msg)
                                    }
                                googleBusy = false
                            }
                        },
                        enabled = !loading && !googleBusy,
                        loading = loading || googleBusy
                    )
                    Spacer(Modifier.height(14.dp))
                    TextButton(onClick = onGuest) {
                        Text("Continuar sem conta (dados locais)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun IsaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    trailing: @Composable (() -> Unit)? = null,
    obscure: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (obscure) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        trailingIcon = trailing,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IsaG2,
            focusedLabelColor = IsaG2,
            cursorColor = IsaG2
        )
    )
}

// ─── QUIZ ───────────────────────────────────────────────────────────────────

@Composable
fun QuizScreen(state: IsaUiState, vm: IsaViewModel) {
    if (state.generating) {
        IsaHeroBackground {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(20.dp))
                Text("Gerando sua rotina...", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Analisando suas respostas ✨", color = Color.White.copy(0.8f), fontSize = 13.sp)
            }
        }
        return
    }

    val q = QuizData.QUESTIONS[state.quizIndex]
    IsaHeroBackground {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            IsaQuizProgress(state.quizIndex + 1, QuizData.TOTAL_Q)
            Spacer(Modifier.height(20.dp))
            Card(shape = IsaCardShape, elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(q.cat.uppercase(), style = MaterialTheme.typography.labelMedium, color = IsaG2)
                    Spacer(Modifier.height(10.dp))
                    Text(q.q, style = MaterialTheme.typography.headlineMedium, color = IsaG1, lineHeight = 26.sp)
                    Spacer(Modifier.height(18.dp))
                    when (q.special) {
                        "horarios" -> {
                            state.quizHorarios.forEachIndexed { i, slot ->
                                HorarioEditor(slot) { vm.updateHorario(i, it) }
                                Spacer(Modifier.height(8.dp))
                            }
                            TextButton(onClick = { vm.addHorario() }) { Text("+ Adicionar horário", color = IsaG2, fontWeight = FontWeight.Bold) }
                        }
                        "materias" -> {
                            MateriasSection(state, vm)
                        }
                        else -> {
                            q.opts?.forEachIndexed { i, opt ->
                                IsaOptionCard('A' + i, opt, state.selectedQuizOption == i) { vm.selectQuizOption(i) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            IsaPrimaryButton(
                text = if (state.quizIndex == QuizData.TOTAL_Q - 1) "Finalizar ✓" else "Próxima →",
                onClick = { vm.nextQuizQuestion() },
                enabled = q.special != null || state.selectedQuizOption != null
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MateriasSection(state: IsaUiState, vm: IsaViewModel) {
    Text("Templates rápidos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IsaG1)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        QuizData.TEMPLATES.keys.forEach { key ->
            FilterChip(
                selected = false,
                onClick = { vm.applyTemplate(key) },
                label = { Text(key, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IsaG2, selectedLabelColor = Color.White)
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    state.quizMaterias.forEachIndexed { i, mat ->
        MateriaEditor(mat) { vm.updateMateria(i, it) }
        Spacer(Modifier.height(8.dp))
    }
    TextButton(onClick = { vm.addMateria() }) { Text("+ Matéria", color = IsaG2, fontWeight = FontWeight.Bold) }
}

@Composable
private fun HorarioEditor(slot: TimeSlot, onChange: (TimeSlot) -> Unit) {
    val days = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))) {
        Column(Modifier.padding(14.dp)) {
            Text("Dias da semana", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                days.forEach { d ->
                    val selected = slot.dias.contains(d)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val parts = slot.dias.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                            if (selected) parts.remove(d) else parts.add(d)
                            onChange(slot.copy(dias = parts.joinToString(", ")))
                        },
                        label = { Text(d, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IsaTextField(value = slot.ini, onValueChange = { onChange(slot.copy(ini = it)) }, label = "Início")
                IsaTextField(value = slot.fim, onValueChange = { onChange(slot.copy(fim = it)) }, label = "Fim")
            }
        }
    }
}

@Composable
private fun MateriaEditor(mat: SubjectDifficulty, onChange: (SubjectDifficulty) -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))) {
        Column(Modifier.padding(14.dp)) {
            IsaTextField(value = mat.nome, onValueChange = { onChange(mat.copy(nome = it)) }, label = "Matéria")
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dificuldade", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${mat.pct}%", fontWeight = FontWeight.Black, color = IsaG2)
            }
            Slider(
                value = mat.pct.toFloat(),
                onValueChange = { onChange(mat.copy(pct = it.toInt())) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = IsaG2, activeTrackColor = IsaG2)
            )
        }
    }
}

// ─── ROTINA ─────────────────────────────────────────────────────────────────

@Composable
fun RoutineScreen(state: IsaUiState, vm: IsaViewModel) {
    val schedule = state.profile?.savedSchedule ?: emptyMap()
    val today = IsaViewModel.todayKey()
    val done = state.profile?.lastComplete == today
    var selectedBlock by remember { mutableStateOf<Pair<String, Int>?>(null) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item {
            AnimatedVisibility(visible = state.completeDayAnim, enter = scaleIn() + fadeIn()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = IsaCoin.copy(0.15f)),
                    shape = IsaCardShape
                ) {
                    Text("🪙 +${maxOf(2, (state.profile?.streak ?: 0) * 2)} moedas!", Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Black, color = IsaCoin)
                }
            }
        }
        item {
            IsaGoldButton(
                text = if (done) "✅ Estudos concluídos hoje!" else "✅ Estudo de hoje completo!",
                onClick = vm::completeDay,
                enabled = !done
            )
        }
        if (state.subjectBars.isNotEmpty()) {
            item {
                IsaSectionCard("🎯 Matérias prioritárias") {
                    state.subjectBars.forEach { (name, pct) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("$pct%", fontSize = 12.sp, color = IsaG2, fontWeight = FontWeight.Black)
                        }
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(Modifier.fillMaxWidth(pct / 100f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(IsaG2))
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📆 Cronograma", fontWeight = FontWeight.Black, color = IsaG1)
                TextButton(onClick = vm::toggleReorganize) {
                    Text(if (state.reorganizeMode) "✅ Salvar ordem" else "🔀 Reorganizar", fontSize = 12.sp)
                }
            }
        }
        item {
            val days = AppConstants.DAYS.filter { schedule[it]?.isNotEmpty() == true }
            if (days.isEmpty()) {
                IsaEmptyState("📋", "Sem cronograma", "Complete o quiz para gerar sua rotina.")
            } else {
                days.forEach { day ->
                    DayScheduleSection(
                        day = day,
                        blocks = schedule[day] ?: emptyList(),
                        reorganize = state.reorganizeMode,
                        selected = selectedBlock,
                        onBlockClick = { d, idx ->
                            if (!state.reorganizeMode) return@DayScheduleSection
                            val cur = selectedBlock
                            if (cur == null) selectedBlock = d to idx
                            else {
                                vm.swapScheduleBlocks(cur.first, cur.second, d, idx)
                                selectedBlock = null
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun DayScheduleSection(
    day: String,
    blocks: List<ScheduleBlock>,
    reorganize: Boolean = false,
    selected: Pair<String, Int>? = null,
    onBlockClick: (String, Int) -> Unit = { _, _ -> }
) {
    Column {
        Surface(shape = RoundedCornerShape(10.dp), color = IsaG2, modifier = Modifier.fillMaxWidth()) {
            Text(day, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(Modifier.height(6.dp))
        blocks.forEachIndexed { idx, block ->
            val isSelected = selected?.first == day && selected.second == idx
            ScheduleBlockCard(
                block = block,
                highlight = isSelected,
                onClick = if (reorganize && !block.isBreak) ({ onBlockClick(day, idx) }) else null
            )
        }
    }
}

@Composable
fun ScheduleBlockCard(block: ScheduleBlock, highlight: Boolean = false, onClick: (() -> Unit)? = null) {
    val color = parseHexColor(block.cor)
    val badge = when {
        block.isRev == true -> "🔁 Revisão"
        block.isBreak == true -> "😴 Descanso"
        else -> block.tipo
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) IsaG2.copy(0.2f) else color.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(if (highlight) 2.dp else 1.dp, if (highlight) IsaG2 else color.copy(0.35f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(block.mat, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("${block.ini} – ${block.fim}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

// ─── TEXT LISTS (métodos, revisões, descanso) ───────────────────────────────

@Composable
fun TextListScreen(title: String, items: List<String>, empty: String = "Nada aqui ainda.") {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (items.isEmpty()) {
            item { IsaEmptyState("📖", "Vazio", empty) }
        } else {
            items(items) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(IsaG2)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(stripHtml(item), lineHeight = 22.sp, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ─── CALENDÁRIO ─────────────────────────────────────────────────────────────

@Composable
fun CalendarScreen(state: IsaUiState, vm: IsaViewModel) {
    var calTab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.calNav(-1) }) { Text("‹", fontSize = 28.sp, fontWeight = FontWeight.Black, color = IsaG2) }
            Text(
                if (state.calViewMonths) state.calMonth.year.toString()
                else "${AppConstants.CAL_MES_CURTO[state.calMonth.monthValue - 1]} ${state.calMonth.year}",
                fontWeight = FontWeight.Black,
                color = IsaG2,
                fontSize = 16.sp,
                modifier = Modifier.clickable { vm.toggleCalMonthView() }
            )
            IconButton(onClick = { vm.calNav(1) }) { Text("›", fontSize = 28.sp, fontWeight = FontWeight.Black, color = IsaG2) }
        }
        if (!state.calViewMonths) {
            CalendarGrid(state, vm)
            Spacer(Modifier.height(12.dp))
            state.calSelectedDay?.let { day ->
                Text(formatDayLabel(day), fontWeight = FontWeight.Black, color = IsaG1, fontSize = 15.sp)
                TabRow(
                    selectedTabIndex = calTab,
                    indicator = { tabPositions ->
                        if (calTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[calTab]),
                                color = IsaG2
                            )
                        }
                    }
                ) {
                    Tab(selected = calTab == 0, onClick = { calTab = 0 }, text = { Text("Eventos", fontSize = 12.sp) })
                    Tab(selected = calTab == 1, onClick = { calTab = 1 }, text = { Text("Anotações", fontSize = 12.sp) })
                }
                when (calTab) {
                    0 -> CalendarEventsList(vm.eventsForDay(day))
                    1 -> CalendarNotesPanel(state.calNoteDraft, vm::updateCalNoteDraft) { vm.saveCalNote(state.calNoteDraft) }
                }
            } ?: Text("Selecione um dia no calendário.", Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            MonthPicker(state, vm)
        }
    }
}

@Composable
private fun CalendarEventsList(events: List<br.com.isa.rotinaestudos.ui.CalendarEventDataUi>) {
    if (events.isEmpty()) {
        Text("Nenhum evento neste dia.", Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        events.forEach { ev ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(ev.title, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = eventColor(ev.type).copy(0.15f)) {
                        Text(AppConstants.CAL_EVENT_LABELS[ev.type] ?: "Evento", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = eventColor(ev.type))
                    }
                    if (ev.message.isNotBlank() && ev.message != ev.title) {
                        Spacer(Modifier.height(4.dp))
                        Text(ev.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarNotesPanel(draft: String, onChange: (String) -> Unit, onSave: () -> Unit) {
    OutlinedTextField(
        value = draft,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
        label = { Text("Anotações do dia") },
        shape = RoundedCornerShape(14.dp)
    )
    IsaPrimaryButton(text = "Salvar anotação", onClick = onSave)
}

@Composable
private fun CalendarGrid(state: IsaUiState, vm: IsaViewModel) {
    val month = state.calMonth
    val first = month.withDayOfMonth(1)
    val pad = first.dayOfWeek.value % 7
    val dim = month.lengthOfMonth()
    val today = LocalDate.now()
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth()) {
                listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IsaG1)
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
                            val hasNote = state.profile?.calNotes?.containsKey(key) == true
                            Box(
                                modifier = Modifier.weight(1f).padding(2.dp).height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            selected -> IsaG2
                                            isToday -> IsaG2.copy(0.75f)
                                            evType != null -> eventColor(evType).copy(0.22f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { vm.selectCalDay(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        dayNum.toString(),
                                        color = if (selected || isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (hasNote) Text("•", fontSize = 8.sp, color = Color(0xFFF1C40F))
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f).height(42.dp))
                        }
                    }
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
            Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { vm.selectCalMonth(i) },
            colors = CardDefaults.cardColors(containerColor = if (selected) IsaG2 else MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                AppConstants.CAL_MES_CURTO[i],
                Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── AVISOS / RANKING ───────────────────────────────────────────────────────

@Composable
fun AvisosScreen(state: IsaUiState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.announcements.isEmpty()) {
            item { IsaEmptyState("📢", "Nenhum aviso", "Comunicados da escola aparecerão aqui.") }
        } else {
            items(state.announcements) { a ->
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFFE67E22).copy(0.15f), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("📢", fontSize = 16.sp) }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(a.adminName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(formatTs(a.createdAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(a.message, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingScreen(state: IsaUiState, currentUid: String?, vm: IsaViewModel) {
    val updatedLabel = if (state.rankingLastUpdate > 0) {
        val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("pt", "BR"))
        "Atualizado às ${fmt.format(java.util.Date(state.rankingLastUpdate))}"
    } else ""
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (updatedLabel.isNotBlank()) {
            item { Text(updatedLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (state.ranking.isEmpty()) {
            item { IsaEmptyState("🏆", "Ranking vazio", "Seja o primeiro a completar seus estudos!") }
        } else {
            itemsIndexed(state.ranking) { i, u ->
                val isMe = u.uid == currentUid
                val medal = isaRankingMedal(i + 1)
                Card(
                    modifier = Modifier.clickable { if (u.uid.isNotBlank()) vm.viewUserProfile(u.uid) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) IsaG2.copy(0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isMe) androidx.compose.foundation.BorderStroke(2.dp, IsaG2.copy(0.4f)) else null
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(medal, fontWeight = FontWeight.Black, modifier = Modifier.width(40.dp), fontSize = if (i < 3) 22.sp else 14.sp)
                        Column(Modifier.weight(1f)) {
                            Text(u.name + if (isMe) " (você)" else "", fontWeight = FontWeight.Bold)
                            Text("🔥 ${u.streak} dias · 🪙 ${u.coins} moedas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─── PERFIL ─────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(state: IsaUiState, vm: IsaViewModel) {
    var bio by remember(state.profile?.bio) { mutableStateOf(state.profile?.bio ?: "") }
    var series by remember(state.profile?.userSeries) { mutableStateOf(state.profile?.userSeries ?: "") }
    var displayName by remember(state.profile?.name) { mutableStateOf(state.profile?.name ?: "") }
    val profile = state.profile
    val equipped = profile?.equippedItems ?: emptyMap()
    val titleId = equipped["title"] as? String
    val titleItem = titleId?.let { br.com.isa.rotinaestudos.domain.ShopCatalog.byId[it] }
    val mascotId = equipped["mascot"] as? String
    val mascotItem = mascotId?.let { br.com.isa.rotinaestudos.domain.ShopCatalog.byId[it] }
    val badges = (equipped["badges"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val accessories = (equipped["accessories"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val mascotLevel = (profile?.mascotData?.get("level") as? Number)?.toInt() ?: 1
    val mascotXp = (profile?.mascotData?.get("xp") as? Number)?.toInt() ?: 0
    val effectId = equipped["effect"] as? String
    val effectItem = effectId?.let { br.com.isa.rotinaestudos.domain.ShopCatalog.byId[it] }
    val trophyId = equipped["trophy"] as? String
    val medalId = equipped["medal"] as? String
    val funnyIds = listOfNotNull(equipped["funny"] as? String)
    val bannerColors = bannerGradient(equipped["banner"] as? String)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth()) {
                Card(shape = IsaCardShape, elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(bannerColors)
                        ) {
                            val iconId = equipped["profileIcon"] as? String
                            val icon = iconId?.let { br.com.isa.rotinaestudos.domain.ShopCatalog.byId[it]?.icon } ?: ""
                            if (icon.isNotEmpty()) {
                                Text(icon, fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                            }
                        }
                        Spacer(Modifier.height(52.dp))
                        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                buildString {
                                    append(profile?.name ?: "")
                                    (equipped["nameEmoji"] as? String)?.let { id ->
                                        br.com.isa.rotinaestudos.domain.ShopCatalog.byId[id]?.icon?.let { append(" $it") }
                                    }
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = nameColor(equipped["nameColor"] as? String)
                            )
                            if (titleItem != null) {
                                Text(titleItem.name.removePrefix("Título: "), fontSize = 12.sp, color = IsaG2, fontWeight = FontWeight.Bold)
                            }
                            Text(profile?.email ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (mascotItem != null) {
                                Spacer(Modifier.height(6.dp))
                                Text("${mascotItem.icon} ${mascotItem.name} · Nv.$mascotLevel · $mascotXp XP", fontSize = 11.sp, color = IsaPurple)
                            }
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp)
                        .size(88.dp)
                        .border(4.dp, frameColor(equipped["frame"] as? String), CircleShape),
                    color = IsaG4,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!profile?.photoURL.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.photoURL,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    profile?.name?.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IsaG1
                                )
                                accessories.take(2).forEach { id ->
                                    Text(br.com.isa.rotinaestudos.domain.ShopCatalog.byId[id]?.icon ?: "", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (badges.isNotEmpty()) {
            item {
                IsaSectionCard("🏅 Distintivos") {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        badges.forEach { id ->
                            val item = br.com.isa.rotinaestudos.domain.ShopCatalog.byId[id]
                            if (item != null) {
                                Surface(shape = RoundedCornerShape(10.dp), color = IsaG2.copy(0.1f)) {
                                    Text("${item.icon} ${item.name}", Modifier.padding(8.dp), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("🔥", "${profile?.streak ?: 0}", "Sequência", Modifier.weight(1f))
                StatBox("🪙", "${profile?.coins ?: 0}", "Moedas", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("🏆", state.myRankPosition ?: "—", "Ranking", Modifier.weight(1f))
                StatBox("📅", joinedLabel(profile?.joinedAt), "Entrada", Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("⏱️", formatStudyTime(profile?.totalStudySeconds ?: 0), "Estudo", Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                    modifier = Modifier.weight(1f).clickable {
                        profile?.uid?.let { vm.viewUserProfile(it) }
                    }
                ) {
                    Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👁️", fontSize = 20.sp)
                        Text("Ver perfil", fontWeight = FontWeight.Black, fontSize = 13.sp, color = IsaG1)
                        Text("como outros veem", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (!state.isGuest) {
            item {
                IsaSectionCard("✏️ Nome de exibição") {
                    OutlinedTextField(value = displayName, onValueChange = { displayName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Seu nome") })
                    Text(
                        if (vm.canChangeName()) "Pode alterar agora" else "Aguarde 14 dias entre alterações",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IsaPrimaryButton(text = "Salvar nome", onClick = { vm.saveDisplayName(displayName) }, enabled = vm.canChangeName())
                }
            }
        }
        if (effectItem != null || trophyId != null || medalId != null || funnyIds.isNotEmpty()) {
            item {
                IsaSectionCard("✨ Cosméticos equipados") {
                    if (effectItem != null) Text("${effectItem.icon} ${effectItem.name}", fontSize = 12.sp)
                    trophyId?.let { br.com.isa.rotinaestudos.domain.ShopCatalog.byId[it] }?.let { Text("${it.icon} ${it.name}", fontSize = 12.sp) }
                    medalId?.let { br.com.isa.rotinaestudos.domain.ShopCatalog.byId[it] }?.let { Text("${it.icon} ${it.name}", fontSize = 12.sp) }
                    funnyIds.forEach { id -> br.com.isa.rotinaestudos.domain.ShopCatalog.byId[id]?.let { Text("${it.icon} ${it.name}", fontSize = 12.sp) } }
                }
            }
        }
        item {
            IsaSectionCard("✏️ Bio") {
                OutlinedTextField(value = bio, onValueChange = { bio = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Sobre você") }, shape = RoundedCornerShape(14.dp))
            }
        }
        item {
            IsaSectionCard("🎓 Série escolar") {
                AppConstants.SERIES_OPTIONS.forEach { opt ->
                    Row(Modifier.fillMaxWidth().clickable { series = opt }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = series == opt, onClick = { series = opt })
                        Text(opt)
                    }
                }
            }
        }
        item {
            IsaPrimaryButton(
                text = "Salvar perfil",
                onClick = { vm.updateBio(bio); vm.updateSeries(series) }
            )
        }
    }
}

@Composable
private fun StatBox(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = IsaG1)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── FLASHCARDS / ADMIN ─────────────────────────────────────────────────────

@Composable
fun FlashcardsScreen(state: IsaUiState, vm: IsaViewModel) {
    var subject by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var studyingSetId by remember { mutableStateOf<String?>(null) }
    var cardIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    val sets = state.profile?.flashcardSets ?: emptyList()
    val studySet = sets.find { it.id == studyingSetId }

    if (studySet != null) {
        val cards = studySet.cards
        Column(Modifier.fillMaxWidth()) {
            TextButton(onClick = { studyingSetId = null; cardIndex = 0; showAnswer = false }) {
                Text("← Voltar aos conjuntos")
            }
            Text(studySet.subject, fontWeight = FontWeight.Black, fontSize = 17.sp, color = IsaG1)
            Text("Cartão ${cardIndex + 1} de ${cards.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            if (cards.isEmpty()) {
                IsaEmptyState("🃏", "Vazio", "Adicione cartões a este conjunto.")
            } else {
                val card = cards[cardIndex.coerceIn(0, cards.lastIndex)]
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clickable { showAnswer = !showAnswer },
                    shape = IsaCardShape,
                    colors = CardDefaults.cardColors(containerColor = IsaPurple.copy(0.1f))
                ) {
                    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (showAnswer) card.a else card.q,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
                Text(
                    if (showAnswer) "Toque para ver pergunta" else "Toque para ver resposta",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IsaPrimaryButton(
                        text = "← Anterior",
                        onClick = { cardIndex = (cardIndex - 1).coerceAtLeast(0); showAnswer = false },
                        modifier = Modifier.weight(1f),
                        enabled = cardIndex > 0
                    )
                    IsaGoldButton(
                        text = "Próximo →",
                        onClick = { cardIndex = (cardIndex + 1).coerceAtMost(cards.lastIndex); showAnswer = false },
                        modifier = Modifier.weight(1f),
                        enabled = cardIndex < cards.lastIndex
                    )
                }
            }
        }
    } else {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            IsaSectionCard("➕ Novo flashcard") {
                IsaTextField(value = subject, onValueChange = { subject = it }, label = "Matéria / conjunto")
                Spacer(Modifier.height(8.dp))
                IsaTextField(value = question, onValueChange = { question = it }, label = "Pergunta")
                Spacer(Modifier.height(8.dp))
                IsaTextField(value = answer, onValueChange = { answer = it }, label = "Resposta")
                Spacer(Modifier.height(10.dp))
                IsaPrimaryButton(
                    text = "Adicionar cartão",
                    onClick = {
                        vm.addFlashcardSet(subject, question, answer)
                        question = ""; answer = ""
                    },
                    enabled = subject.isNotBlank() && question.isNotBlank()
                )
            }
            Spacer(Modifier.height(12.dp))
            if (sets.isEmpty()) {
                IsaEmptyState("🃏", "Nenhum conjunto", "Crie seu primeiro flashcard acima!")
            } else {
                sets.forEach { set ->
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = IsaPurple.copy(0.12f), modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("🃏", fontSize = 22.sp) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f).clickable { studyingSetId = set.id; cardIndex = 0; showAnswer = false }) {
                                Text(set.subject, fontWeight = FontWeight.Bold)
                                Text("${set.cards.size} cartões · toque para estudar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { vm.deleteFlashcardSet(set.id) }) { Text("🗑", fontSize = 16.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScreen(state: IsaUiState, vm: IsaViewModel) {
    var date by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("avaliacao") }

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        IsaTextField(value = date, onValueChange = { date = it }, label = "Data (YYYY-MM-DD)")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AppConstants.CAL_EVENT_LABELS.keys.take(8).forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(AppConstants.CAL_EVENT_LABELS[t] ?: t, fontSize = 10.sp) })
            }
        }
        IsaTextField(value = title, onValueChange = { title = it }, label = "Título")
        IsaTextField(value = message, onValueChange = { message = it }, label = "Descrição")
        IsaPrimaryButton(
            text = "Adicionar evento",
            onClick = { vm.saveAdminEvent(date, title, message, type, null, false) }
        )
        Text("Calendário escolar 2026", fontWeight = FontWeight.Black, color = IsaG1)
        SchoolCalendar2026.build().entries.sortedBy { it.key }.take(30).forEach { (d, ev) ->
            Card(Modifier.fillMaxWidth().clickable { date = d; title = ev.title; type = ev.type }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(d, fontSize = 11.sp, modifier = Modifier.width(96.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(ev.title, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── HELPERS ────────────────────────────────────────────────────────────────

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
    .replace("&amp;", "&")
    .trim()

private fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    IsaGreen
}

private fun formatTs(ms: Long): String =
    if (ms == 0L) "" else java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("pt", "BR")).format(java.util.Date(ms))

private fun formatStudyTime(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    return if (h > 0) "${h}h" else "${m}m"
}

private fun bannerGradient(bannerId: String?): Brush = when (bannerId) {
    "banner_ocean" -> Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))
    "banner_sunset" -> Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF7043)))
    "banner_galaxy" -> Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF7B1FA2)))
    "banner_forest" -> Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF43A047)))
    "banner_gold" -> Brush.linearGradient(listOf(Color(0xFFFF8F00), Color(0xFFFFD54F)))
    else -> Brush.linearGradient(listOf(IsaG1, IsaG2, Color(0xFF66BB44)))
}

private fun nameColor(colorId: String?): Color = when (colorId) {
    "color_emerald" -> Color(0xFF2E7D32)
    "color_gold" -> Color(0xFFFFB300)
    "color_purple" -> Color(0xFF7B1FA2)
    "color_coral" -> Color(0xFFFF5722)
    "color_cyan" -> Color(0xFF00ACC1)
    "name_rainbow" -> IsaG2
    else -> IsaG1
}

private fun frameColor(frameId: String?): Color = when (frameId) {
    "frame_gold" -> Color(0xFFFFB300)
    "frame_diamond" -> Color(0xFF42A5F5)
    "frame_fire" -> Color(0xFFFF5722)
    "frame_rainbow" -> Color(0xFFAB47BC)
    "frame_legend" -> Color(0xFFFFD700)
    else -> IsaG2
}

private fun joinedLabel(joinedAt: String?): String {
    if (joinedAt.isNullOrBlank()) return "—"
    return try {
        joinedAt.substring(0, minOf(10, joinedAt.length)).replace("-", "/")
    } catch (_: Exception) {
        "—"
    }
}
