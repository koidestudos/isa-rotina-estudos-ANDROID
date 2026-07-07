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

data class StudyingNowInfo(
    val active: Boolean = false,
    val mode: String = "study",
    val motivation: String = "",
    val since: String = "",
    val elapsedSecs: Int = 0
)

data class StudyingActiveUser(
    val uid: String = "",
    val name: String = "",
    val photoURL: String = "",
    val motivation: String = "",
    val mode: String = "study",
    val since: String = "",
    val elapsedBase: Int = 0,
    val equippedItems: Map<String, Any> = emptyMap(),
    val lastOnline: String = ""
)

data class ChatSummary(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val chatApproved: Boolean = false,
    val hasPending: Boolean = false,
    val updatedAt: Long = 0L
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

data class ExamTimelineItem(
    val id: String = "",
    val type: String = "",
    val name: String = ""
)

data class GradesConfig(
    val setupDone: Boolean = false,
    val style: String = "isa",
    val minAvg: Double = 7.0,
    val examNames: List<String> = emptyList(),
    val timeline: List<ExamTimelineItem> = emptyList(),
    val subjects: List<String> = emptyList()
)

data class SubjectGrades(
    val exams: Map<String, String> = emptyMap(),
    val recoveries: Map<String, String> = emptyMap(),
    val provaFinal: String = ""
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
    val nameLastChanged: String = "",
    val userSeries: String = "",
    val totalStudySeconds: Long = 0,
    val studyMotivation: String = "",
    val flashcardSets: List<FlashcardSet> = emptyList(),
    val learningHistory: List<LearningEntry> = emptyList(),
    val friends: List<FriendEntry> = emptyList(),
    val ownedItems: List<String> = emptyList(),
    val equippedItems: Map<String, Any> = emptyMap(),
    val mascotData: Map<String, Any> = mapOf("xp" to 0, "level" to 1),
    val gradesConfig: GradesConfig? = null,
    val gradesData: Map<String, SubjectGrades> = emptyMap(),
    val banned: Boolean = false,
    val banReason: String = "",
    val warnings: List<Map<String, String>> = emptyList()
)
