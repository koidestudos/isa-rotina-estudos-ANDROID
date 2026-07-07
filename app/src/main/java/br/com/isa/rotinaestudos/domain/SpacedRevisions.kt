package br.com.isa.rotinaestudos.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class RevisionEntry(
    val subject: String,
    val difficulty: Int,
    val day1: String,
    val day7: String,
    val day30: String
)

object SpacedRevisions {
    private val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun generate(materias: List<br.com.isa.rotinaestudos.data.model.SubjectDifficulty>): List<RevisionEntry> {
        val today = LocalDate.now()
        return materias
            .filter { it.nome.trim().isNotEmpty() }
            .sortedByDescending { it.pct }
            .take(8)
            .map { m ->
                RevisionEntry(
                    subject = m.nome.trim(),
                    difficulty = m.pct,
                    day1 = today.plusDays(1).format(fmt),
                    day7 = today.plusDays(7).format(fmt),
                    day30 = today.plusDays(30).format(fmt)
                )
            }
    }
}
