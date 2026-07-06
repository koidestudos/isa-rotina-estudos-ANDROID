package br.com.isa.rotinaestudos.domain

import br.com.isa.rotinaestudos.data.model.QuizAnswer
import br.com.isa.rotinaestudos.data.model.SubjectDifficulty
import br.com.isa.rotinaestudos.data.model.TimeSlot

data class QuizQuestion(
    val cat: String,
    val q: String,
    val opts: List<String>? = null,
    val special: String? = null
)

object QuizData {
    const val TOTAL_Q = 26

    val TEMPLATES = mapOf(
        "1º Ensino Médio" to listOf(
            "Física I", "Física II", "Matemática I", "Matemática II", "Matemática III",
            "Português/Gramática", "Interpretação de Texto", "Literatura", "Redação",
            "Biologia I", "Biologia II", "Química I", "Química II", "História",
            "Geografia", "Filosofia", "Sociologia", "Arte", "Inglês"
        ),
        "6º ao 8º Ano" to listOf(
            "Matemática", "Português", "Redação", "História", "Geografia",
            "Ciências", "Inglês", "Arte", "Desenho Geométrico", "Ensino Religioso", "Cultura Maker"
        ),
        "1º ao 5º Ano" to listOf(
            "Matemática", "Português", "Redação", "História", "Geografia",
            "Ciências", "Inglês", "Arte", "Gramática", "Ensino Religioso"
        )
    )

    val QUESTIONS = listOf(
        QuizQuestion("🎯 Objetivos", "Qual é o seu principal objetivo?", listOf("Melhorar notas", "Passar de ano", "Preparar-se para vestibular/ENEM", "Aprender por interesse próprio", "Outro")),
        QuizQuestion("🎯 Objetivos", "Como você avalia seu desempenho atual?", listOf("Muito ruim", "Ruim", "Regular", "Bom", "Excelente")),
        QuizQuestion("🎯 Objetivos", "Você possui alguma prova importante próxima?", listOf("Não", "Em mais de 3 meses", "Em 1–3 meses", "Em 2–4 semanas", "Em menos de 2 semanas")),
        QuizQuestion("🎯 Objetivos", "Qual nota média deseja alcançar?", listOf("6", "7", "8", "9", "10")),
        QuizQuestion("🎯 Objetivos", "Quanto você está disposto a se dedicar?", listOf("Muito pouco", "Pouco", "Moderadamente", "Bastante", "Totalmente")),
        QuizQuestion("⏰ Tempo Disponível", "Em quais horários você está livre para estudar?", special = "horarios"),
        QuizQuestion("⏰ Tempo Disponível", "Em qual período você rende mais?", listOf("Madrugada", "Manhã", "Tarde", "Noite", "Igual em todos")),
        QuizQuestion("⏰ Tempo Disponível", "Quanto tempo consegue focar sem pausa?", listOf("Menos de 15 min", "15–25 min", "25–40 min", "40–60 min", "Mais de 60 min")),
        QuizQuestion("⏰ Tempo Disponível", "Quantos dias por semana pode estudar?", listOf("1–2 dias", "3 dias", "4–5 dias", "6 dias", "Todos os dias")),
        QuizQuestion("⏰ Tempo Disponível", "Quantos descansos planeja ter durante a rotina?", listOf("A — 1 descanso", "B — 2 descansos", "C — 3 descansos", "D — 4 descansos", "E — 5 descansos")),
        QuizQuestion("⏰ Tempo Disponível", "Quanto tempo dura cada descanso?", listOf("5 minutos", "10 minutos", "15 minutos", "20 minutos", "30 minutos")),
        QuizQuestion("🏠 Ambiente", "Onde costuma estudar?", listOf("Quarto", "Sala", "Biblioteca", "Escola", "Outro lugar")),
        QuizQuestion("🏠 Ambiente", "O ambiente é silencioso?", listOf("Nunca", "Raramente", "Às vezes", "Frequentemente", "Sempre")),
        QuizQuestion("🏠 Ambiente", "É interrompido com frequência?", listOf("Sempre", "Frequentemente", "Às vezes", "Raramente", "Nunca")),
        QuizQuestion("🏠 Ambiente", "Usa celular durante os estudos?", listOf("Sempre", "Frequentemente", "Às vezes", "Raramente", "Nunca")),
        QuizQuestion("🏠 Ambiente", "Seu local de estudo ajuda na concentração?", listOf("Nunca", "Raramente", "Às vezes", "Frequentemente", "Sempre")),
        QuizQuestion("📚 Métodos de Estudo", "Você aprende melhor:", listOf("Lendo", "Ouvindo", "Assistindo vídeos", "Praticando exercícios", "Misturando métodos")),
        QuizQuestion("📚 Métodos de Estudo", "Faz revisões do conteúdo?", listOf("Nunca", "Raramente", "Às vezes", "Frequentemente", "Sempre")),
        QuizQuestion("📚 Métodos de Estudo", "Resolve exercícios após estudar?", listOf("Nunca", "Raramente", "Às vezes", "Frequentemente", "Sempre")),
        QuizQuestion("📚 Métodos de Estudo", "Qual seu método favorito?", listOf("Resumos", "Exercícios", "Videoaulas", "Flashcards", "Mapas mentais")),
        QuizQuestion("📚 Métodos de Estudo", "Assiste videoaulas?", listOf("Nunca", "Raramente", "Às vezes", "Frequentemente", "Sempre")),
        QuizQuestion("💪 Hábitos", "Você procrastina (adia o estudo)?", listOf("Sempre", "Frequentemente", "Às vezes", "Raramente", "Nunca")),
        QuizQuestion("💪 Hábitos", "Como está sua disciplina?", listOf("Péssima", "Ruim", "Regular", "Boa", "Excelente")),
        QuizQuestion("💪 Hábitos", "Como está sua motivação?", listOf("Muito baixa", "Baixa", "Média", "Alta", "Muito alta")),
        QuizQuestion("💪 Hábitos", "Quando perde um dia de estudos, você:", listOf("Desiste da rotina", "Demora para voltar", "Volta no dia seguinte", "Reorganiza rapidamente", "Continua normalmente")),
        QuizQuestion("💪 Hábitos", "Quais matérias você possui dificuldade e qual o nível?", special = "materias")
    )

    fun getAnswerText(answers: List<QuizAnswer>, qIndex: Int): String {
        val a = answers.find { it.q == qIndex + 1 } ?: return ""
        return a.txt ?: a.ans ?: ""
    }

    fun getHorarios(answers: List<QuizAnswer>): List<TimeSlot> =
        answers.find { it.special == "horarios" }?.horarios ?: emptyList()

    fun getMaterias(answers: List<QuizAnswer>): List<SubjectDifficulty> =
        answers.find { it.special == "materias" }?.materias?.filter { it.nome.isNotBlank() } ?: emptyList()
}
