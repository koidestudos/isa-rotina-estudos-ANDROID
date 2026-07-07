package br.com.isa.rotinaestudos.domain

import br.com.isa.rotinaestudos.data.model.GradesConfig
import br.com.isa.rotinaestudos.data.model.SubjectGrades

data class SubjectAnalysis(
    val targetSum: Double,
    val mediaSum: Double,
    val deficit: Double,
    val passed: Boolean,
    val neededPerExam: Double,
    val emptyExams: Int,
    val rec2: Double?
)

data class SubjectStatus(val pass: Boolean, val deficit: Double)

object GradesHelper {

    fun defaultExamNames(count: Int = 8): List<String> {
        val names = mutableListOf<String>()
        var pair = 1
        while (names.size < count) {
            names.add("Avaliação Mensal $pair (AVM$pair)")
            names.add("Avaliação Bimestral $pair (AVB$pair)")
            pair++
        }
        return names.take(count)
    }

    fun buildExamTimeline(examNames: List<String>, style: String): List<Map<String, String>> {
        val timeline = mutableListOf<Map<String, String>>()
        var recNum = 1
        examNames.forEachIndexed { i, name ->
            timeline.add(mapOf("id" to "exam_$i", "type" to "exam", "name" to name))
            if ((i + 1) % 4 == 0) {
                if (style == "isa" && i == examNames.lastIndex) {
                    timeline.add(mapOf("id" to "pf_1", "type" to "provaFinal", "name" to "Prova Final"))
                }
                timeline.add(mapOf("id" to "rec_$recNum", "type" to "recovery", "name" to "Recuperação $recNum"))
                recNum++
            }
        }
        return timeline
    }

    fun parseGrade(v: String?): Double? {
        if (v.isNullOrBlank()) return null
        val n = v.replace(',', '.').toDoubleOrNull() ?: return null
        return n
    }

    private fun pairMedia(g1: String?, g2: String?): Double? {
        val a = parseGrade(g1) ?: return null
        val b = parseGrade(g2) ?: return null
        return (a + b) / 2.0
    }

    fun calcSubjectAnalysis(cfg: GradesConfig, data: SubjectGrades): SubjectAnalysis {
        val examCount = cfg.examNames.size
        val numPairs = examCount / 2
        val targetSum = cfg.minAvg * numPairs
        val examGrades = cfg.examNames.indices.map { i -> data.exams["exam_$i"] ?: "" }
        var mediaSum = 0.0
        val blocks = (examCount + 3) / 4
        var rec2: Double? = null
        for (b in 0 until blocks) {
            val start = b * 4
            var m1 = pairMedia(
                examGrades.getOrNull(start),
                examGrades.getOrNull(start + 1)
            )
            var m2 = pairMedia(
                examGrades.getOrNull(start + 2),
                examGrades.getOrNull(start + 3)
            )
            val rec = parseGrade(data.recoveries["rec_${b + 1}"])
            if (rec != null && m1 != null && m2 != null) {
                if (cfg.style == "isa") {
                    if (m1 <= m2) m1 = rec else m2 = rec
                } else {
                    if (m1 <= m2) m1 = maxOf(m1, rec) else m2 = maxOf(m2, rec)
                }
            }
            if (m1 != null) mediaSum += m1
            if (m2 != null) mediaSum += m2
            if (b == 1) rec2 = parseGrade(data.recoveries["rec_2"])
        }
        if (cfg.style == "isa") {
            val pf = parseGrade(data.provaFinal)
            if (pf != null && mediaSum < targetSum) {
                mediaSum += minOf(pf, targetSum - mediaSum)
            }
        }
        var passed = false
        if (cfg.style == "isa" && rec2 != null && rec2 >= cfg.minAvg) passed = true
        else if (mediaSum >= targetSum) passed = true
        val deficit = maxOf(0.0, targetSum - mediaSum)
        val emptyExams = examGrades.count { parseGrade(it) == null }
        val neededPerExam = if (emptyExams > 0) deficit / emptyExams else 0.0
        return SubjectAnalysis(targetSum, mediaSum, deficit, passed, neededPerExam, emptyExams, rec2)
    }

    fun calcSubjectStatus(cfg: GradesConfig?, sub: String, gradesData: Map<String, SubjectGrades>): SubjectStatus {
        if (cfg == null || !cfg.setupDone) return SubjectStatus(pass = false, deficit = 0.0)
        val analysis = calcSubjectAnalysis(cfg, gradesData[sub] ?: SubjectGrades())
        return SubjectStatus(pass = analysis.passed, deficit = analysis.deficit)
    }
}
