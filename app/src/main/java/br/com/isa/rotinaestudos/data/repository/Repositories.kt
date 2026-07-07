package br.com.isa.rotinaestudos.data.repository

import br.com.isa.rotinaestudos.data.model.Announcement
import br.com.isa.rotinaestudos.data.model.CalendarEvent
import br.com.isa.rotinaestudos.data.model.ExamTimelineItem
import br.com.isa.rotinaestudos.data.model.GradesConfig
import br.com.isa.rotinaestudos.data.model.SubjectGrades
import br.com.isa.rotinaestudos.data.model.Flashcard
import br.com.isa.rotinaestudos.data.model.FlashcardSet
import br.com.isa.rotinaestudos.data.model.ChatSummary
import br.com.isa.rotinaestudos.data.model.FriendEntry
import br.com.isa.rotinaestudos.data.model.LearningEntry
import br.com.isa.rotinaestudos.data.model.StudyingActiveUser
import br.com.isa.rotinaestudos.data.model.StudyingNowInfo
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

@Suppress("UNCHECKED_CAST")
private fun parseStudyingNow(raw: Any?): StudyingNowInfo? {
    val m = raw as? Map<String, Any?> ?: return null
    return StudyingNowInfo(
        active = m["active"] as? Boolean ?: false,
        mode = m["mode"] as? String ?: "study",
        motivation = m["motivation"] as? String ?: "",
        since = m["since"] as? String ?: "",
        elapsedSecs = (m["elapsedSecs"] as? Number)?.toInt() ?: 0
    )
}

object FirestoreMapper {

    @Suppress("UNCHECKED_CAST")
    fun parseEquippedItemsPublic(raw: Any?): Map<String, Any> = parseEquippedItems(raw)

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
            nameLastChanged = d["nameLastChanged"] as? String ?: "",
            userSeries = d["userSeries"] as? String ?: "",
            totalStudySeconds = (d["totalStudySeconds"] as? Number)?.toLong() ?: 0L,
            studyMotivation = d["studyMotivation"] as? String ?: "",
            flashcardSets = parseFlashcardSets(d["flashcardSets"]),
            learningHistory = parseLearningHistory(d["learningHistory"]),
            friends = parseFriends(d["friends"]),
            ownedItems = (d["ownedItems"] as? List<String>) ?: emptyList(),
            equippedItems = parseEquippedItems(d["equippedItems"]),
            mascotData = (d["mascotData"] as? Map<String, Any>) ?: mapOf("xp" to 0, "level" to 1),
            gradesConfig = parseGradesConfig(d["gradesConfig"]),
            gradesData = parseGradesData(d["gradesData"]),
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
        "nameLastChanged" to profile.nameLastChanged,
        "userSeries" to profile.userSeries,
        "totalStudySeconds" to profile.totalStudySeconds,
        "studyMotivation" to profile.studyMotivation,
        "flashcardSets" to profile.flashcardSets.map { it.toMap() },
        "learningHistory" to profile.learningHistory.map { it.toMap() },
        "friends" to profile.friends.map { mapOf("uid" to it.uid, "name" to it.name) },
        "ownedItems" to profile.ownedItems,
        "equippedItems" to profile.equippedItems,
        "mascotData" to profile.mascotData,
        "gradesConfig" to profile.gradesConfig?.toMap(),
        "gradesData" to profile.gradesData.mapValues { (_, g) -> g.toMap() },
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
    private fun parseEquippedItems(raw: Any?): Map<String, Any> {
        val map = raw as? Map<String, Any?> ?: return emptyMap()
        return map.mapNotNull { (k, v) ->
            when (v) {
                is List<*> -> k to v.filterIsInstance<String>()
                is String -> k to v
                else -> v?.let { k to it }
            }
        }.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseGradesConfig(raw: Any?): GradesConfig? {
        val m = raw as? Map<String, Any?> ?: return null
        return GradesConfig(
            setupDone = m["setupDone"] as? Boolean ?: false,
            style = m["style"] as? String ?: "isa",
            minAvg = (m["minAvg"] as? Number)?.toDouble() ?: 7.0,
            examNames = (m["examNames"] as? List<String>) ?: emptyList(),
            timeline = (m["timeline"] as? List<Map<String, Any?>>)?.map {
                ExamTimelineItem(
                    id = it["id"] as? String ?: "",
                    type = it["type"] as? String ?: "",
                    name = it["name"] as? String ?: ""
                )
            } ?: emptyList(),
            subjects = (m["subjects"] as? List<String>) ?: emptyList()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseGradesData(raw: Any?): Map<String, SubjectGrades> {
        val map = raw as? Map<String, Map<String, Any?>> ?: return emptyMap()
        return map.mapValues { (_, v) ->
            SubjectGrades(
                exams = (v["exams"] as? Map<String, String>) ?: emptyMap(),
                recoveries = (v["recoveries"] as? Map<String, String>) ?: emptyMap(),
                provaFinal = v["provaFinal"] as? String ?: ""
            )
        }
    }

    private fun GradesConfig.toMap(): Map<String, Any?> = mapOf(
        "setupDone" to setupDone,
        "style" to style,
        "minAvg" to minAvg,
        "examNames" to examNames,
        "timeline" to timeline.map { mapOf("id" to it.id, "type" to it.type, "name" to it.name) },
        "subjects" to subjects
    )

    private fun SubjectGrades.toMap(): Map<String, Any?> = mapOf(
        "exams" to exams,
        "recoveries" to recoveries,
        "provaFinal" to provaFinal
    )

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

object RankHelper {
    fun score(profile: UserProfile): Long =
        profile.streak.toLong() * 1_000_000_000L + profile.coins

    fun sort(users: List<UserProfile>): List<UserProfile> =
        users.filter { !it.banned }.sortedByDescending { score(it) }

    fun rankPosition(users: List<UserProfile>, uid: String?): String? {
        if (uid.isNullOrBlank()) return null
        val sorted = sort(users).take(30)
        val idx = sorted.indexOfFirst { it.uid == uid }
        return when {
            idx >= 0 -> "${idx + 1}º lugar"
            sorted.isNotEmpty() -> "Fora do top 30"
            else -> null
        }
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
        return RankHelper.sort(snap.documents.map { FirestoreMapper.fromDocument(it) }).take(30)
    }

    suspend fun loadStudyingActive(): List<StudyingActiveUser> {
        val snap = db.collection("users").limit(100).get().await()
        return snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            if (d["banned"] == true) return@mapNotNull null
            val studying = parseStudyingNow(d["studyingNow"]) ?: return@mapNotNull null
            if (!studying.active) return@mapNotNull null
            StudyingActiveUser(
                uid = doc.id,
                name = d["name"] as? String ?: "Estudante",
                photoURL = d["photoURL"] as? String ?: "",
                motivation = studying.motivation.ifBlank { d["studyMotivation"] as? String ?: "" },
                mode = studying.mode,
                since = studying.since,
                elapsedBase = studying.elapsedSecs,
                equippedItems = FirestoreMapper.parseEquippedItemsPublic(d["equippedItems"]),
                lastOnline = d["lastOnline"]?.toString() ?: d["updatedAt"]?.toString() ?: ""
            )
        }
    }

    suspend fun setStudyingNow(uid: String, info: StudyingNowInfo?) {
        val ref = db.collection("users").document(uid)
        if (info == null || !info.active) {
            ref.update(
                mapOf(
                    "studyingNow" to FieldValue.delete(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            ref.set(
                mapOf(
                    "studyingNow" to mapOf(
                        "active" to true,
                        "mode" to info.mode,
                        "motivation" to info.motivation,
                        "since" to info.since,
                        "elapsedSecs" to info.elapsedSecs
                    ),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    suspend fun updateStudyMotivation(uid: String, motivation: String) {
        db.collection("users").document(uid).update(
            mapOf(
                "studyMotivation" to motivation,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun findByEmail(email: String): UserProfile? {
        val snap = db.collection("users").whereEqualTo("email", email.trim().lowercase()).limit(1).get().await()
        return snap.documents.firstOrNull()?.let { FirestoreMapper.fromDocument(it) }
    }

    suspend fun adminUpdateCoinsStreak(uid: String, coins: Int?, streak: Int?) {
        val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
        if (coins != null) updates["coins"] = coins
        if (streak != null) updates["streak"] = streak
        db.collection("users").document(uid).update(updates).await()
    }

    suspend fun adminBanUser(uid: String, reason: String, adminEmail: String) {
        db.collection("users").document(uid).update(
            mapOf(
                "banned" to true,
                "banReason" to reason.ifBlank { "Violou as regras do app." },
                "bannedAt" to java.time.Instant.now().toString(),
                "bannedBy" to adminEmail,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun adminUnbanUser(uid: String) {
        db.collection("users").document(uid).update(
            mapOf(
                "banned" to false,
                "banReason" to "",
                "bannedAt" to "",
                "bannedBy" to "",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun adminSendWarning(uid: String, message: String, adminName: String, adminEmail: String) {
        val entry = mapOf(
            "message" to message,
            "adminName" to adminName,
            "adminEmail" to adminEmail,
            "createdAt" to java.time.Instant.now().toString()
        )
        db.collection("users").document(uid).update(
            mapOf(
                "warnings" to FieldValue.arrayUnion(entry),
                "warningCount" to FieldValue.increment(1),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
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

    suspend fun addAnnouncement(message: String, adminName: String) {
        db.collection("announcements").add(
            mapOf(
                "message" to message,
                "adminName" to adminName,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun loadSoepEmails(): List<String> {
        return try {
            val doc = db.collection("app_config").document("soep").get().await()
            val emails = doc.data?.get("emails") as? List<String>
            if (!emails.isNullOrEmpty()) emails else listOf("tiagogamerplayer133@gmail.com")
        } catch (_: Exception) {
            listOf("tiagogamerplayer133@gmail.com")
        }
    }

    suspend fun saveSoepEmails(emails: List<String>) {
        db.collection("app_config").document("soep").set(
            mapOf("emails" to emails, "updatedAt" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun loadAdminChats(): List<ChatSummary> {
        val snap = try {
            db.collection("chats").orderBy("updatedAt").limit(40).get().await()
        } catch (_: Exception) {
            db.collection("chats").limit(40).get().await()
        }
        return snap.documents.map { doc ->
            val d = doc.data ?: emptyMap()
            val ts = d["updatedAt"]
            val millis = when (ts) {
                is Timestamp -> ts.toDate().time
                else -> 0L
            }
            @Suppress("UNCHECKED_CAST")
            ChatSummary(
                id = doc.id,
                participants = (d["participants"] as? List<String>) ?: emptyList(),
                participantNames = (d["participantNames"] as? Map<String, String>) ?: emptyMap(),
                lastMessage = d["lastMessage"] as? String ?: "",
                chatApproved = d["chatApproved"] as? Boolean ?: false,
                hasPending = d["hasPending"] as? Boolean ?: false,
                updatedAt = millis
            )
        }.sortedByDescending { it.updatedAt }
    }
}
