package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.data.model.GradesConfig
import br.com.isa.rotinaestudos.data.model.SubjectGrades
import br.com.isa.rotinaestudos.domain.DicasData
import br.com.isa.rotinaestudos.domain.GradesHelper
import br.com.isa.rotinaestudos.domain.RevisionEntry
import br.com.isa.rotinaestudos.domain.ShopCatalog
import br.com.isa.rotinaestudos.ui.IsaUiState
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.theme.IsaCoin
import br.com.isa.rotinaestudos.ui.components.IsaEmptyState
import br.com.isa.rotinaestudos.ui.components.IsaGoldButton
import br.com.isa.rotinaestudos.ui.components.IsaPrimaryButton
import br.com.isa.rotinaestudos.ui.components.IsaSectionCard
import br.com.isa.rotinaestudos.ui.theme.IsaCardShape
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2
import br.com.isa.rotinaestudos.ui.theme.IsaG4
import br.com.isa.rotinaestudos.ui.theme.IsaPurple

// ─── MÉTODOS + RECOMENDAÇÕES ────────────────────────────────────────────────

@Composable
fun MethodsScreen(methods: List<String>, recommendations: List<String>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        item {
            IsaSectionCard("🧠 Métodos de Estudo") {
                if (methods.isEmpty()) {
                    Text("Complete o quiz para ver métodos personalizados.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                } else {
                    methods.forEach { m ->
                        MethodCard(m)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        item {
            IsaSectionCard("💡 Recomendações") {
                if (recommendations.isEmpty()) {
                    Text("Recomendações aparecem após gerar sua rotina.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                } else {
                    recommendations.forEach { r ->
                        MethodCard(r)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodCard(text: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = IsaG4.copy(0.4f))) {
        Row(Modifier.padding(14.dp)) {
            Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(4.dp)).background(IsaG2))
            Spacer(Modifier.width(10.dp))
            Text(stripHtml(text), fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

// ─── REVISÕES ESPAÇADAS ─────────────────────────────────────────────────────

@Composable
fun RevisionsScreen(revisions: List<RevisionEntry>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        if (revisions.isEmpty()) {
            item { IsaEmptyState("🔁", "Sem revisões", "Complete o quiz para ver seu plano de repetição espaçada.") }
        } else {
            items(revisions) { rev ->
                Card(shape = IsaCardShape, elevation = CardDefaults.cardElevation(3.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rev.subject, fontWeight = FontWeight.Black, fontSize = 15.sp, color = IsaG1, modifier = Modifier.weight(1f))
                            Text("${rev.difficulty}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            RevBadge("+1 dia", rev.day1)
                            RevBadge("+7 dias", rev.day7)
                            RevBadge("+30 dias", rev.day30)
                        }
                    }
                }
            }
            item {
                Text(
                    "Baseado em repetição espaçada — o método mais eficaz para retenção de longo prazo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RevBadge(label: String, date: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = IsaG2.copy(0.12f)) {
        Text("$label: $date", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IsaG1)
    }
}

// ─── DICAS ──────────────────────────────────────────────────────────────────

@Composable
fun DicasScreen(onDone: () -> Unit) {
    var selectedCat by remember { mutableIntStateOf(-1) }
    val doneTips = remember { mutableStateOf(setOf<String>()) }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        if (selectedCat < 0) {
            Text("Escolha uma categoria:", fontWeight = FontWeight.Bold, color = IsaG1, modifier = Modifier.padding(bottom = 12.dp))
            DicasData.categories.forEachIndexed { i, cat ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { selectedCat = i },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icon, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        } else {
            val cat = DicasData.categories[selectedCat]
            TextButton(onClick = { selectedCat = -1 }) { Text("← Voltar às categorias") }
            Text("${cat.icon} ${cat.name}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = IsaG1, modifier = Modifier.padding(vertical = 8.dp))
            cat.tips.forEach { tip ->
                val key = "${cat.name}:$tip"
                val done = doneTips.value.contains(key)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (done) IsaG2.copy(0.08f) else MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡 $tip", modifier = Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (done) IsaG2 else IsaG2.copy(0.15f),
                            modifier = Modifier.clickable(enabled = !done) {
                                doneTips.value = doneTips.value + key
                                onDone()
                            }
                        ) {
                            Text(
                                if (done) "✓" else "Feito!",
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (done) Color.White else IsaG1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── LOJA ───────────────────────────────────────────────────────────────────

@Composable
fun ShopScreen(state: IsaUiState, vm: IsaViewModel) {
    val profile = state.profile
    val filtered = ShopCatalog.items.filter { ShopCatalog.matchesFilter(it, state.shopFilter) }

    Column {
        Surface(shape = RoundedCornerShape(14.dp), color = IsaCoin.copy(0.15f), modifier = Modifier.fillMaxWidth()) {
            Text(
                "🪙 ${profile?.coins ?: 0} moedas",
                Modifier.padding(14.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = IsaCoin
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ShopCatalog.categories.forEach { cat ->
                FilterChip(
                    selected = state.shopFilter == cat.id,
                    onClick = { vm.setShopFilter(cat.id) },
                    label = { Text(cat.label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IsaG2, selectedLabelColor = Color.White)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(420.dp)) {
            items(filtered) { item ->
                val owned = profile?.ownedItems?.contains(item.id) == true
                val equipped = profile?.let { vm.isEquipped(it, item.id) } == true
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.icon, fontSize = 32.sp, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            Text(ShopCatalog.rarityLabel(item.rarity), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = rarityColor(item.rarity))
                        }
                        when {
                            !owned -> IsaPrimaryButton(
                                text = "🪙 ${item.price}",
                                onClick = { vm.buyShopItem(item.id) },
                                modifier = Modifier.width(90.dp)
                            )
                            item.slot.isNotBlank() -> Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (equipped) IsaG2 else IsaG2.copy(0.12f),
                                modifier = Modifier.clickable { vm.toggleEquipItem(item.id) }
                            ) {
                                Text(
                                    if (equipped) "✓ On" else "Equipar",
                                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (equipped) Color.White else IsaG1
                                )
                            }
                            else -> Text("✓", fontWeight = FontWeight.Black, color = IsaG2)
                        }
                    }
                }
            }
        }
    }
}

private fun rarityColor(r: String) = when (r) {
    "legend" -> Color(0xFFFFD700)
    "epic" -> Color(0xFF9C27B0)
    "rare" -> Color(0xFF2196F3)
    else -> Color(0xFF78909C)
}

// ─── NOTAS ──────────────────────────────────────────────────────────────────

@Composable
fun GradesScreen(state: IsaUiState, vm: IsaViewModel) {
    val cfg = state.profile?.gradesConfig
    var wizardStep by remember { mutableIntStateOf(0) }
    var style by remember { mutableStateOf("isa") }
    var minAvg by remember { mutableStateOf("7") }
    var examNames by remember { mutableStateOf(GradesHelper.defaultExamNames()) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    if (cfg?.setupDone == true && selectedSubject == null) {
        GradesSubjectList(state, vm) { selectedSubject = it }
    } else if (cfg?.setupDone == true && selectedSubject != null) {
        GradesSubjectDetail(
            subject = selectedSubject!!,
            cfg = cfg,
            data = state.profile?.gradesData?.get(selectedSubject!!) ?: SubjectGrades(),
            onBack = { selectedSubject = null },
            onGradeChange = { id, val_, type -> vm.setNotaGrade(selectedSubject!!, id, val_, type) }
        )
    } else {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            when (wizardStep) {
                0 -> {
                    Text("Estilo de cálculo", fontWeight = FontWeight.Bold, color = IsaG1)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StyleChip("ISA (escola)", style == "isa") { style = "isa" }
                        StyleChip("Personalizado", style == "custom") { style = "custom" }
                    }
                    Spacer(Modifier.height(16.dp))
                    IsaPrimaryButton(text = "Próximo →", onClick = { wizardStep = 1 })
                }
                1 -> {
                    OutlinedTextField(value = minAvg, onValueChange = { minAvg = it }, label = { Text("Média mínima") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Row {
                        TextButton(onClick = { wizardStep = 0 }) { Text("← Voltar") }
                        Spacer(Modifier.weight(1f))
                        IsaPrimaryButton(text = "Próximo →", onClick = { wizardStep = 2 }, modifier = Modifier.width(140.dp))
                    }
                }
                else -> {
                    Text("Provas do ano (${examNames.size})", fontWeight = FontWeight.Bold, color = IsaG1)
                    examNames.forEachIndexed { i, name ->
                        OutlinedTextField(
                            value = name,
                            onValueChange = { v -> examNames = examNames.toMutableList().also { it[i] = v } },
                            label = { Text("Prova ${i + 1}") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                    Row(Modifier.padding(vertical = 8.dp)) {
                        TextButton(onClick = {
                            val pair = examNames.size / 2 + 1
                            examNames = examNames + listOf(
                                "Avaliação Mensal $pair (AVM$pair)",
                                "Avaliação Bimestral $pair (AVB$pair)"
                            )
                        }) { Text("+ Par de provas") }
                    }
                    IsaGoldButton(
                        text = "✅ Concluir configuração",
                        onClick = {
                            vm.finishGradesSetup(style, minAvg.replace(',', '.').toDoubleOrNull() ?: 7.0, examNames)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) IsaG2 else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick).border(if (selected) 0.dp else 1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(12.dp))
    ) {
        Text(label, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), fontWeight = FontWeight.Bold, color = if (selected) Color.White else IsaG1, fontSize = 12.sp)
    }
}

@Composable
private fun GradesSubjectList(state: IsaUiState, vm: IsaViewModel, onSelect: (String) -> Unit) {
    val subjects = vm.getNotasSubjects()
    val cfg = state.profile?.gradesConfig
    if (subjects.isEmpty()) {
        IsaEmptyState("📊", "Sem matérias", "Complete o quiz para usar as notas escolares.")
    } else {
        subjects.forEach { sub ->
            val st = GradesHelper.calcSubjectStatus(cfg, sub, state.profile?.gradesData ?: emptyMap())
            val (label, color) = when {
                st.pass -> "Aprovado" to IsaG2
                st.deficit > 0 -> "Faltam ${"%.1f".format(st.deficit)} pts" to Color(0xFFFFB74D)
                else -> "Em risco" to Color(0xFFEF5350)
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onSelect(sub) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(sub, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(0.15f)) {
                        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradesSubjectDetail(
    subject: String,
    cfg: GradesConfig,
    data: SubjectGrades,
    onBack: () -> Unit,
    onGradeChange: (String, String, String) -> Unit
) {
    val analysis = GradesHelper.calcSubjectAnalysis(cfg, data)
    Column(Modifier.verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) { Text("← Voltar às matérias") }
        Text(subject, fontWeight = FontWeight.Black, fontSize = 17.sp, color = IsaG1)
        Spacer(Modifier.height(12.dp))
        cfg.timeline.forEach { item ->
            when (item.type) {
                "exam" -> {
                    val val_ = data.exams[item.id] ?: ""
                    OutlinedTextField(
                        value = val_,
                        onValueChange = { onGradeChange(item.id, it, "exam") },
                        label = { Text(item.name) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                "recovery" -> {
                    val val_ = data.recoveries[item.id] ?: ""
                    OutlinedTextField(
                        value = val_,
                        onValueChange = { onGradeChange(item.id, it, "recovery") },
                        label = { Text("🔁 ${item.name}") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                "provaFinal" -> {
                    OutlinedTextField(
                        value = data.provaFinal,
                        onValueChange = { onGradeChange("pf", it, "pf") },
                        label = { Text("📝 ${item.name}") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        IsaSectionCard("📈 Situação no ano") {
            Text("Meta: ${"%.1f".format(analysis.targetSum)} · Atual: ${"%.1f".format(analysis.mediaSum)}", fontSize = 13.sp)
            Text("Falta: ${"%.1f".format(analysis.deficit)} pontos", fontSize = 13.sp)
            if (analysis.emptyExams > 0) {
                Text("Provas restantes: ${analysis.emptyExams} · Média necessária ~${"%.1f".format(analysis.neededPerExam)}", fontSize = 13.sp)
            }
            analysis.rec2?.let { rec ->
                Text("2ª Recuperação: $rec ${if (rec >= cfg.minAvg) "✅" else "(precisa ≥ ${cfg.minAvg})"}", fontSize = 12.sp)
            }
            Text(
                if (analysis.passed) "✅ Aprovado" else "❌ Ainda não passou",
                fontWeight = FontWeight.Bold,
                color = if (analysis.passed) IsaG2 else Color(0xFFEF5350),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// ─── TIMER ──────────────────────────────────────────────────────────────────

@Composable
fun StudyTimerScreen(state: IsaUiState, vm: IsaViewModel) {
    val session = state.studyTimerSessionSeconds
    val total = state.profile?.totalStudySeconds ?: 0L
    val h = session / 3600
    val m = (session % 3600) / 60
    val s = session % 60
    val display = if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Sessão atual", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(display, fontSize = 52.sp, fontWeight = FontWeight.Black, color = IsaG1)
        Text("Total estudado: ${formatTotalSeconds(total)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.studyTimerRunning) {
                IsaPrimaryButton(text = "⏸ Pausar", onClick = vm::pauseStudyTimer, modifier = Modifier.weight(1f))
            } else {
                IsaGoldButton(text = "▶ Iniciar", onClick = vm::startStudyTimer, modifier = Modifier.weight(1f))
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f).clickable { vm.resetStudyTimerSession() }
            ) {
                Text("↺ Zerar", Modifier.padding(vertical = 16.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        IsaPrimaryButton(text = "💾 Salvar sessão", onClick = vm::saveStudyTimer, enabled = session > 0 && !state.studyTimerRunning)
        if (state.profile?.studyMotivation?.isNotBlank() == true) {
            Spacer(Modifier.height(16.dp))
            IsaSectionCard("💪 Motivação") {
                Text(state.profile!!.studyMotivation, fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
    }
}

private fun formatTotalSeconds(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    return if (h > 0) "${h}h ${m}min" else "${m}min"
}

private fun stripHtml(html: String): String = html
    .replace(Regex("<[^>]+>"), "")
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .trim()
