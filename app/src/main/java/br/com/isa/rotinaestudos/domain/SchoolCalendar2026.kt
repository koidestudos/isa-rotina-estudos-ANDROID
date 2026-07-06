package br.com.isa.rotinaestudos.domain

import java.time.DayOfWeek
import java.time.LocalDate

data class CalendarEventData(
    val type: String,
    val title: String,
    val message: String = ""
)

object SchoolCalendar2026 {

    private var cache: Map<String, CalendarEventData>? = null

    fun build(): Map<String, CalendarEventData> {
        cache?.let { return it }

        val ev = mutableMapOf<String, CalendarEventData>()

        calPut(ev, 2026, 1, 26, "inicio_1", "Início do Período Letivo — 1º Semestre")
        calPut(ev, 2026, 2, 11, "simulado_isa", "Aniversário ISA")
        calPutRange(ev, 2026, 2, 14, 17, "feriado", "Carnaval")
        calPut(ev, 2026, 2, 18, "feriado", "Quarta-feira de Cinzas")
        calPutRange(ev, 2026, 2, 25, 27, "avaliacao", "1ª AVM")
        calPutRange(ev, 2026, 3, 2, 3, "avaliacao", "1ª AVM")
        calPut(ev, 2026, 3, 14, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 3, 21, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 3, 28, "simulado_isa", "Simulado ISA")
        calPutRange(ev, 2026, 4, 2, 4, "feriado", "Semana Santa")
        calPut(ev, 2026, 4, 11, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 4, 21, "feriado", "Tiradentes")
        calPut(ev, 2026, 4, 25, "plantao", "Plantão Escolar")
        calPut(ev, 2026, 5, 1, "feriado", "Dia do Trabalho")
        calPutRange(ev, 2026, 5, 13, 19, "avaliacao", "2ª AVM")
        calPut(ev, 2026, 5, 16, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 5, 23, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 6, 4, "feriado", "Corpus Christi")
        calPut(ev, 2026, 6, 13, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 6, 20, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 6, 27, "resultado", "Resultado — 1º Semestre")
        calPutRange(ev, 2026, 6, 29, 30, "recuperacao", "Aulas de Recuperação")
        calPutRange(ev, 2026, 7, 1, 3, "recuperacao", "Aulas de Recuperação")
        calPut(ev, 2026, 7, 4, "ferias", "Férias")
        calPut(ev, 2026, 7, 5, "ferias", "Férias")
        calPutRange(ev, 2026, 7, 6, 8, "av_recuperacao", "Av. de Recuperação")
        calPutRange(ev, 2026, 7, 9, 31, "ferias", "Férias")
        calPut(ev, 2026, 8, 1, "ferias", "Férias")
        calPut(ev, 2026, 8, 3, "inicio_2", "Início do Período Letivo — 2º Semestre")
        calPut(ev, 2026, 8, 7, "resultado", "Resultado — Recuperação")
        calPut(ev, 2026, 8, 8, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 8, 16, "feriado", "Aniversário de Teresina")
        calPut(ev, 2026, 8, 22, "simulado_bern", "Simulado Bernoulli")
        calPutRange(ev, 2026, 8, 24, 28, "avaliacao", "3ª AVM")
        calPut(ev, 2026, 9, 7, "feriado", "Independência do Brasil")
        calPut(ev, 2026, 9, 19, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 9, 26, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 10, 12, "feriado", "N. Sra. Aparecida")
        calPut(ev, 2026, 10, 15, "feriado", "Dia do Professor")
        calPut(ev, 2026, 10, 19, "feriado", "Dia do Piauí")
        calPut(ev, 2026, 10, 20, "avaliacao", "4ª AVM")
        calPutRange(ev, 2026, 10, 23, 26, "avaliacao", "4ª AVM")
        calPut(ev, 2026, 11, 2, "feriado", "Finados")
        calPut(ev, 2026, 11, 14, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 11, 15, "feriado", "Proclamação da República")
        calPut(ev, 2026, 11, 16, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 11, 17, "simulado_bern", "Simulado Bernoulli")
        calPut(ev, 2026, 11, 20, "feriado", "Consciência Negra")
        calPut(ev, 2026, 11, 23, "simulado_isa", "Simulado ISA")
        calPut(ev, 2026, 12, 2, "resultado", "Resultado — 2º Semestre")
        calPutRange(ev, 2026, 12, 3, 5, "prova_final", "Prova Final")
        calPut(ev, 2026, 12, 8, "feriado", "Imaculada Conceição")
        calPut(ev, 2026, 12, 11, "resultado", "Resultado — Prova Final")
        calPutRange(ev, 2026, 12, 14, 18, "recuperacao", "Aulas de Recuperação Geral")
        calPutRange(ev, 2026, 12, 21, 23, "av_recuperacao", "Av. de Recuperação Geral")
        calPut(ev, 2026, 12, 25, "feriado", "Natal")
        calPut(ev, 2026, 12, 30, "resultado", "Resultado Geral")

        val aulas = mutableSetOf<String>()
        fun addA(y: Int, m: Int, a: Int, b: Int) {
            for (d in a..b) aulas.add(calKey(y, m, d))
        }
        addA(2026, 1, 27, 31)
        addA(2026, 2, 2, 6)
        addA(2026, 2, 9, 10)
        addA(2026, 2, 12, 13)
        addA(2026, 2, 19, 20)
        addA(2026, 2, 23, 24)
        addA(2026, 3, 4, 6)
        addA(2026, 3, 9, 13)
        addA(2026, 3, 16, 20)
        addA(2026, 3, 23, 27)
        addA(2026, 3, 30, 31)
        addA(2026, 4, 1, 1)
        addA(2026, 4, 6, 10)
        addA(2026, 4, 13, 17)
        addA(2026, 4, 20, 20)
        addA(2026, 4, 22, 24)
        addA(2026, 4, 27, 30)
        addA(2026, 5, 4, 8)
        addA(2026, 5, 11, 12)
        addA(2026, 5, 20, 22)
        addA(2026, 5, 25, 29)
        addA(2026, 6, 1, 3)
        addA(2026, 6, 5, 5)
        addA(2026, 6, 8, 12)
        addA(2026, 6, 15, 19)
        addA(2026, 6, 22, 26)
        addA(2026, 8, 4, 6)
        addA(2026, 8, 10, 15)
        addA(2026, 8, 17, 22)
        addA(2026, 8, 29, 29)
        addA(2026, 8, 31, 31)
        addA(2026, 9, 1, 5)
        addA(2026, 9, 8, 12)
        addA(2026, 9, 14, 18)
        addA(2026, 9, 21, 25)
        addA(2026, 9, 28, 30)
        addA(2026, 10, 1, 3)
        addA(2026, 10, 5, 11)
        addA(2026, 10, 13, 14)
        addA(2026, 10, 16, 18)
        addA(2026, 10, 21, 22)
        addA(2026, 10, 27, 31)
        addA(2026, 11, 3, 7)
        addA(2026, 11, 9, 13)
        addA(2026, 11, 17, 19)
        addA(2026, 11, 21, 24)
        addA(2026, 11, 26, 28)
        addA(2026, 12, 1, 1)
        addA(2026, 12, 7, 7)
        addA(2026, 12, 9, 10)
        addA(2026, 12, 12, 12)

        var dt = LocalDate.of(2026, 1, 26)
        val end = LocalDate.of(2026, 12, 30)
        while (!dt.isAfter(end)) {
            val k = calKey(dt.year, dt.monthValue, dt.dayOfMonth)
            if (ev.containsKey(k)) {
                dt = dt.plusDays(1)
                continue
            }
            if (isVac(k)) {
                ev[k] = CalendarEventData(type = "ferias", title = "Férias")
                dt = dt.plusDays(1)
                continue
            }
            if (aulas.contains(k)) {
                dt = dt.plusDays(1)
                continue
            }
            val isWeekend = dt.dayOfWeek == DayOfWeek.SATURDAY || dt.dayOfWeek == DayOfWeek.SUNDAY
            ev[k] = CalendarEventData(
                type = "feriado",
                title = if (isWeekend) "Fim de semana" else "Feriado / Sem aula"
            )
            dt = dt.plusDays(1)
        }

        cache = ev
        return ev
    }

    fun getEvent(key: String, overrides: Map<String, Map<String, Any?>>): CalendarEventData? {
        val base = build()[key]
        val ov = overrides[key]
        if (ov?.get("hidden") == true) return null
        if (base == null && ov == null) return null
        return CalendarEventData(
            type = (ov?.get("type") as? String) ?: base?.type ?: "",
            title = when {
                ov?.get("title") != null && ov["title"] != "" -> ov["title"] as String
                else -> base?.title ?: ""
            },
            message = (ov?.get("message") as? String) ?: base?.message ?: ""
        )
    }

    private fun calKey(y: Int, m: Int, d: Int): String =
        "$y-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"

    private fun calPut(
        ev: MutableMap<String, CalendarEventData>,
        y: Int,
        m: Int,
        d: Int,
        type: String,
        title: String,
        message: String = ""
    ) {
        ev[calKey(y, m, d)] = CalendarEventData(type, title, message.ifEmpty { title })
    }

    private fun calPutRange(
        ev: MutableMap<String, CalendarEventData>,
        y: Int,
        m: Int,
        d1: Int,
        d2: Int,
        type: String,
        title: String,
        message: String = ""
    ) {
        for (d in d1..d2) calPut(ev, y, m, d, type, title, message)
    }

    private fun isVac(k: String): Boolean =
        k == "2026-08-01" ||
            k == "2026-07-04" ||
            k == "2026-07-05" ||
            (k.startsWith("2026-07-") && (k.substring(8).toIntOrNull() ?: 0) >= 9)
}
