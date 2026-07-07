package br.com.isa.rotinaestudos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.isa.rotinaestudos.ui.components.IsaGoldButton
import br.com.isa.rotinaestudos.ui.components.IsaHeroBackground
import br.com.isa.rotinaestudos.ui.components.IsaPrimaryButton
import br.com.isa.rotinaestudos.ui.theme.IsaCardShape
import br.com.isa.rotinaestudos.ui.theme.IsaG1
import br.com.isa.rotinaestudos.ui.theme.IsaG2

@Composable
fun AvisoScreen(onContinue: () -> Unit) {
    var accepted by remember { mutableStateOf(false) }
    IsaHeroBackground {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(shape = IsaCardShape, elevation = CardDefaults.cardElevation(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aviso Importante", fontWeight = FontWeight.Black, fontSize = 20.sp, color = IsaG1)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "ESSE APP AINDA ESTÁ EM DESENVOLVIMENTO E QUALQUER BUGS E PROBLEMAS PODERÃO SER RESOLVIDOS NO FUTURO. CASO HAJA ALGUM PROBLEMA, FAVOR RELATAR PARA NÓS.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                        Text("Li e concordo em prosseguir", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(16.dp))
                    IsaPrimaryButton(text = "Prosseguir →", onClick = onContinue, enabled = accepted)
                }
            }
        }
    }
}

@Composable
fun IntroScreen(onStartQuiz: () -> Unit) {
    IsaHeroBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(shape = IsaCardShape, elevation = CardDefaults.cardElevation(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Questionário ISA", fontWeight = FontWeight.Black, fontSize = 20.sp, color = IsaG1)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Você receberá 25 perguntas utilizadas para criar a sua rotina de estudo.\n\nEsse processo leva apenas alguns minutos.\n\nVocê está pronto?",
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                        color = IsaG2,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))
                    IsaGoldButton(text = "Começar questionário →", onClick = onStartQuiz)
                }
            }
        }
    }
}
