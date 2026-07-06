package br.com.isa.rotinaestudos.domain

import br.com.isa.rotinaestudos.AppConstants
import br.com.isa.rotinaestudos.data.model.QuizAnswer
import br.com.isa.rotinaestudos.data.model.ScheduleBlock
import br.com.isa.rotinaestudos.data.model.SubjectDifficulty
import br.com.isa.rotinaestudos.data.model.TimeSlot
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

data class GenerationResult(
    val schedule: Map<String, List<ScheduleBlock>>,
    val methodsHtml: List<String>,
    val recommendations: List<String>,
    val restPlan: List<String>,
    val subtitle: String,
    val subjectBars: List<Pair<String, Int>>
)

private data class PoolItem(
    val nome: String,
    val pct: Int,
    val colorIdx: Int
)

object ScheduleGenerator {

    fun generate(
        answers: List<QuizAnswer>,
        horarios: List<TimeSlot>,
        materias: List<SubjectDifficulty>
    ): GenerationResult {
        val provaProxima = getAns(answers, 3)
        val foco = getAns(answers, 8)
        val numDescansos = parseNumFromAns(getAns(answers, 10), 2)
        val durDescanso = parseNumFromAns(getAns(answers, 11), 10)
        val silencio = getAns(answers, 13)
        val interrupcoes = getAns(answers, 14)
        val celular = getAns(answers, 15)
        val aprende = getAns(answers, 17)
        val procrastina = getAns(answers, 22)
        val disciplina = getAns(answers, 23)

        var sessMin = 30
        sessMin = when {
            foco.contains("Mais de 60") -> 55
            foco.contains("40–60") -> 45
            foco.contains("25–40") -> 30
            foco.contains("15–25") -> 20
            else -> 15
        }
        if (procrastina.contains("Sempre")) {
            sessMin = minOf(sessMin, 20)
        } else if (procrastina.contains("Frequente")) {
            sessMin = minOf(sessMin, 25)
        }
        val pausaMin = when {
            sessMin >= 45 -> 10
            sessMin >= 30 -> 7
            else -> 5
        }

        val urgent = provaProxima.contains("menos de 2") || provaProxima.contains("2–4 sem")

        val mats = materias
            .filter { it.nome.trim().isNotEmpty() }
            .sortedByDescending { it.pct }

        val subjectBars = mats.map { it.nome to it.pct }

        val tipos = when {
            aprende.contains("Assistindo") -> listOf(
                "🎥 Videoaula", "✍️ Exercícios", "📝 Revisão", "🔍 Pesquisa", "📋 Resumo"
            )
            aprende.contains("Lendo") -> listOf(
                "📖 Leitura", "📝 Resumo", "🧠 Mapa Mental", "✍️ Exercícios", "🔍 Fixação"
            )
            aprende.contains("Praticando") -> listOf(
                "✍️ Exercícios", "📊 Simulado", "🔁 Revisão", "📖 Teoria", "✅ Questões"
            )
            else -> listOf(
                "📖 Leitura", "✍️ Exercícios", "🔁 Revisão", "🧠 Mapa Mental", "📝 Resumo"
            )
        }

        val diasBlocos = AppConstants.DAYS.associateWith { mutableListOf<ScheduleBlock>() }.toMutableMap()
        val fallbackMat = PoolItem("Revisão Geral", 50, 0)

        horarios.forEachIndexed { hi, h ->
            val ini = toMin(h.ini)
            val fim = toMin(h.fim)
            var t = ini
            var pool = buildWeightedPool(mats)
            if (pool.isEmpty()) pool = listOf(fallbackMat)
            val poolShuf = safeShuffle(pool, AppConstants.DAYS.indexOf(h.dias) * 100 + hi * 17)
            var pi = 0
            var tipoIdx = AppConstants.DAYS.indexOf(h.dias)
            var breaksLeft = numDescansos
            var studyCount = 0
            val totalMin = fim - ini
            val breakEvery = if (numDescansos > 0) {
                maxOf(1, floor((totalMin.toDouble() / (sessMin + pausaMin)) / (numDescansos + 1)).toInt())
            } else {
                9999
            }

            while (t + sessMin <= fim) {
                studyCount++
                if (breaksLeft > 0 && studyCount > 1 && studyCount % breakEvery == 0 &&
                    t + durDescanso + sessMin <= fim
                ) {
                    diasBlocos.getValue(h.dias).add(
                        ScheduleBlock(
                            ini = fromMin(t),
                            fim = fromMin(t + durDescanso),
                            mat = "☕ Descanso",
                            tipo = "😴 Pausa",
                            cor = "#95a5a6",
                            isBreak = true
                        )
                    )
                    t += durDescanso
                    breaksLeft--
                    continue
                }
                val raw = poolShuf[pi % poolShuf.size]
                val mat = if (raw.nome.isNotBlank()) raw else fallbackMat
                val tipo = tipos[tipoIdx % tipos.size]
                diasBlocos.getValue(h.dias).add(
                    ScheduleBlock(
                        ini = fromMin(t),
                        fim = fromMin(t + sessMin),
                        mat = mat.nome,
                        tipo = tipo,
                        cor = AppConstants.MAT_COLORS[(mat.colorIdx) % AppConstants.MAT_COLORS.size]
                    )
                )
                t += sessMin + pausaMin
                pi++
                tipoIdx++
            }
        }

        AppConstants.DAYS.forEach { d ->
            val bs = diasBlocos.getValue(d)
            if (bs.size >= 2) {
                val last = bs.last()
                bs[bs.lastIndex] = last.copy(tipo = "🔁 Revisão", isRev = true)
            }
        }

        val schedule = diasBlocos.mapValues { it.value.toList() }

        val methodsHtml = buildMethodsHtml(aprende, mats, procrastina, urgent, disciplina)
        val recommendations = buildRecommendations(celular, silencio, interrupcoes, mats)
        val restPlan = buildRestPlan(sessMin, pausaMin)
        val subtitle = if (mats.isNotEmpty()) {
            "Foco: ${mats.take(2).joinToString(" e ") { it.nome }}"
        } else {
            "Gerada com base no seu perfil"
        }

        return GenerationResult(
            schedule = schedule,
            methodsHtml = methodsHtml,
            recommendations = recommendations,
            restPlan = restPlan,
            subtitle = subtitle,
            subjectBars = subjectBars
        )
    }

    private fun getAns(answers: List<QuizAnswer>, q: Int): String =
        QuizData.getAnswerText(answers, q - 1)

    private fun buildWeightedPool(matsArr: List<SubjectDifficulty>): List<PoolItem> {
        val pool = mutableListOf<PoolItem>()
        matsArr.filter { it.nome.trim().isNotEmpty() }.forEachIndexed { i, m ->
            val slots = when {
                m.pct >= 80 -> 4
                m.pct >= 60 -> 3
                m.pct >= 40 -> 2
                else -> 1
            }
            repeat(slots) {
                pool.add(PoolItem(m.nome.trim(), m.pct, i))
            }
        }
        return pool
    }

    private fun buildMethodsHtml(
        aprende: String,
        mats: List<SubjectDifficulty>,
        procrastina: String,
        urgent: Boolean,
        disciplina: String
    ): List<String> {
        val metodos = mutableListOf<String>()
        when {
            aprende.contains("Assistindo") -> metodos.add(
                "🎥 <strong>Videoaulas primeiro.</strong> Você absorve melhor vendo. Anote enquanto assiste e resolva exercícios logo após cada vídeo para fixar o conteúdo."
            )
            aprende.contains("Lendo") -> metodos.add(
                "📖 <strong>Leitura ativa.</strong> Sublinhe, escreva à margem e faça resumos ao final de cada tópico. Use mapas mentais para conectar ideias e facilitar revisões."
            )
            aprende.contains("Praticando") -> metodos.add(
                "✍️ <strong>Prática primeiro.</strong> Resolva exercícios logo após o conteúdo. Simulados e questões comentadas são seus melhores aliados para fixação."
            )
            aprende.contains("Ouvindo") -> metodos.add(
                "🎧 <strong>Aprenda ouvindo.</strong> Audioaulas e podcasts educativos são ideais para você. Explique o conteúdo em voz alta para fixar melhor."
            )
            else -> metodos.add(
                "🔀 <strong>Método variado.</strong> Alterne leitura, vídeos e exercícios para manter o engajamento e maximizar a retenção ao longo do tempo."
            )
        }
        mats.firstOrNull()?.let { hardest ->
            metodos.add(
                "🎯 Sua matéria mais difícil é <strong>${hardest.nome}</strong>. Reserve os <em>primeiros</em> blocos de estudo do dia para ela, quando o foco está no pico."
            )
        }
        if (procrastina.contains("Sempre") || procrastina.contains("Frequente")) {
            metodos.add(
                "⚠️ <strong>Combata a procrastinação.</strong> Use a regra dos 2 minutos: se leva menos de 2 min, faça agora. Para o resto, comprometa-se com apenas 5 minutos iniciais."
            )
        }
        if (urgent) {
            metodos.add(
                "🚨 <strong>Prova próxima!</strong> Foque em revisões rápidas de pontos críticos. Priorize simulados e questões dos anos anteriores."
            )
        }
        if (disciplina.contains("Péssima") || disciplina.contains("Ruim")) {
            metodos.add(
                "📌 <strong>Construa disciplina aos poucos.</strong> Comece com metas pequenas (1 sessão/dia) e aumente gradualmente. Constância supera intensidade."
            )
        }
        return metodos
    }

    private fun buildRecommendations(
        celular: String,
        silencio: String,
        interrupcoes: String,
        mats: List<SubjectDifficulty>
    ): List<String> {
        val recs = mutableListOf<String>()
        if (celular.contains("Sempre") || celular.contains("Frequente")) {
            recs.add("📵 Coloque o celular no modo avião ou em outro cômodo durante os blocos de estudo.")
        }
        if (silencio.contains("Nunca") || silencio.contains("Raramente")) {
            recs.add("🔇 Seu ambiente tem muito ruído. Tente fones com ruído branco ou música instrumental.")
        }
        if (interrupcoes.contains("Sempre") || interrupcoes.contains("Frequente")) {
            recs.add("🚪 Combine com quem mora com você horários em que não pode ser interrompido.")
        }
        mats.firstOrNull()?.let { hardest ->
            recs.add("🎯 Nunca deixe passar um dia sem estudar <strong>${hardest.nome}</strong> — sua maior prioridade.")
        }
        recs.add("⏱️ Trate seus horários de estudo como compromissos inegociáveis na agenda.")
        return recs
    }

    private fun buildRestPlan(sessMin: Int, pausaMin: Int): List<String> = listOf(
        "⏱️ <strong>Técnica recomendada:</strong> $sessMin min de estudo → $pausaMin min de descanso (Pomodoro adaptado ao seu perfil)",
        "😴 <strong>Sono:</strong> Priorize 8–9 horas por noite. Isso melhora a consolidação de memória durante o sono REM.",
        "🍽️ <strong>Alimentação:</strong> Pause 20–30 min para refeições. Evite estudar com fome intensa ou logo após comer pesado.",
        "🌿 <strong>Recuperação mental:</strong> Reserve ao menos 1 dia por semana completamente livre de estudos.",
        "🚶 <strong>Movimento:</strong> Nas pausas, levante-se, alongue-se ou caminhe por alguns minutos. Isso reativa o foco significativamente."
    )

    private fun toMin(t: String): Int {
        val parts = t.split(":").map { it.toIntOrNull() ?: 0 }
        return parts[0] * 60 + parts[1]
    }

    private fun fromMin(m: Int): String =
        "%02d:%02d".format(m / 60, m % 60)

    private fun parseNumFromAns(txt: String, def: Int): Int {
        val match = Regex("""\d+""").find(txt)
        return match?.value?.toIntOrNull() ?: def
    }

    private fun safeShuffle(arr: List<PoolItem>, seed: Int = 0): List<PoolItem> {
        val a = arr.filter { it.nome.isNotBlank() }.map { it.copy() }.toMutableList()
        for (i in a.size - 1 downTo 1) {
            val j = floor(abs(sin((i + 1) * (seed + 1.7) * 43758.5453)) * (i + 1)).toInt()
            val tmp = a[i]
            a[i] = a[j]
            a[j] = tmp
        }
        return a
    }
}
