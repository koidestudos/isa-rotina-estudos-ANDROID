package br.com.isa.rotinaestudos.data.repository

import br.com.isa.rotinaestudos.data.model.Announcement
import br.com.isa.rotinaestudos.data.model.CalendarEvent
import br.com.isa.rotinaestudos.data.model.Flashcard
import br.com.isa.rotinaestudos.data.model.FlashcardSet
import br.com.isa.rotinaestudos.data.model.FriendEntry
import br.com.isa.rotinaestudos.data.model.LearningEntry
import br.com.isa.rotinaestudos.data.model.QuizAnswer
import br.com.isa.rotinaestudos.data.model.ScheduleBlock
import br.com.isa.rotinaestudos.data.model.SubjectDifficulty
import br.com.isa.rotinaestudos.data.model.TimeSlot
import br.com.isa.rotinaestudos.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreMapper {

    @Suppress("UNCHECKED_CAST")
    fun fromDocument(doc: DocumentSnapshot): UserProfile {
        val d = doc.data ?: return UserProfile(uid = doc.id)
        return UserProfile(
            uid = doc.id,
            name = d["name"] as? String ?: "",
            email = d["email"] as? String ?: "",
            bio = d["bio"] as? String ?: "",
            photoURL = d["photoURL"] as? String ?: "",
            joinedAt = d["joinedAt"] as? String ?: "",
            coins = (d["coins"] as? Number)?.toInt() ?: 0,
            streak = (d["streak"] as? Number)?.toInt() ?: 0,
            lastComplete = d["lastComplete"] as? String ?: "",
            calNotes = (d["calNotes"] as? Map<String, String>) ?: emptyMap(),
            quizAnswers = parseQuizAnswers(d["quizAnswers"]),
            hasRoutine = d["hasRoutine"] as? Boolean ?: false,
            savedSchedule = parseSchedule(d["savedSchedule"]),
            routineName = d["routineName"] as? String ?: "Sua Rotina Personalizada",
            userSeries = d["userSeries"] as? String ?: "",
            totalStudySeconds = (d["totalStudySeconds"] as? Number)?.toLong() ?: 0L,
            studyMotivation = d["studyMotivation"] as? String ?: "",
            flashcardSets = parseFlashcardSets(d["flashcardSets"]),
            learningHistory = parseLearningHistory(d["learningHistory"]),
            friends = parseFriends(d["friends"]),
            ownedItems = (d["ownedItems"] as? List<String>) ?: emptyList(),
            equippedItems = (d["equippedItems"] as? Map<String, String>) ?: emptyMap(),
            mascotData = (d["mascotData"] as? Map<String, Any>) ?: mapOf("xp" to 0, "level" to 1),
            banned = d["banned"] as? Boolean ?: false,
            banReason = d["banReason"] as? String ?: "",
            warnings = (d["warnings"] as? List<Map<String, String>>) ?: emptyList()
        )
    }

    fun toMap(profile: UserProfile): Map<String, Any?> = mapOf(
        "name" to profile.name,
        "email" to profile.email,
        "bio" to profile.bio,
        "photoURL" to profile.photoURL,
        "joinedAt" to profile.joinedAt,
        "coins" to profile.coins,
        "streak" to profile.streak,
        "lastComplete" to profile.lastComplete,
        "calNotes" to profile.calNotes,
        "quizAnswers" to profile.quizAnswers.map { it.toMap() },
        "hasRoutine" to profile.hasRoutine,
        "savedSchedule" to profile.savedSchedule?.mapValues { (_, blocks) ->
            blocks.map { b -> b.toMap() }
        },
        "routineName" to profile.routineName,
        "userSeries" to profile.userSeries,
        "totalStudySeconds" to profile.totalStudySeconds,
        "studyMotivation" to profile.studyMotivation,
        "flashcardSets" to profile.flashcardSets.map { it.toMap() },
        "learningHistory" to profile.learningHistory.map { it.toMap() },
        "friends" to profile.friends.map { mapOf("uid" to it.uid, "name" to it.name) },
        "ownedItems" to profile.ownedItems,
        "equippedItems" to profile.equippedItems,
        "mascotData" to profile.mascotData,
        "lastOnline" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseQuizAnswers(raw: Any?): List<QuizAnswer> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map { m ->
            QuizAnswer(
                q = (m["q"] as? Number)?.toInt() ?: 0,
                ans = m["ans"] as? String,
                txt = m["txt"] as? String,
                special = m["special"] as? String,
                horarios = (m["val"] as? List<Map<String, Any?>>)?.mapNotNull { h ->
                    if (m["special"] == "horarios") TimeSlot(
                        dias = h["dias"] as? String ?: "SEG",
                        ini = h["ini"] as? String ?: "08:00",
                        fim = h["fim"] as? String ?: "10:00"
                    ) else null
                } ?: (m["horarios"] as? List<Map<String, Any?>>)?.map {
                    TimeSlot(
                        dias = it["dias"] as? String ?: "SEG",
                        ini = it["ini"] as? String ?: "08:00",
                        fim = it["fim"] as? String ?: "10:00"
                    )
                },
                materias = (m["val"] as? List<Map<String, Any?>>)?.mapNotNull { s ->
                    if (m["special"] == "materias") SubjectDifficulty(
                        nome = s["nome"] as? String ?: "",
                        pct = (s["pct"] as? Number)?.toInt() ?: 50
                    ) else null
                } ?: (m["materias"] as? List<Map<String, Any?>>)?.map {
                    SubjectDifficulty(
                        nome = it["nome"] as? String ?: "",
                        pct = (it["pct"] as? Number)?.toInt() ?: 50
                    )
                }
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseSchedule(raw: Any?): Map<String, List<ScheduleBlock>>? {
        val map = raw as? Map<String, List<Map<String, Any?>>> ?: return null
        return map.mapValues { (_, blocks) ->
            blocks.map { b ->
                ScheduleBlock(
                    ini = b["ini"] as? String ?: "",
                    fim = b["fim"] as? String ?: "",
                    mat = b["mat"] as? String ?: "",
                    tipo = b["tipo"] as? String ?: "",
                    cor = b["cor"] as? String ?: "#228B22",
                    isBreak = b["isBreak"] as? Boolean ?: false,
                    isRev = b["isRev"] as? Boolean ?: false
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFlashcardSets(raw: Any?): List<FlashcardSet> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map { s ->
            FlashcardSet(
                id = s["id"] as? String ?: "",
                subject = s["subject"] as? String ?: "",
                cards = (s["cards"] as? List<Map<String, String>>)?.map {
                    Flashcard(q = it["q"] ?: "", a = it["a"] ?: "")
                } ?: emptyList()
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseLearningHistory(raw: Any?): List<LearningEntry> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map {
            LearningEntry(
                id = it["id"] as? String ?: "",
                title = it["title"] as? String ?: "",
                text = it["text"] as? String ?: "",
                date = it["date"] as? String ?: ""
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFriends(raw: Any?): List<FriendEntry> {
        val list = raw as? List<Map<String, Any?>> ?: return emptyList()
        return list.map {
            FriendEntry(uid = it["uid"] as? String ?: "", name = it["name"] as? String ?: "")
        }
    }

    private fun QuizAnswer.toMap(): Map<String, Any?> = when (special) {
        "horarios" -> mapOf("q" to q, "special" to special, "val" to horarios?.map {
            mapOf("dias" to it.dias, "ini" to it.ini, "fim" to it.fim)
        })
        "materias" -> mapOf("q" to q, "special" to special, "val" to materias?.map {
            mapOf("nome" to it.nome, "pct" to it.pct)
        })
        else -> mapOf("q" to q, "ans" to ans, "txt" to txt)
    }

    private fun ScheduleBlock.toMap() = mapOf(
        "ini" to ini, "fim" to fim, "mat" to mat, "tipo" to tipo,
        "cor" to cor, "isBreak" to isBreak, "isRev" to isRev
    )

    private fun FlashcardSet.toMap() = mapOf(
        "id" to id, "subject" to subject,
        "cards" to cards.map { mapOf("q" to it.q, "a" to it.a) }
    )

    private fun LearningEntry.toMap() = mapOf(
        "id" to id, "title" to title, "text" to text, "date" to date
    )

    fun announcementFrom(doc: DocumentSnapshot): Announcement {
        val d = doc.data ?: return Announcement(id = doc.id)
        val ts = d["createdAt"]
        val millis = when (ts) {
            is Timestamp -> ts.toDate().time
            else -> 0L
        }
        return Announcement(
            id = doc.id,
            message = d["message"] as? String ?: "",
            adminName = d["adminName"] as? String ?: "Admin",
            createdAt = millis
        )
    }

    fun calendarEventFrom(doc: DocumentSnapshot): CalendarEvent {
        val d = doc.data ?: return CalendarEvent(id = doc.id)
        return CalendarEvent(
            id = doc.id,
            date = d["date"] as? String ?: "",
            title = d["title"] as? String ?: "",
            message = d["message"] as? String ?: "",
            type = d["type"] as? String ?: ""
        )
    }
}

class UserRepository(private val db: FirebaseFirestore) {
    suspend fun loadUser(uid: String): UserProfile? {
        val doc = db.collection("users").document(uid).get().await()
        return if (doc.exists()) FirestoreMapper.fromDocument(doc) else null
    }

    suspend fun saveUser(profile: UserProfile) {
        db.collection("users").document(profile.uid)
            .set(FirestoreMapper.toMap(profile), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun loadRanking(): List<UserProfile> {
        val snap = db.collection("users").limit(50).get().await()
        return snap.documents.map { FirestoreMapper.fromDocument(it) }
            .sortedByDescending { (it.streak * 1_000_000_000L) + it.coins }
    }
}

class ContentRepository(private val db: FirebaseFirestore) {
    suspend fun loadAnnouncements(): List<Announcement> {
        val snap = try {
            db.collection("announcements").orderBy("createdAt").limit(40).get().await()
        } catch (_: Exception) {
            db.collection("announcements").limit(40).get().await()
        }
        return snap.documents.map { FirestoreMapper.announcementFrom(it) }
    }

    suspend fun loadCalendarEvents(): Map<String, CalendarEvent> {
        val snap = db.collection("calendar_events").limit(200).get().await()
        return snap.documents.map { FirestoreMapper.calendarEventFrom(it) }
            .filter { it.date.isNotBlank() }
            .associateBy { it.date }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun loadCalendarOverrides(): Map<String, Map<String, Any?>> {
        val doc = db.collection("app_config").document("calendar_overrides").get().await()
        if (!doc.exists()) return emptyMap()
        return (doc.data?.get("overrides") as? Map<String, Map<String, Any?>>) ?: emptyMap()
    }

    suspend fun saveCalendarOverride(date: String, type: String, title: String, message: String, adminName: String) {
        val ref = db.collection("app_config").document("calendar_overrides")
        val snap = ref.get().await()
        val overrides = (snap.data?.get("overrides") as? MutableMap<String, Any>)?.toMutableMap() ?: mutableMapOf()
        overrides[date] = mapOf(
            "date" to date, "type" to type, "title" to title, "message" to message,
            "adminName" to adminName, "updatedAt" to java.time.Instant.now().toString()
        )
        ref.set(mapOf("overrides" to overrides, "updatedAt" to FieldValue.serverTimestamp()), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun addCalendarEvent(date: String, title: String, message: String, type: String, adminName: String) {
        db.collection("calendar_events").add(
            mapOf(
                "date" to date, "title" to title, "message" to message, "type" to type,
                "adminName" to adminName, "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun updateCalendarEvent(id: String, date: String, title: String, message: String, type: String) {
        db.collection("calendar_events").document(id).update(
            mapOf("date" to date, "title" to title, "message" to message, "type" to type, "updatedAt" to FieldValue.serverTimestamp())
        ).await()
    }

    suspend fun deleteCalendarEvent(id: String) {
        db.collection("calendar_events").document(id).delete().await()
    }

    suspend fun loadAdminCalendarEvents(): List<CalendarEvent> {
        val snap = try {
            db.collection("calendar_events").orderBy("date").limit(200).get().await()
        } catch (_: Exception) {
            db.collection("calendar_events").limit(200).get().await()
        }
        return snap.documents.map { FirestoreMapper.calendarEventFrom(it) }
    }
}
