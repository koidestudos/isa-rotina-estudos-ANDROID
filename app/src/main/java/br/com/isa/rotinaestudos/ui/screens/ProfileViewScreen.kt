package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.data.model.UserProfile
import br.com.isa.rotinaestudos.domain.ShopCatalog
import br.com.isa.rotinaestudos.ui.components.IsaSectionCard
import br.com.isa.rotinaestudos.ui.theme.IsaCardShape
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2
import br.com.isa.rotinaestudos.ui.theme.IsaG4
import br.com.isa.rotinaestudos.ui.theme.IsaPurple
import coil.compose.AsyncImage

@Composable
fun UserProfileFullScreen(
    profile: UserProfile?,
    isOwn: Boolean = false,
    rankPosition: String? = null,
    onBack: () -> Unit,
    onViewProfile: ((String) -> Unit)? = null
) {
    if (profile == null) return
    val equipped = profile.equippedItems
    val titleId = equipped["title"] as? String
    val titleItem = titleId?.let { ShopCatalog.byId[it] }
    val mascotId = equipped["mascot"] as? String
    val mascotItem = mascotId?.let { ShopCatalog.byId[it] }
    val badges = (equipped["badges"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val accessories = (equipped["accessories"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val mascotLevel = (profile.mascotData["level"] as? Number)?.toInt() ?: 1
    val mascotXp = (profile.mascotData["xp"] as? Number)?.toInt() ?: 0
    val effectId = equipped["effect"] as? String
    val effectItem = effectId?.let { ShopCatalog.byId[it] }
    val trophyId = equipped["trophy"] as? String
    val medalId = equipped["medal"] as? String
    val funnyId = equipped["funny"] as? String
    val nameGlow = equipped["nameGlow"] as? String

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Perfil", fontWeight = FontWeight.Black, fontSize = 18.sp, color = IsaG1)
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Card(shape = IsaCardShape, elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(profileBannerBrush(equipped["banner"] as? String))
                        ) {
                            val iconId = equipped["profileIcon"] as? String
                            val icon = iconId?.let { ShopCatalog.byId[it]?.icon } ?: ""
                            if (icon.isNotEmpty()) {
                                Text(icon, fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                            }
                            if (mascotItem != null) {
                                Column(
                                    Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(mascotItem.icon, fontSize = 32.sp)
                                    Text("Nv. $mascotLevel · $mascotXp XP", fontSize = 10.sp, color = Color.White.copy(0.85f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(52.dp))
                        Column(
                            Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                buildString {
                                    (equipped["nameEmoji"] as? String)?.let { id ->
                                        ShopCatalog.byId[id]?.icon?.let { append("$it ") }
                                    }
                                    append(profile.name.ifBlank { "Estudante" })
                                    if (nameGlow != null) append(" ✨")
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = profileNameColor(equipped["nameColor"] as? String),
                                textAlign = TextAlign.Center
                            )
                            if (titleItem != null) {
                                Text(
                                    "« ${titleItem.name.removePrefix("Título: ")} »",
                                    fontSize = 12.sp,
                                    color = IsaPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (profile.userSeries.isNotBlank()) {
                                Text("🎓 ${profile.userSeries}", fontSize = 12.sp, color = IsaG2, fontWeight = FontWeight.Bold)
                            }
                            if (isOwn && profile.email.isNotBlank()) {
                                Text(profile.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .size(96.dp)
                        .border(4.dp, profileFrameColor(equipped["frame"] as? String), CircleShape),
                    color = IsaG4,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (profile.photoURL.isNotBlank()) {
                            AsyncImage(
                                model = profile.photoURL,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                profile.name.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = IsaG1
                            )
                        }
                        accessories.take(2).forEachIndexed { i, id ->
                            Text(
                                ShopCatalog.byId[id]?.icon ?: "",
                                fontSize = 16.sp,
                                modifier = Modifier.align(if (i == 0) Alignment.TopEnd else Alignment.BottomStart)
                            )
                        }
                    }
                }
            }

            if (badges.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.forEach { id ->
                        ShopCatalog.byId[id]?.let { item ->
                            Surface(shape = RoundedCornerShape(99.dp), color = IsaG2.copy(0.1f)) {
                                Text("${item.icon} ${item.name}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (effectItem != null || trophyId != null || medalId != null || funnyId != null || accessories.isNotEmpty()) {
                IsaSectionCard("✨ Personalização") {
                    effectItem?.let { Text("${it.icon} ${it.name}", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp)) }
                    trophyId?.let { ShopCatalog.byId[it] }?.let { Text("${it.icon} ${it.name}", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp)) }
                    medalId?.let { ShopCatalog.byId[it] }?.let { Text("${it.icon} ${it.name}", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp)) }
                    funnyId?.let { ShopCatalog.byId[it] }?.let { Text("${it.icon} ${it.name}", fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp)) }
                    if (accessories.isNotEmpty()) {
                        Text(
                            accessories.mapNotNull { ShopCatalog.byId[it]?.icon }.joinToString(" "),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileStatCard("🔥", "${profile.streak}", "Sequência", Modifier.weight(1f))
                ProfileStatCard("🪙", "${profile.coins}", "Moedas", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileStatCard("⏱️", formatStudyTime(profile.totalStudySeconds), "Estudo", Modifier.weight(1f))
                if (isOwn && rankPosition != null) {
                    ProfileStatCard("🏆", rankPosition, "Ranking", Modifier.weight(1f))
                } else if (profile.joinedAt.isNotBlank()) {
                    ProfileStatCard("📅", joinedLabel(profile.joinedAt), "Entrada", Modifier.weight(1f))
                }
            }

            IsaSectionCard("Bio") {
                Text(
                    profile.bio.ifBlank { "Sem bio ainda." },
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ProfileStatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 18.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 15.sp, color = IsaG1, textAlign = TextAlign.Center)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun profileBannerBrush(bannerId: String?): Brush = when (bannerId) {
    "banner_ocean" -> Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))
    "banner_sunset" -> Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF7043)))
    "banner_galaxy" -> Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF7B1FA2)))
    "banner_forest" -> Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF43A047)))
    "banner_gold" -> Brush.linearGradient(listOf(Color(0xFFFF8F00), Color(0xFFFFD54F)))
    else -> Brush.linearGradient(listOf(IsaG1, IsaG2, Color(0xFF66BB44)))
}

private fun profileNameColor(colorId: String?): Color = when (colorId) {
    "color_emerald" -> Color(0xFF2E7D32)
    "color_gold" -> Color(0xFFFFB300)
    "color_purple" -> Color(0xFF7B1FA2)
    "color_coral" -> Color(0xFFFF5722)
    "color_cyan" -> Color(0xFF00ACC1)
    else -> IsaG1
}

private fun profileFrameColor(frameId: String?): Color = when (frameId) {
    "frame_gold" -> Color(0xFFFFB300)
    "frame_diamond" -> Color(0xFF42A5F5)
    "frame_fire" -> Color(0xFFFF5722)
    "frame_rainbow" -> Color(0xFFAB47BC)
    "frame_legend" -> Color(0xFFFFD700)
    else -> IsaG2
}

private fun formatStudyTime(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun joinedLabel(joinedAt: String): String =
    joinedAt.take(10).replace("-", "/").ifBlank { "—" }
