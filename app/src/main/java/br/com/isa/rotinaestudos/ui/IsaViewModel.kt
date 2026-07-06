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
import br.com.isa.rotinaestudos.domain.QuizData
import br.com.isa.rotinaestudos.domain.ScheduleGenerator
import br.com.isa.rotinaestudos.domain.SchoolCalendar2026
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class MainTab(val label: String, val icon: String) {
    ROTINA("Rotina", "📅"),
    METODOS("Como Estudar", "🧠"),
    DESCANSO("Descanso", "😴"),
    ESTUDANDO("Estudando", "📚"),
    FLASHCARDS("Flashcards", "🃏"),
    CALENDARIO("Calendário", "📅"),
    AVISOS("Avisos", "📢"),
    RANKING("Ranking", "🏆"),
    PERFIL("Perfil", "👤"),
    ADMIN("Admin", "🛡️"),
    MAIS("Mais", "⋯")
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
    val isAdmin: Boolean = false
)

class IsaViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userRepo = UserRepository(db)
    private val contentRepo = ContentRepository(db)

    private val _state = MutableStateFlow(IsaUiState())
    val state: StateFlow<IsaUiState> = _state.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _state.update { it.copy(authUser = user, isAdmin = AppConstants.isAdminEmail(user?.email)) }
            if (user != null) loadUser(user)
            else _state.update { it.copy(loading = false, profile = null) }
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
                        joinedAt = java.time.Instant.now().toString()
                    )
                    userRepo.saveUser(profile)
                }
                if (profile.banned) {
                    auth.signOut()
                    _state.update { it.copy(loading = false, authError = "Conta banida: ${profile.banReason}") }
                    return@launch
                }
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
                        showQuiz = !profile.hasRoutine,
                        quizAnswers = profile.quizAnswers,
                        methods = emptyList(),
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

    fun logout() {
        auth.signOut()
        _state.update { IsaUiState(loading = false) }
    }

    fun selectTab(tab: MainTab) {
        _state.update { it.copy(currentTab = tab) }
        if (tab == MainTab.AVISOS) refreshAnnouncements()
        if (tab == MainTab.RANKING) refreshRanking()
        if (tab == MainTab.CALENDARIO) refreshCalendarData()
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
            val profile = _state.value.profile?.copy(
                quizAnswers = answers,
                hasRoutine = true,
                savedSchedule = result.schedule
            ) ?: return@launch
            userRepo.saveUser(profile)
            _state.update {
                it.copy(
                    generating = false,
                    showQuiz = false,
                    profile = profile,
                    methods = result.methodsHtml,
                    recommendations = result.recommendations,
                    restPlan = result.restPlan,
                    routineSubtitle = result.subtitle,
                    currentTab = MainTab.ROTINA
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
                restPlan = result.restPlan,
                routineSubtitle = result.subtitle
            )
        }
    }

    fun completeDay() {
        val p = _state.value.profile ?: return
        val today = todayKey()
        if (p.lastComplete == today) return
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val streak = if (p.lastComplete == yesterday) p.streak + 1 else 1
        val earned = maxOf(2, streak * 2)
        val updated = p.copy(lastComplete = today, streak = streak, coins = p.coins + earned)
        saveProfile(updated)
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
                userRepo.saveUser(profile)
                _state.update { it.copy(profile = profile) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun refreshAnnouncements() {
        viewModelScope.launch {
            _state.update { it.copy(announcements = contentRepo.loadAnnouncements()) }
        }
    }

    fun refreshRanking() {
        viewModelScope.launch {
            _state.update { it.copy(ranking = userRepo.loadRanking()) }
        }
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

    companion object {
        fun todayKey() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

data class CalendarEventDataUi(val type: String, val title: String, val message: String, val source: String)
