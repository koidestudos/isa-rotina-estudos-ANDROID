package br.com.isa.rotinaestudos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.isa.rotinaestudos.ui.MainTab
import br.com.isa.rotinaestudos.ui.OverlaySheet
import br.com.isa.rotinaestudos.ui.theme.IsaButtonShape
import br.com.isa.rotinaestudos.ui.theme.IsaCardShape
import br.com.isa.rotinaestudos.ui.theme.IsaCoin
import br.com.isa.rotinaestudos.ui.theme.IsaCoinText
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2
import br.com.isa.rotinaestudos.ui.theme.IsaG3
import br.com.isa.rotinaestudos.ui.theme.IsaOrange
import br.com.isa.rotinaestudos.ui.theme.IsaPillShape
import br.com.isa.rotinaestudos.ui.theme.IsaPurple
import br.com.isa.rotinaestudos.ui.theme.IsaYellow

val IsaHeroGradient = Brush.linearGradient(
    colors = listOf(IsaG1, IsaG2, Color(0xFF66BB44)),
    start = androidx.compose.ui.geometry.Offset(0f, 0f),
    end = androidx.compose.ui.geometry.Offset(1000f, 1200f)
)

val IsaPrimaryButtonGradient = Brush.linearGradient(listOf(IsaG2, IsaG3))
val IsaGoldButtonGradient = Brush.linearGradient(listOf(IsaCoin, IsaYellow))

@Composable
fun IsaHeroBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(IsaHeroGradient)) {
        content()
    }
}

@Composable
fun IsaBootScreen() {
    IsaHeroBackground {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IsaG2,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("ISA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                "Carregando ISA Estudos...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun IsaSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, IsaCardShape, ambientColor = Color.Black.copy(0.08f)),
        shape = IsaCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = IsaG1)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun IsaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val alpha by animateFloatAsState(if (enabled) 1f else 0.5f, label = "btnAlpha")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(IsaButtonShape)
            .background(IsaPrimaryButtonGradient)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun IsaGoogleButton(onClick: () -> Unit, enabled: Boolean = true, loading: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(IsaButtonShape)
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(0.5f), IsaButtonShape)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = IsaButtonShape
    ) {
        Row(
            Modifier.padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = IsaG2)
            } else {
                GoogleLogo()
                Spacer(Modifier.width(10.dp))
                Text("Continuar com Google", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun GoogleLogo() {
    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        Text("G", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF4285F4))
    }
}

@Composable
fun IsaGoldButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(IsaButtonShape)
            .background(if (enabled) IsaGoldButtonGradient else Brush.linearGradient(listOf(Color.Gray, Color.LightGray)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
fun IsaGamificationHeader(
    title: String,
    subtitle: String,
    userName: String,
    coins: Int,
    streak: Int,
    onSettingsClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(IsaHeroGradient)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = IsaG2, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("ISA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, color = Color.White.copy(0.85f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = Color.White)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(0.15f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙 $coins", color = IsaCoinText, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("🔥 $streak dias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(userName, color = Color.White.copy(0.9f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun IsaPillTabRow(
    tabs: List<MainTab>,
    selected: MainTab,
    onSelect: (MainTab) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        tabs.forEach { tab ->
            val active = tab == selected
            Surface(
                shape = IsaPillShape,
                color = if (active) IsaG2 else MaterialTheme.colorScheme.surface,
                shadowElevation = if (active) 4.dp else 1.dp,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(tab) }
                    .then(
                        if (!active) Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(0.4f), IsaPillShape)
                        else Modifier
                    )
            ) {
                Text(
                    "${tab.icon} ${tab.label}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
fun IsaQuizProgress(current: Int, total: Int) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Pergunta $current de $total", color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("${(current * 100 / total)}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(IsaPillShape),
            color = Color.White,
            trackColor = Color.White.copy(0.25f)
        )
    }
}

@Composable
fun IsaOptionCard(letter: Char, text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) IsaG2.copy(0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, IsaG2) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (selected) IsaG2 else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        letter.toString(),
                        fontWeight = FontWeight.Black,
                        color = if (selected) Color.White else IsaG1,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun IsaFabStack(
    onCalendar: () -> Unit,
    onTips: () -> Unit,
    onAvisos: () -> Unit,
    onShop: () -> Unit,
    onNotas: () -> Unit,
    onTimer: () -> Unit
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(end = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        IsaFab(color = Color(0xFF29B6F6), icon = "📊", label = "Notas", onClick = onNotas)
        IsaFab(color = Color(0xFFFF7043), icon = "⏱️", label = "Timer", onClick = onTimer)
        IsaFab(color = IsaOrange, icon = "📢", label = "Avisos", onClick = onAvisos)
        IsaFab(color = IsaPurple, icon = "🛍️", label = "Loja", onClick = onShop)
        IsaFab(color = Color.White, icon = "💡", label = "Dicas", onClick = onTips, borderColor = IsaG2)
        IsaFab(color = IsaG2, icon = "📅", label = "Calendário", onClick = onCalendar)
    }
}

@Composable
private fun IsaFab(
    color: Color,
    icon: String,
    label: String,
    onClick: () -> Unit,
    borderColor: Color? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        Surface(
            shape = IsaPillShape,
            color = MaterialTheme.colorScheme.surface.copy(0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IsaG1)
        }
        Surface(
            shape = CircleShape,
            color = color,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(52.dp)
                .then(if (borderColor != null) Modifier.border(2.dp, borderColor, CircleShape) else Modifier)
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 22.sp)
            }
        }
    }
}

@Composable
fun IsaBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.55f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(tween(280)) { it } + fadeIn(tween(200)),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp)
                        .navigationBarsPadding()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {},
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, style = MaterialTheme.typography.headlineMedium, color = IsaG1)
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }
                        Box(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IsaEmptyState(emoji: String, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, textAlign = TextAlign.Center, color = IsaG1)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun isaRankingMedal(position: Int): String = when (position) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> "#$position"
}

@Composable
fun IsaComingSoonCard(title: String, description: String) {
    IsaSectionCard(title) {
        IsaEmptyState("🚀", "Em breve no app", description)
    }
}

fun overlayTitle(sheet: OverlaySheet): String = when (sheet) {
    OverlaySheet.CALENDARIO -> "📅 Calendário"
    OverlaySheet.AVISOS -> "📢 Avisos"
    OverlaySheet.LOJA -> "🛍️ Loja ISA"
    OverlaySheet.DICAS -> "💡 Dicas"
    OverlaySheet.NOTAS -> "📊 Notas Escolares"
    OverlaySheet.TIMER -> "⏱️ Timer de Estudo"
    OverlaySheet.SETTINGS -> "⚙️ Configurações"
    OverlaySheet.ADMIN -> "🛡️ Admin"
    OverlaySheet.FLASHCARDS -> "🃏 Flashcards"
    OverlaySheet.NONE -> ""
}
