package br.com.isa.rotinaestudos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.isa.rotinaestudos.AppConstants
import br.com.isa.rotinaestudos.data.model.Announcement
import br.com.isa.rotinaestudos.data.model.CalendarEvent
import br.com.isa.rotinaestudos.data.model.QuizAnswer
import br.com.isa.rotinaestudos.data.model.ScheduleBlock
import br.com.isa.rotinaestudos.data.model.SubjectDifficulty
import br.com.isa.rotinaestudos.data.model.TimeSlot
import br.com.isa.rotinaestudos.data.model.UserProfile
import br.com.isa.rotinaestudos.data.repository.ContentRepository
import br.com.isa.rotinaestudos.data.repository.UserRepository
import br.com.isa.rotinaestudos.data.model.ExamTimelineItem
import br.com.isa.rotinaestudos.data.model.Flashcard
import br.com.isa.rotinaestudos.data.model.FlashcardSet
import br.com.isa.rotinaestudos.data.model.GradesConfig
import br.com.isa.rotinaestudos.data.model.SubjectGrades
import br.com.isa.rotinaestudos.domain.DicasData
import br.com.isa.rotinaestudos.domain.GradesHelper
import br.com.isa.rotinaestudos.domain.RevisionEntry
import br.com.isa.rotinaestudos.domain.ShopCatalog
import br.com.isa.rotinaestudos.domain.SpacedRevisions
import br.com.isa.rotinaestudos.domain.ScheduleGenerator
import br.com.isa.rotinaestudos.domain.SchoolCalendar2026
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import br.com.isa.rotinaestudos.domain.QuizData
import br.com.isa.rotinaestudos.data.local.IsaPreferences
import br.com.isa.rotinaestudos.ui.screens.IsaPopupData
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.launch

enum class MainTab(val label: String, val icon: String) {
    ROTINA("Rotina", "📅"),
    METODOS("Como Estudar", "🧠"),
    REVISOES("Revisões", "🔁"),
    DESCANSO("Descanso", "😴"),
    PERFIL("Perfil", "👤"),
    RANKING("Ranking", "🏆")
}

enum class OverlaySheet {
    NONE, CALENDARIO, AVISOS, LOJA, DICAS, NOTAS, TIMER, SETTINGS, ADMIN, FLASHCARDS
}

data class IsaUiState(
    val loading: Boolean = true,
    val authUser: FirebaseUser? = null,
    val profile: UserProfile? = null,
    val error: String? = null,
    val authError: String? = null,
    val currentTab: MainTab = MainTab.ROTINA,
    val showQuiz: Boolean = false,
    val quizIndex: Int = 0,
    val quizAnswers: List<QuizAnswer> = emptyList(),
    val quizHorarios: List<TimeSlot> = listOf(TimeSlot()),
    val quizMaterias: List<SubjectDifficulty> = listOf(SubjectDifficulty()),
    val selectedQuizOption: Int? = null,
    val generating: Boolean = false,
    val methods: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val revisions: List<RevisionEntry> = emptyList(),
    val restPlan: List<String> = emptyList(),
    val routineSubtitle: String = "",
    val announcements: List<Announcement> = emptyList(),
    val ranking: List<UserProfile> = emptyList(),
    val adminEvents: List<CalendarEvent> = emptyList(),
    val calendarOverrides: Map<String, Map<String, Any?>> = emptyMap(),
    val adminCalendarEvents: Map<String, CalendarEvent> = emptyMap(),
    val calMonth: LocalDate = LocalDate.now(),
    val calSelectedDay: String? = null,
    val calViewMonths: Boolean = false,
    val calNoteDraft: String = "",
    val studyActive: List<UserProfile> = emptyList(),
    val adminMessage: String? = null,
    val isAdmin: Boolean = false,
    val activeOverlay: OverlaySheet = OverlaySheet.NONE,
    val darkTheme: Boolean? = null,
    val completeDayAnim: Boolean = false,
    val shopFilter: String = "all",
    val successMessage: String? = null,
    val studyTimerRunning: Boolean = false,
    val studyTimerSeconds: Int = 0,
    val studyTimerSessionSeconds: Int = 0,
    val isGuest: Boolean = false,
    val showAviso: Boolean = false,
    val showIntro: Boolean = false,
    val celebrationPopup: IsaPopupData? = null,
    val streakToast: Int? = null,
    val showWarningsPopup: Boolean = false,
    val reorganizeMode: Boolean = false,
    val viewedUser: UserProfile? = null,
    val rankingLastUpdate: Long = 0L,
    val subjectBars: List<Pair<String, Int>> = emptyList(),
    val shopThemeId: String? = null
)

class IsaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userRepo = UserRepository(db)
    private val contentRepo = ContentRepository(db)

    private val _state = MutableStateFlow(IsaUiState())
    val state: StateFlow<IsaUiState> = _state.asStateFlow()

    private var lastRankingRefresh = 0L
    private var lastAnnouncementsRefresh = 0L
    private var timerJob: Job? = null
    private var warningsShownSession = false

    init {
        val savedDark = IsaPreferences.darkTheme
        if (savedDark != null) _state.update { it.copy(darkTheme = savedDark) }
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _state.update { it.copy(authUser = user, isAdmin = AppConstants.isAdminEmail(user?.email)) }
            if (user != null) {
                IsaPreferences.isGuest = false
                loadUser(user)
            } else if (IsaPreferences.isGuest) {
                loadGuest()
            } else {
                _state.update { it.copy(loading = false, profile = null, isGuest = false) }
            }
        }
    }

    private fun loadUser(user: FirebaseUser) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                var profile = userRepo.loadUser(user.uid)
                if (profile == null) {
                    profile = UserProfile(
                        uid = user.uid,
                        name = user.displayName ?: user.email?.substringBefore('@') ?: "Aluno",
                        email = user.email ?: "",
                        photoURL = user.photoUrl?.toString() ?: "",
                        joinedAt = java.time.Instant.now().toString()
                    )
                    userRepo.saveUser(profile)
                } else if (user.photoUrl != null && profile.photoURL.isBlank()) {
                    profile = profile.copy(photoURL = user.photoUrl.toString())
                    userRepo.saveUser(profile)
                }
                if (profile.banned) {
                    auth.signOut()
                    _state.update { it.copy(loading = false, authError = "Conta banida: ${profile.banReason}") }
                    return@launch
                }
                profile = applyStreakReset(profile)
                val themeId = profile.equippedItems["theme"] as? String
                val showWarnings = profile.warnings.isNotEmpty() && !warningsShownSession
                if (showWarnings) warningsShownSession = true
                val announcements = contentRepo.loadAnnouncements()
                val adminEvents = contentRepo.loadCalendarEvents()
                val overrides = contentRepo.loadCalendarOverrides()
                val ranking = userRepo.loadRanking()
                _state.update {
                    it.copy(
                        loading = false,
                        profile = profile,
                        announcements = announcements,
                        adminCalendarEvents = adminEvents,
                        calendarOverrides = overrides,
                        ranking = ranking,
                        rankingLastUpdate = System.currentTimeMillis(),
                        showQuiz = false,
                        showAviso = !profile.hasRoutine,
                        showIntro = false,
                        quizAnswers = profile.quizAnswers,
                        shopThemeId = themeId,
                        showWarningsPopup = showWarnings,
                        calSelectedDay = todayKey()
                    )
                }
                if (profile.hasRoutine && profile.savedSchedule != null) {
                    regenerateTexts(profile)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _state.update { it.copy(authError = null, loading = true) }
            try {
                auth.signInWithEmailAndPassword(email.trim(), pass).await()
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, authError = authMessage(e.message)) }
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _state.update { it.copy(authError = null, loading = true) }
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val profile = UserProfile(
                    uid = result.user!!.uid,
                    name = name.trim().ifBlank { email.substringBefore('@') },
                    email = email.trim(),
                    joinedAt = java.time.Instant.now().toString()
                )
                userRepo.saveUser(profile)
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, authError = authMessage(e.message)) }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(authError = null, loading = true) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, authError = googleAuthMessage(e.message)) }
            }
        }
    }

    fun reportAuthError(message: String) {
        _state.update { it.copy(loading = false, authError = message) }
    }

    fun logout() {
        auth.signOut()
        IsaPreferences.clearGuest()
        IsaPreferences.isGuest = false
        warningsShownSession = false
        val dark = IsaPreferences.darkTheme
        _state.update { IsaUiState(loading = false, darkTheme = dark) }
    }

    fun notifySuccess(message: String) {
        _state.update { it.copy(successMessage = message) }
    }

    fun selectTab(tab: MainTab) {
        _state.update { it.copy(currentTab = tab) }
    }

    fun openOverlay(sheet: OverlaySheet) {
        _state.update { it.copy(activeOverlay = sheet) }
        when (sheet) {
            OverlaySheet.AVISOS -> refreshAnnouncements()
            OverlaySheet.CALENDARIO -> refreshCalendarData()
            OverlaySheet.ADMIN -> loadAdminEvents()
            else -> {}
        }
    }

    fun closeOverlay() {
        _state.update { it.copy(activeOverlay = OverlaySheet.NONE) }
    }

    fun toggleDarkTheme() {
        val current = _state.value.darkTheme
        setDarkTheme(!(current ?: true))
    }

    fun setDarkTheme(enabled: Boolean) {
        IsaPreferences.darkTheme = enabled
        _state.update { it.copy(darkTheme = enabled) }
    }

    fun continueAsGuest() {
        IsaPreferences.isGuest = true
        IsaPreferences.guestName = "Visitante"
        loadGuest()
    }

    private fun loadGuest() {
        val profile = UserProfile(
            uid = "guest",
            name = IsaPreferences.guestName,
            email = "",
            coins = IsaPreferences.getGuestInt("guest_coins"),
            streak = IsaPreferences.getGuestInt("guest_streak"),
            lastComplete = IsaPreferences.getGuestString("guest_last_complete"),
            hasRoutine = IsaPreferences.guestHasRoutine,
            joinedAt = java.time.Instant.now().toString()
        )
        _state.update {
            it.copy(
                loading = false,
                isGuest = true,
                profile = profile,
                showQuiz = false,
                showAviso = !profile.hasRoutine,
                showIntro = false,
                authUser = null
            )
        }
    }

    fun acceptAviso() {
        _state.update { it.copy(showAviso = false, showIntro = true) }
    }

    fun startQuizFromIntro() {
        _state.update { it.copy(showIntro = false, showQuiz = true) }
    }

    fun resetRoutine() {
        val p = _state.value.profile ?: return
        val cleared = p.copy(
            hasRoutine = false,
            quizAnswers = emptyList(),
            savedSchedule = null,
            studyMotivation = ""
        )
        saveProfile(cleared)
        IsaPreferences.guestHasRoutine = false
        _state.update {
            it.copy(
                profile = cleared,
                showAviso = true,
                showIntro = false,
                showQuiz = false,
                methods = emptyList(),
                recommendations = emptyList(),
                revisions = emptyList(),
                restPlan = emptyList(),
                subjectBars = emptyList()
            )
        }
    }

    fun dismissPopup() {
        _state.update { it.copy(celebrationPopup = null) }
    }

    fun dismissWarningsPopup() {
        _state.update { it.copy(showWarningsPopup = false) }
    }

    fun canChangeName(): Boolean {
        val p = _state.value.profile ?: return false
        if (p.nameLastChanged.isBlank()) return true
        return try {
            val next = java.time.Instant.parse(p.nameLastChanged).atZone(java.time.ZoneId.systemDefault()).toLocalDate().plusDays(14)
            !LocalDate.now().isBefore(next)
        } catch (_: Exception) { true }
    }

    fun saveDisplayName(name: String) {
        val p = _state.value.profile ?: return
        if (!canChangeName()) {
            _state.update { it.copy(error = "Aguarde 14 dias entre alterações de nome.") }
            return
        }
        if (name.trim().length < 2) {
            _state.update { it.copy(error = "Nome deve ter ao menos 2 caracteres.") }
            return
        }
        val updated = p.copy(name = name.trim(), nameLastChanged = java.time.Instant.now().toString())
        viewModelScope.launch {
            try {
                auth.currentUser?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                )?.await()
                saveProfile(updated)
                _state.update { it.copy(successMessage = "Nome atualizado!") }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleReorganize() {
        val mode = !_state.value.reorganizeMode
        _state.update { it.copy(reorganizeMode = mode) }
        if (mode) {
            _state.update {
                it.copy(celebrationPopup = IsaPopupData("🔀", "Modo reorganizar", "Toque em dois blocos para trocar de posição. Descansos não movem."))
            }
        }
    }

    fun swapScheduleBlocks(dayA: String, idxA: Int, dayB: String, idxB: Int) {
        val p = _state.value.profile ?: return
        val schedule = p.savedSchedule?.toMutableMap() ?: return
        val listA = schedule[dayA]?.toMutableList() ?: return
        val listB = if (dayA == dayB) listA else schedule[dayB]?.toMutableList() ?: return
        if (idxA !in listA.indices || idxB !in listB.indices) return
        val a = listA[idxA]
        val b = listB[idxB]
        if (a.isBreak || b.isBreak) return
        listA[idxA] = b
        listB[idxB] = a
        schedule[dayA] = listA
        if (dayA != dayB) schedule[dayB] = listB
        saveProfile(p.copy(savedSchedule = schedule))
    }

    fun viewUserProfile(uid: String) {
        if (_state.value.isGuest) return
        viewModelScope.launch {
            try {
                val user = userRepo.loadUser(uid)
                _state.update { it.copy(viewedUser = user) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Não foi possível carregar o perfil.") }
            }
        }
    }

    fun closeViewedUser() {
        _state.update { it.copy(viewedUser = null) }
    }

    fun startRankingAutoRefresh() {
        viewModelScope.launch {
            while (_state.value.currentTab == MainTab.RANKING) {
                delay(60_000)
                forceRefreshRanking()
            }
        }
    }

    fun adminSaveUser(email: String, coins: Int?, streak: Int?) {
        viewModelScope.launch {
            try {
                val user = userRepo.findByEmail(email) ?: run {
                    _state.update { it.copy(adminMessage = "Usuário não encontrado.") }; return@launch
                }
                userRepo.adminUpdateCoinsStreak(user.uid, coins, streak)
                forceRefreshRanking()
                _state.update { it.copy(adminMessage = "Usuário atualizado!") }
            } catch (e: Exception) {
                _state.update { it.copy(adminMessage = "Erro: ${e.message}") }
            }
        }
    }

    fun adminFetchStatus(email: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepo.findByEmail(email)
                if (user == null) onResult("Usuário não encontrado.")
                else if (user.banned) onResult("🚫 Banido · ${user.banReason} · ${user.warnings.size} aviso(s)")
                else onResult("✅ Conta ativa · ${user.warnings.size} aviso(s)")
            } catch (_: Exception) { onResult("Erro ao consultar.") }
        }
    }

    fun adminBanUser(email: String, reason: String) {
        viewModelScope.launch {
            try {
                val user = userRepo.findByEmail(email) ?: run {
                    _state.update { it.copy(adminMessage = "Usuário não encontrado.") }; return@launch
                }
                val adminEmail = _state.value.profile?.email ?: ""
                if (user.email.equals(adminEmail, true)) {
                    _state.update { it.copy(adminMessage = "Você não pode banir a si mesmo.") }; return@launch
                }
                userRepo.adminBanUser(user.uid, reason, adminEmail)
                _state.update { it.copy(adminMessage = "Usuário banido.") }
            } catch (e: Exception) {
                _state.update { it.copy(adminMessage = "Erro: ${e.message}") }
            }
        }
    }

    fun adminUnbanUser(email: String) {
        viewModelScope.launch {
            try {
                val user = userRepo.findByEmail(email) ?: run {
                    _state.update { it.copy(adminMessage = "Usuário não encontrado.") }; return@launch
                }
                userRepo.adminUnbanUser(user.uid)
                _state.update { it.copy(adminMessage = "Usuário desbanido.") }
            } catch (e: Exception) {
                _state.update { it.copy(adminMessage = "Erro: ${e.message}") }
            }
        }
    }

    fun adminSendWarning(email: String, message: String) {
        viewModelScope.launch {
            try {
                val user = userRepo.findByEmail(email) ?: run {
                    _state.update { it.copy(adminMessage = "Usuário não encontrado.") }; return@launch
                }
                if (message.isBlank()) {
                    _state.update { it.copy(adminMessage = "Escreva a mensagem de aviso.") }; return@launch
                }
                val admin = _state.value.profile
                userRepo.adminSendWarning(user.uid, message, admin?.name ?: "Admin", admin?.email ?: "")
                _state.update { it.copy(adminMessage = "Aviso enviado.") }
            } catch (e: Exception) {
                _state.update { it.copy(adminMessage = "Erro: ${e.message}") }
            }
        }
    }

    fun adminSendAnnouncement(message: String) {
        viewModelScope.launch {
            try {
                if (message.isBlank()) return@launch
                contentRepo.addAnnouncement(message, _state.value.profile?.name ?: "Admin")
                refreshAnnouncements()
                lastAnnouncementsRefresh = 0L
                _state.update { it.copy(adminMessage = "Comunicado publicado!") }
            } catch (e: Exception) {
                _state.update { it.copy(adminMessage = "Erro: ${e.message}") }
            }
        }
    }

    private fun applyStreakReset(profile: UserProfile): UserProfile {
        val today = todayKey()
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (profile.lastComplete.isNotBlank() && profile.lastComplete != today && profile.lastComplete != yesterday && profile.streak > 0) {
            val updated = profile.copy(streak = 0)
            viewModelScope.launch { userRepo.saveUser(updated) }
            return updated
        }
        return profile
    }


    fun selectQuizOption(index: Int) {
        _state.update { it.copy(selectedQuizOption = index) }
    }

    fun nextQuizQuestion() {
        val s = _state.value
        val q = QuizData.QUESTIONS[s.quizIndex]
        val answers = s.quizAnswers.toMutableList()
        when (q.special) {
            "horarios" -> answers.add(QuizAnswer(q = s.quizIndex + 1, special = "horarios", horarios = s.quizHorarios.filter { it.ini.isNotBlank() }))
            "materias" -> answers.add(QuizAnswer(q = s.quizIndex + 1, special = "materias", materias = s.quizMaterias.filter { it.nome.isNotBlank() }))
            else -> {
                val idx = s.selectedQuizOption ?: return
                val letters = listOf("A", "B", "C", "D", "E")
                answers.add(QuizAnswer(q = s.quizIndex + 1, ans = letters[idx], txt = q.opts?.get(idx)))
            }
        }
        if (s.quizIndex < QuizData.TOTAL_Q - 1) {
            _state.update { it.copy(quizIndex = s.quizIndex + 1, quizAnswers = answers, selectedQuizOption = null) }
        } else {
            finishQuiz(answers)
        }
    }

    private fun finishQuiz(answers: List<QuizAnswer>) {
        val horarios = QuizData.getHorarios(answers)
        val materias = QuizData.getMaterias(answers).ifEmpty { listOf(SubjectDifficulty("Revisão Geral", 50)) }
        _state.update { it.copy(generating = true, quizAnswers = answers) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val result = ScheduleGenerator.generate(answers, horarios, materias)
            val motivacao = QuizData.getAnswerText(answers, 23)
            val profile = _state.value.profile?.copy(
                quizAnswers = answers,
                hasRoutine = true,
                savedSchedule = result.schedule,
                studyMotivation = motivacao
            ) ?: return@launch
            saveProfile(profile)
            IsaPreferences.guestHasRoutine = true
            _state.update {
                it.copy(
                    generating = false,
                    showQuiz = false,
                    showAviso = false,
                    showIntro = false,
                    profile = profile,
                    methods = result.methodsHtml,
                    recommendations = result.recommendations,
                    revisions = SpacedRevisions.generate(materias),
                    restPlan = result.restPlan,
                    routineSubtitle = result.subtitle,
                    subjectBars = result.subjectBars,
                    currentTab = MainTab.ROTINA,
                    celebrationPopup = IsaPopupData("🎉", "Rotina criada!", "Sua rotina personalizada está pronta. Bons estudos!")
                )
            }
        }
    }

    private fun regenerateTexts(profile: UserProfile) {
        val horarios = QuizData.getHorarios(profile.quizAnswers)
        val materias = QuizData.getMaterias(profile.quizAnswers).ifEmpty { listOf(SubjectDifficulty("Revisão Geral", 50)) }
        if (horarios.isEmpty() || profile.savedSchedule == null) return
        val result = ScheduleGenerator.generate(profile.quizAnswers, horarios, materias)
        _state.update {
            it.copy(
                methods = result.methodsHtml,
                recommendations = result.recommendations,
                revisions = SpacedRevisions.generate(materias),
                restPlan = result.restPlan,
                routineSubtitle = result.subtitle,
                subjectBars = result.subjectBars
            )
        }
    }

    fun completeDay() {
        val p = _state.value.profile ?: return
        val today = todayKey()
        if (p.lastComplete == today) {
            _state.update {
                it.copy(celebrationPopup = IsaPopupData("ℹ️", "Já concluído hoje!", "Você já marcou os estudos de hoje. Volte amanhã! 😊"))
            }
            return
        }
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val streak = if (p.lastComplete == yesterday) p.streak + 1 else 1
        val earned = maxOf(2, streak * 2)
        val mascot = addMascotXp(p, 12)
        val updated = mascot.copy(lastComplete = today, streak = streak, coins = p.coins + earned)
        _state.update {
            it.copy(
                completeDayAnim = true,
                streakToast = streak,
                celebrationPopup = IsaPopupData("🎉", "Parabéns!", "Você completou os estudos de hoje e ganhou 🪙 $earned moedas!")
            )
        }
        saveProfile(updated)
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            _state.update { it.copy(completeDayAnim = false, streakToast = null) }
        }
    }

    fun saveCalNote(note: String) {
        val day = _state.value.calSelectedDay ?: return
        val p = _state.value.profile ?: return
        val notes = p.calNotes.toMutableMap()
        if (note.isBlank()) notes.remove(day) else notes[day] = note
        saveProfile(p.copy(calNotes = notes))
    }

    fun selectCalDay(key: String) {
        val note = _state.value.profile?.calNotes?.get(key) ?: ""
        _state.update { it.copy(calSelectedDay = key, calNoteDraft = note) }
    }

    fun updateCalNoteDraft(text: String) {
        _state.update { it.copy(calNoteDraft = text) }
    }

    fun calNav(delta: Int) {
        val s = _state.value
        val newMonth = if (s.calViewMonths) s.calMonth.plusYears(delta.toLong())
        else s.calMonth.plusMonths(delta.toLong())
        val inView = s.calSelectedDay?.let { key ->
            val p = key.split("-")
            p.size == 3 && p[0].toIntOrNull() == newMonth.year && p[1].toIntOrNull() == newMonth.monthValue
        } ?: false
        _state.update {
            it.copy(
                calMonth = newMonth,
                calSelectedDay = if (inView) it.calSelectedDay else null,
                calNoteDraft = if (inView) it.calNoteDraft else ""
            )
        }
    }

    fun toggleCalMonthView() {
        _state.update { it.copy(calViewMonths = !it.calViewMonths) }
    }

    fun selectCalMonth(monthIndex: Int) {
        val m = _state.value.calMonth.withMonth(monthIndex + 1)
        _state.update { it.copy(calMonth = m, calViewMonths = false, calSelectedDay = null, calNoteDraft = "") }
    }

    fun updateSeries(series: String) {
        val p = _state.value.profile ?: return
        saveProfile(p.copy(userSeries = series))
    }

    fun updateBio(bio: String) {
        val p = _state.value.profile ?: return
        saveProfile(p.copy(bio = bio))
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                if (_state.value.isGuest || profile.uid == "guest") {
                    IsaPreferences.guestHasRoutine = profile.hasRoutine
                    IsaPreferences.setGuestInt("guest_coins", profile.coins)
                    IsaPreferences.setGuestInt("guest_streak", profile.streak)
                    IsaPreferences.setGuestString("guest_last_complete", profile.lastComplete)
                    IsaPreferences.guestName = profile.name
                    _state.update { it.copy(profile = profile) }
                } else {
                    userRepo.saveUser(profile)
                    _state.update {
                        it.copy(
                            profile = profile,
                            shopThemeId = profile.equippedItems["theme"] as? String
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun refreshAnnouncements() {
        val now = System.currentTimeMillis()
        if (now - lastAnnouncementsRefresh < 60_000) return
        lastAnnouncementsRefresh = now
        viewModelScope.launch {
            _state.update { it.copy(announcements = contentRepo.loadAnnouncements()) }
        }
    }

    fun refreshRanking() {
        val now = System.currentTimeMillis()
        if (now - lastRankingRefresh < 60_000) return
        lastRankingRefresh = now
        viewModelScope.launch {
            _state.update { it.copy(ranking = userRepo.loadRanking(), rankingLastUpdate = System.currentTimeMillis()) }
        }
    }

    fun forceRefreshRanking() {
        lastRankingRefresh = 0L
        refreshRanking()
    }

    fun refreshCalendarData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    adminCalendarEvents = contentRepo.loadCalendarEvents(),
                    calendarOverrides = contentRepo.loadCalendarOverrides()
                )
            }
        }
    }

    fun loadAdminEvents() {
        viewModelScope.launch {
            val events = contentRepo.loadAdminCalendarEvents()
            _state.update { it.copy(adminEvents = events) }
        }
    }

    fun saveAdminEvent(date: String, title: String, message: String, type: String, editId: String?, isSchool: Boolean) {
        viewModelScope.launch {
            try {
                val adminName = _state.value.profile?.name ?: "Admin"
                if (isSchool) {
                    contentRepo.saveCalendarOverride(date, type, title, message, adminName)
                } else if (editId != null) {
                    contentRepo.updateCalendarEvent(editId, date, title, message, type)
                } else {
                    contentRepo.addCalendarEvent(date, title, message, type, adminName)
                }
                refreshCalendarData()
                loadAdminEvents()
                _state.update { it.copy(adminMessage = "Evento salvo!") }
            } catch (e: Exception) {
                _state.update { it.copy(adminMessage = "Erro: ${e.message}") }
            }
        }
    }

    fun eventsForDay(key: String): List<CalendarEventDataUi> {
        val s = _state.value
        val out = mutableListOf<CalendarEventDataUi>()
        if (s.profile?.userSeries == "1º EM" || s.profile?.userSeries == "2º EM") {
            SchoolCalendar2026.getEvent(key, s.calendarOverrides)?.let {
                out.add(CalendarEventDataUi(it.type, it.title, it.message, "series"))
            }
        }
        s.adminCalendarEvents[key]?.let {
            out.add(CalendarEventDataUi(it.type, it.title, it.message, "admin"))
        }
        return out
    }

    fun eventTypeForDay(key: String): String? {
        val admin = _state.value.adminCalendarEvents[key]
        if (admin != null && admin.type.isNotBlank()) return admin.type
        if (_state.value.profile?.userSeries == "1º EM" || _state.value.profile?.userSeries == "2º EM") {
            return SchoolCalendar2026.getEvent(key, _state.value.calendarOverrides)?.type
        }
        return null
    }

    fun addHorario() {
        _state.update { it.copy(quizHorarios = it.quizHorarios + TimeSlot()) }
    }

    fun updateHorario(index: Int, slot: TimeSlot) {
        val list = _state.value.quizHorarios.toMutableList()
        if (index in list.indices) {
            list[index] = slot
            _state.update { it.copy(quizHorarios = list) }
        }
    }

    fun addMateria() {
        _state.update { it.copy(quizMaterias = it.quizMaterias + SubjectDifficulty()) }
    }

    fun updateMateria(index: Int, mat: SubjectDifficulty) {
        val list = _state.value.quizMaterias.toMutableList()
        if (index in list.indices) {
            list[index] = mat
            _state.update { it.copy(quizMaterias = list) }
        }
    }

    fun applyTemplate(templateKey: String) {
        val subjects = QuizData.TEMPLATES[templateKey] ?: return
        _state.update {
            it.copy(quizMaterias = subjects.map { name -> SubjectDifficulty(name, 50) })
        }
    }

    private fun authMessage(msg: String?) = when {
        msg?.contains("password", true) == true -> "Senha incorreta."
        msg?.contains("email", true) == true -> "E-mail inválido ou não encontrado."
        msg?.contains("network", true) == true -> "Sem conexão."
        else -> msg ?: "Erro ao autenticar."
    }

    private fun googleAuthMessage(msg: String?) = when {
        msg?.contains("network", true) == true -> "Sem conexão."
        msg?.contains("account-exists-with-different-credential", true) == true ->
            "Este e-mail já está cadastrado com senha. Use e-mail e senha para entrar."
        msg?.contains("invalid-credential", true) == true ->
            "Credencial Google inválida. Verifique SHA-1 e google-services.json no Firebase."
        msg?.contains("user-disabled", true) == true -> "Conta desativada."
        msg != null -> "Firebase: $msg"
        else -> "Erro ao entrar com Google."
    }

    fun setShopFilter(filter: String) {
        _state.update { it.copy(shopFilter = filter) }
    }

    fun buyShopItem(itemId: String) {
        val p = _state.value.profile ?: return
        val item = ShopCatalog.byId[itemId] ?: return
        if (p.ownedItems.contains(itemId)) {
            _state.update { it.copy(successMessage = "Você já possui este item!") }
            return
        }
        if (p.coins < item.price) {
            _state.update { it.copy(error = "Moedas insuficientes. Precisa de ${item.price}, tem ${p.coins}.") }
            return
        }
        saveProfile(p.copy(coins = p.coins - item.price, ownedItems = p.ownedItems + itemId))
        _state.update { it.copy(successMessage = "🎉 Você adquiriu ${item.name}!") }
    }

    fun toggleEquipItem(itemId: String) {
        val p = _state.value.profile ?: return
        if (!p.ownedItems.contains(itemId)) return
        val item = ShopCatalog.byId[itemId] ?: return
        val equipped = p.equippedItems.toMutableMap()
        if (isEquipped(p, itemId)) {
            unequipItem(equipped, item)
        } else {
            if (!equipItem(equipped, item)) return
        }
        val newProfile = p.copy(equippedItems = equipped)
        val themeId = equipped["theme"] as? String
        saveProfile(newProfile)
        if (item.slot == "theme") {
            _state.update { it.copy(shopThemeId = themeId) }
        }
    }

    fun isEquipped(profile: UserProfile, itemId: String): Boolean {
        val item = ShopCatalog.byId[itemId] ?: return false
        return when (item.slot) {
            "badges", "accessories" -> {
                val list = profile.equippedItems[item.slot] as? List<*> ?: emptyList<Any>()
                list.contains(itemId)
            }
            else -> profile.equippedItems[item.slot] == itemId
        }
    }

    private fun equipItem(equipped: MutableMap<String, Any>, item: br.com.isa.rotinaestudos.domain.ShopItem): Boolean {
        when (item.slot) {
            "badges", "accessories" -> {
                val list = (equipped[item.slot] as? List<String>)?.toMutableList() ?: mutableListOf()
                if (list.size >= 4 && !list.contains(item.id)) {
                    _state.update { it.copy(error = "Equipe no máximo 4 itens deste tipo.") }
                    return false
                }
                if (!list.contains(item.id)) list.add(item.id)
                equipped[item.slot] = list
            }
            else -> equipped[item.slot] = item.id
        }
        return true
    }

    private fun unequipItem(equipped: MutableMap<String, Any>, item: br.com.isa.rotinaestudos.domain.ShopItem) {
        when (item.slot) {
            "badges", "accessories" -> {
                val list = (equipped[item.slot] as? List<String>)?.toMutableList() ?: mutableListOf()
                list.remove(item.id)
                equipped[item.slot] = list
            }
            else -> equipped.remove(item.slot)
        }
    }

    fun completeDica() {
        val p = _state.value.profile ?: return
        val earned = DicasData.coinsFor(maxOf(p.streak, 1))
        saveProfile(p.copy(coins = p.coins + earned))
        _state.update { it.copy(successMessage = "💡 Ótimo hábito! +$earned moedas") }
    }

    fun getNotasSubjects(): List<String> {
        val fromQuiz = QuizData.getMaterias(_state.value.profile?.quizAnswers ?: emptyList())
            .map { it.nome.trim() }.filter { it.isNotEmpty() }
        if (fromQuiz.isNotEmpty()) return fromQuiz
        return _state.value.profile?.gradesConfig?.subjects ?: emptyList()
    }

    fun finishGradesSetup(style: String, minAvg: Double, examNames: List<String>) {
        val subjects = getNotasSubjects()
        if (subjects.isEmpty()) {
            _state.update { it.copy(error = "Complete o quiz primeiro para obter matérias.") }
            return
        }
        val timeline = GradesHelper.buildExamTimeline(examNames, style).map {
            ExamTimelineItem(id = it["id"] ?: "", type = it["type"] ?: "", name = it["name"] ?: "")
        }
        val cfg = GradesConfig(
            setupDone = true,
            style = style,
            minAvg = minAvg,
            examNames = examNames,
            timeline = timeline,
            subjects = subjects
        )
        val p = _state.value.profile ?: return
        val data = subjects.associateWith { p.gradesData[it] ?: SubjectGrades() }
        saveProfile(p.copy(gradesConfig = cfg, gradesData = data))
        _state.update { it.copy(successMessage = "📊 Notas configuradas!") }
    }

    fun setNotaGrade(subject: String, id: String, value: String, type: String) {
        val p = _state.value.profile ?: return
        val current = p.gradesData[subject] ?: SubjectGrades()
        val updated = when (type) {
            "exam" -> current.copy(exams = current.exams + (id to value))
            "recovery" -> current.copy(recoveries = current.recoveries + (id to value))
            "pf" -> current.copy(provaFinal = value)
            else -> current
        }
        saveProfile(p.copy(gradesData = p.gradesData + (subject to updated)))
    }

    fun addFlashcardSet(subject: String, question: String, answer: String) {
        val p = _state.value.profile ?: return
        if (subject.isBlank() || question.isBlank()) return
        val existing = p.flashcardSets.find { it.subject.equals(subject, true) }
        val newCard = Flashcard(q = question, a = answer)
        val sets = if (existing != null) {
            p.flashcardSets.map {
                if (it.id == existing.id) it.copy(cards = it.cards + newCard) else it
            }
        } else {
            p.flashcardSets + FlashcardSet(
                id = System.currentTimeMillis().toString(),
                subject = subject.trim(),
                cards = listOf(newCard)
            )
        }
        saveProfile(p.copy(flashcardSets = sets))
        _state.update { it.copy(successMessage = "🃏 Flashcard adicionado!") }
    }

    fun deleteFlashcardSet(setId: String) {
        val p = _state.value.profile ?: return
        saveProfile(p.copy(flashcardSets = p.flashcardSets.filter { it.id != setId }))
    }

    fun startStudyTimer() {
        if (_state.value.studyTimerRunning) return
        _state.update { it.copy(studyTimerRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.studyTimerRunning) {
                delay(1000)
                _state.update {
                    it.copy(
                        studyTimerSeconds = it.studyTimerSeconds + 1,
                        studyTimerSessionSeconds = it.studyTimerSessionSeconds + 1
                    )
                }
            }
        }
    }

    fun pauseStudyTimer() {
        _state.update { it.copy(studyTimerRunning = false) }
        timerJob?.cancel()
    }

    fun resetStudyTimerSession() {
        pauseStudyTimer()
        _state.update { it.copy(studyTimerSessionSeconds = 0) }
    }

    fun saveStudyTimer() {
        val p = _state.value.profile ?: return
        val session = _state.value.studyTimerSessionSeconds
        if (session <= 0) return
        pauseStudyTimer()
        saveProfile(p.copy(totalStudySeconds = p.totalStudySeconds + session))
        _state.update {
            it.copy(
                studyTimerSessionSeconds = 0,
                successMessage = "⏱️ ${formatSeconds(session)} registrados!"
            )
        }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    fun clearAdminMessage() {
        _state.update { it.copy(adminMessage = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun addMascotXp(profile: UserProfile, amount: Int): UserProfile {
        val mascotId = profile.equippedItems["mascot"] as? String ?: return profile
        if (mascotId.isBlank()) return profile
        val data = profile.mascotData.toMutableMap()
        var xp = (data["xp"] as? Number)?.toInt() ?: 0
        var level = (data["level"] as? Number)?.toInt() ?: 1
        xp += amount
        while (level < 10 && xp >= mascotXpForLevel(level + 1)) level++
        data["xp"] = xp
        data["level"] = level
        return profile.copy(mascotData = data)
    }

    private fun mascotXpForLevel(lv: Int): Int =
        if (lv <= 1) 0 else ((lv - 1) * 100) + ((lv - 1) * (lv - 1) * 25)

    private fun formatSeconds(sec: Int): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) "${h}h ${m}min" else if (m > 0) "${m}min ${s}s" else "${s}s"
    }

    companion object {
        fun todayKey() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

data class CalendarEventDataUi(val type: String, val title: String, val message: String, val source: String)
