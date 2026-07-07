package br.com.isa.rotinaestudos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.isa.rotinaestudos.auth.GoogleAuth
import br.com.isa.rotinaestudos.ui.IsaViewModel
import br.com.isa.rotinaestudos.ui.MainTab
import br.com.isa.rotinaestudos.ui.OverlaySheet
import br.com.isa.rotinaestudos.ui.components.IsaBootScreen
import br.com.isa.rotinaestudos.ui.components.IsaBottomSheet
import br.com.isa.rotinaestudos.ui.components.IsaGamificationHeader
import br.com.isa.rotinaestudos.ui.components.IsaSidebarHost
import br.com.isa.rotinaestudos.ui.components.overlayTitle
import br.com.isa.rotinaestudos.ui.screens.AvisoScreen
import br.com.isa.rotinaestudos.ui.screens.AvisosFullScreen
import br.com.isa.rotinaestudos.ui.screens.ChangePasswordSheet
import br.com.isa.rotinaestudos.ui.screens.FullAdminScreen
import br.com.isa.rotinaestudos.ui.screens.IntroScreen
import br.com.isa.rotinaestudos.ui.screens.IsaCelebrationPopup
import br.com.isa.rotinaestudos.ui.screens.WarningsPopup
import br.com.isa.rotinaestudos.ui.screens.AuthScreen
import br.com.isa.rotinaestudos.ui.screens.AvisosScreen
import br.com.isa.rotinaestudos.ui.screens.CalendarScreen
import br.com.isa.rotinaestudos.ui.screens.FlashcardsScreen
import br.com.isa.rotinaestudos.ui.screens.ProfileScreen
import br.com.isa.rotinaestudos.ui.screens.QuizScreen
import br.com.isa.rotinaestudos.ui.screens.RankingScreen
import br.com.isa.rotinaestudos.ui.screens.RoutineScreen
import br.com.isa.rotinaestudos.ui.screens.TextListScreen
import br.com.isa.rotinaestudos.ui.screens.DicasScreen
import br.com.isa.rotinaestudos.ui.screens.GradesScreen
import br.com.isa.rotinaestudos.ui.screens.MethodsScreen
import br.com.isa.rotinaestudos.ui.screens.MaisScreen
import br.com.isa.rotinaestudos.ui.screens.ShopScreen
import br.com.isa.rotinaestudos.ui.screens.StudyingScreen
import br.com.isa.rotinaestudos.ui.screens.UserProfileFullScreen
import br.com.isa.rotinaestudos.ui.screens.ChatScreen
import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import br.com.isa.rotinaestudos.AppConstants
import br.com.isa.rotinaestudos.ui.components.IsaPrimaryButton
import br.com.isa.rotinaestudos.ui.components.IsaSectionCard
import br.com.isa.rotinaestudos.ui.theme.IsaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: IsaViewModel = viewModel()
            val state by vm.state.collectAsState()
            val systemDark = isSystemInDarkTheme()
            IsaTheme(darkTheme = state.darkTheme ?: systemDark, shopThemeId = state.shopThemeId) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    IsaApp(vm)
                }
            }
        }
    }
}

@Composable
fun IsaApp(vm: IsaViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.adminMessage, state.successMessage) {
        state.error?.let { snackbar.showSnackbar(it); vm.clearError() }
        state.adminMessage?.let { snackbar.showSnackbar(it); vm.clearAdminMessage() }
        state.successMessage?.let { snackbar.showSnackbar(it); vm.clearSuccessMessage() }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            state.loading && state.authUser == null && !state.isGuest -> IsaBootScreen()
            state.authUser == null && !state.isGuest -> AuthScreen(
                loading = state.loading,
                error = state.authError,
                onLogin = vm::login,
                onRegister = vm::register,
                onGoogleSignIn = vm::signInWithGoogle,
                onGoogleError = vm::reportAuthError,
                onGuest = vm::continueAsGuest
            )
            state.showAviso -> AvisoScreen(onContinue = vm::acceptAviso)
            state.showIntro -> IntroScreen(onStartQuiz = vm::startQuizFromIntro)
            state.showQuiz -> QuizScreen(state, vm)
            state.viewedUser != null -> UserProfileFullScreen(
                profile = state.viewedUser,
                isOwn = state.viewedUser?.uid == state.authUser?.uid,
                rankPosition = if (state.viewedUser?.uid == state.authUser?.uid) state.myRankPosition else null,
                onBack = vm::closeViewedUser
            )
            else -> MainShell(state, vm, snackbar)
        }
        IsaCelebrationPopup(state.celebrationPopup, vm::dismissPopup)
        WarningsPopup(
            warnings = if (state.showWarningsPopup) state.profile?.warnings ?: emptyList() else emptyList(),
            onDismiss = vm::dismissWarningsPopup
        )
        state.streakToast?.let { days ->
            Snackbar(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
            ) {
                Text("🔥 $days ${if (days == 1) "dia" else "dias"} seguidos!")
            }
        }
    }
}

@Composable
private fun MainShell(
    state: br.com.isa.rotinaestudos.ui.IsaUiState,
    vm: IsaViewModel,
    snackbar: SnackbarHostState
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(state.currentTab) {
        if (state.currentTab == MainTab.RANKING) {
            vm.forceRefreshRanking()
            vm.startRankingAutoRefresh()
        }
    }

    Box(Modifier.fillMaxSize()) {
        IsaSidebarHost(
            selected = state.currentTab,
            isAdmin = state.isAdmin,
            onSelect = vm::selectTab,
            onClassApp = { openClassApp(context) },
            onAdmin = { vm.openOverlay(OverlaySheet.ADMIN) },
            drawerState = drawerState,
            header = { onMenuClick ->
                IsaGamificationHeader(
                    title = state.profile?.routineName?.ifBlank { "Sua Rotina Personalizada" } ?: "Sua Rotina Personalizada",
                    subtitle = state.routineSubtitle,
                    userName = state.profile?.name ?: "Aluno",
                    coins = state.profile?.coins ?: 0,
                    streak = state.profile?.streak ?: 0,
                    onSettingsClick = { vm.openOverlay(OverlaySheet.SETTINGS) },
                    onMenuClick = onMenuClick
                )
            },
            body = {
                AnimatedContent(
                    targetState = state.currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabContent",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        MainTab.ROTINA -> RoutineScreen(state, vm)
                        MainTab.METODOS -> MethodsScreen(state.methods, state.recommendations)
                        MainTab.DESCANSO -> TextListScreen("😴 Plano de Descanso", state.restPlan, "Plano de descanso personalizado após o quiz.")
                        MainTab.ESTUDANDO -> StudyingScreen(state, vm)
                        MainTab.FLASHCARDS -> FlashcardsScreen(state, vm)
                        MainTab.CALENDARIO -> CalendarScreen(state, vm)
                        MainTab.AVISOS -> AvisosFullScreen(state, vm)
                        MainTab.PERFIL -> ProfileScreen(state, vm)
                        MainTab.RANKING -> RankingScreen(state, state.authUser?.uid, vm)
                        MainTab.MAIS -> MaisScreen(state, vm, vm::openOverlay)
                    }
                }
            }
        )
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }

    when (state.activeOverlay) {
        OverlaySheet.CALENDARIO -> IsaBottomSheet(overlayTitle(OverlaySheet.CALENDARIO), vm::closeOverlay) {
            CalendarScreen(state, vm)
        }
        OverlaySheet.AVISOS -> IsaBottomSheet(overlayTitle(OverlaySheet.AVISOS), vm::closeOverlay) {
            AvisosFullScreen(state, vm)
        }
        OverlaySheet.DICAS -> IsaBottomSheet(overlayTitle(OverlaySheet.DICAS), vm::closeOverlay) {
            DicasScreen(onDone = vm::completeDica)
        }
        OverlaySheet.LOJA -> IsaBottomSheet(overlayTitle(OverlaySheet.LOJA), vm::closeOverlay) {
            ShopScreen(state, vm)
        }
        OverlaySheet.NOTAS -> IsaBottomSheet(overlayTitle(OverlaySheet.NOTAS), vm::closeOverlay) {
            GradesScreen(state, vm)
        }
        OverlaySheet.TIMER -> IsaBottomSheet(overlayTitle(OverlaySheet.TIMER), vm::closeOverlay) {
            StudyingScreen(state, vm)
        }
        OverlaySheet.SETTINGS -> IsaBottomSheet(overlayTitle(OverlaySheet.SETTINGS), vm::closeOverlay) {
            SettingsSheet(state, vm, onClassApp = { openClassApp(context) })
        }
        OverlaySheet.ADMIN -> IsaBottomSheet(overlayTitle(OverlaySheet.ADMIN), vm::closeOverlay) {
            FullAdminScreen(state, vm)
        }
        OverlaySheet.FLASHCARDS -> IsaBottomSheet(overlayTitle(OverlaySheet.FLASHCARDS), vm::closeOverlay) {
            FlashcardsScreen(state, vm)
        }
        OverlaySheet.CHAT -> IsaBottomSheet(overlayTitle(OverlaySheet.CHAT), vm::closeOverlay) {
            ChatScreen(state, vm)
        }
        OverlaySheet.NONE -> {}
    }
}

@Composable
private fun SettingsSheet(
    state: br.com.isa.rotinaestudos.ui.IsaUiState,
    vm: IsaViewModel,
    onClassApp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val systemDark = isSystemInDarkTheme()
    var showPassword by remember { mutableStateOf(false) }
    var series by remember(state.profile?.userSeries) { mutableStateOf(state.profile?.userSeries ?: "") }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 4.dp)) {
        if (state.isGuest) {
            Text("👤 Modo visitante — dados salvos só neste aparelho", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }
        RowSetting("Modo escuro", state.darkTheme ?: systemDark) { vm.setDarkTheme(it) }
        IsaSectionCard("🎓 Série escolar") {
            AppConstants.SERIES_OPTIONS.forEach { opt ->
                Row(
                    Modifier.fillMaxWidth().clickable { series = opt }.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = series == opt, onClick = { series = opt })
                    Text(opt, fontSize = 14.sp)
                }
            }
            IsaPrimaryButton(text = "Salvar série", onClick = { vm.updateSeries(series) })
        }
        if (!state.isGuest) {
            TextButton(onClick = { showPassword = true }, modifier = Modifier.padding(vertical = 4.dp)) {
                Text("🔑 Alterar senha")
            }
        }
        TextButton(onClick = { vm.resetRoutine() }, modifier = Modifier.padding(vertical = 4.dp)) {
            Text("🔄 Refazer rotina")
        }
        TextButton(onClick = onClassApp, modifier = Modifier.padding(vertical = 4.dp)) {
            Text("📱 Abrir ClassApp")
        }
        if (state.isAdmin) {
            TextButton(onClick = { vm.openOverlay(OverlaySheet.ADMIN) }, modifier = Modifier.padding(vertical = 4.dp)) {
                Text("🛡️ Painel Admin")
            }
        }
        TextButton(onClick = {
            scope.launch {
                GoogleAuth.signOut(context)
                vm.logout()
            }
        }, modifier = Modifier.padding(vertical = 4.dp)) {
            Text("Sair da conta")
        }
    }
    if (showPassword) {
        IsaBottomSheet("🔑 Alterar senha", { showPassword = false }) {
            ChangePasswordSheet { msg ->
                vm.notifySuccess(msg)
                showPassword = false
            }
        }
    }
}

@Composable
private fun RowSetting(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

fun openClassApp(context: android.content.Context) {
    val pkg = AppConstants.CLASSAPP_PACKAGE
    val launch = context.packageManager.getLaunchIntentForPackage(pkg)
    if (launch != null) {
        context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } else {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg&hl=pt_BR")
            )
        )
    }
}
