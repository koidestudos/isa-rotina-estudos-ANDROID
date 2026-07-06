package br.com.isa.rotinaestudos.data.model

data class SubjectDifficulty(
    val nome: String = "",
    val pct: Int = 50
)

data class TimeSlot(
    val dias: String = "SEG",
    val ini: String = "08:00",
    val fim: String = "10:00"
)

data class QuizAnswer(
    val q: Int = 0,
    val ans: String? = null,
    val txt: String? = null,
    val special: String? = null,
    val horarios: List<TimeSlot>? = null,
    val materias: List<SubjectDifficulty>? = null
)

data class ScheduleBlock(
    val ini: String,
    val fim: String,
    val mat: String,
    val tipo: String,
    val cor: String,
    val isBreak: Boolean = false,
    val isRev: Boolean = false
)

data class FlashcardSet(
    val id: String = "",
    val subject: String = "",
    val cards: List<Flashcard> = emptyList()
)

data class Flashcard(
    val q: String = "",
    val a: String = ""
)

data class LearningEntry(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val date: String = ""
)

data class FriendEntry(
    val uid: String = "",
    val name: String = ""
)

data class CalendarEvent(
    val date: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val source: String = "admin",
    val id: String = ""
)

data class Announcement(
    val id: String = "",
    val message: String = "",
    val adminName: String = "",
    val createdAt: Long = 0L
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val photoURL: String = "",
    val joinedAt: String = "",
    val coins: Int = 0,
    val streak: Int = 0,
    val lastComplete: String = "",
    val calNotes: Map<String, String> = emptyMap(),
    val quizAnswers: List<QuizAnswer> = emptyList(),
    val hasRoutine: Boolean = false,
    val savedSchedule: Map<String, List<ScheduleBlock>>? = null,
    val routineName: String = "Sua Rotina Personalizada",
    val userSeries: String = "",
    val totalStudySeconds: Long = 0,
    val studyMotivation: String = "",
    val flashcardSets: List<FlashcardSet> = emptyList(),
    val learningHistory: List<LearningEntry> = emptyList(),
    val friends: List<FriendEntry> = emptyList(),
    val ownedItems: List<String> = emptyList(),
    val equippedItems: Map<String, String> = emptyMap(),
    val mascotData: Map<String, Any> = mapOf("xp" to 0, "level" to 1),
    val banned: Boolean = false,
    val banReason: String = "",
    val warnings: List<Map<String, String>> = emptyList()
)
