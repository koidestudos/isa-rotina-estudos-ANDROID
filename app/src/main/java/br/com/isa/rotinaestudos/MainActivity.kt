package br.com.isa.rotinaestudos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.MainTab
import br.com.isa.rotinaestudos.ui.screens.AdminScreen
import br.com.isa.rotinaestudos.ui.screens.AuthScreen
import br.com.isa.rotinaestudos.ui.screens.AvisosScreen
import br.com.isa.rotinaestudos.ui.screens.CalendarScreen
import br.com.isa.rotinaestudos.ui.screens.FlashcardsScreen
import br.com.isa.rotinaestudos.ui.screens.MaisScreen
import br.com.isa.rotinaestudos.ui.screens.ProfileScreen
import br.com.isa.rotinaestudos.ui.screens.QuizScreen
import br.com.isa.rotinaestudos.ui.screens.RankingScreen
import br.com.isa.rotinaestudos.ui.screens.RoutineScreen
import br.com.isa.rotinaestudos.ui.screens.TextListScreen
import br.com.isa.rotinaestudos.ui.theme.IsaGreen
import br.com.isa.rotinaestudos.ui.theme.IsaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IsaTheme {
                IsaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsaApp(vm: IsaViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    when {
        state.loading && state.authUser == null -> {
            CircularProgressIndicator()
        }
        state.authUser == null -> {
            AuthScreen(
                loading = state.loading,
                error = state.authError,
                onLogin = vm::login,
                onRegister = vm::register
            )
        }
        state.showQuiz -> {
            QuizScreen(state, vm)
        }
        else -> {
            MainShell(state, vm)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(state: br.com.isa.rotinaestudos.ui.IsaUiState, vm: IsaViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "ISA Rotina",
                    modifier = Modifier.padding(20.dp),
                    fontWeight = FontWeight.Black,
                    color = IsaGreen
                )
                MainTab.entries.forEach { tab ->
                    if (tab == MainTab.MAIS) return@forEach
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.selectTab(tab)
                                scope.launch { drawerState.close() }
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(tab.icon)
                        Spacer(Modifier.width(12.dp))
                        Text(tab.label, fontWeight = if (state.currentTab == tab) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.selectTab(MainTab.MAIS)
                            scope.launch { drawerState.close() }
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text("⋯")
                    Spacer(Modifier.width(12.dp))
                    Text("Mais")
                }
                if (state.isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.selectTab(MainTab.ADMIN)
                                vm.loadAdminEvents()
                                scope.launch { drawerState.close() }
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text("🛡️")
                        Spacer(Modifier.width(12.dp))
                        Text("Admin", fontWeight = if (state.currentTab == MainTab.ADMIN) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${state.currentTab.icon} ${state.currentTab.label}") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = IsaGreen, titleContentColor = Color.White, navigationIconContentColor = Color.White)
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                when (state.currentTab) {
                    MainTab.ROTINA -> RoutineScreen(state, vm::completeDay)
                    MainTab.METODOS -> TextListScreen("🧠 Como Estudar", state.methods)
                    MainTab.DESCANSO -> TextListScreen("😴 Plano de Descanso", state.restPlan)
                    MainTab.ESTUDANDO -> TextListScreen("📚 Estudando", listOf("Timer de estudo e aprendizagens em desenvolvimento no app nativo."))
                    MainTab.FLASHCARDS -> FlashcardsScreen(state)
                    MainTab.CALENDARIO -> CalendarScreen(state, vm)
                    MainTab.AVISOS -> AvisosScreen(state)
                    MainTab.RANKING -> RankingScreen(state)
                    MainTab.PERFIL -> ProfileScreen(state, vm)
                    MainTab.ADMIN -> AdminScreen(state, vm)
                    MainTab.MAIS -> MaisScreen()
                }
            }
        }
    }
}

fun openClassApp(context: android.content.Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://classapp.com.br")))
}
