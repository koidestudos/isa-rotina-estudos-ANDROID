package br.com.isa.rotinaestudos.domain

data class DicaCategory(val icon: String, val name: String, val tips: List<String>)

object DicasData {
    val categories = listOf(
        DicaCategory("📋", "Organização", listOf(
            "Use listas de tarefas diárias com prioridades.",
            "Separe o material antes de começar a estudar.",
            "Organize seu espaço físico antes de sentar.",
            "Planeje a semana toda no domingo."
        )),
        DicaCategory("😴", "Sono", listOf(
            "Dormir 8–9 horas melhora a retenção de memória.",
            "Evite telas 1 hora antes de dormir.",
            "Horários regulares de sono aumentam produtividade.",
            "Cochilos de 20 min recarregam a energia."
        )),
        DicaCategory("⚡", "Produtividade", listOf(
            "Use Pomodoro: 25 min estudo + 5 min pausa.",
            "Comece sempre pela tarefa mais difícil.",
            "Defina 3 metas por sessão de estudo.",
            "Evite multitarefa durante os estudos."
        )),
        DicaCategory("🎯", "Concentração", listOf(
            "Coloque o celular no modo avião ao estudar.",
            "Use fones com ruído branco se houver barulho.",
            "Beba água regularmente para manter o foco.",
            "Feche abas desnecessárias no computador."
        )),
        DicaCategory("⏰", "Gestão do Tempo", listOf(
            "Divida tarefas grandes em partes menores.",
            "Use alarmes para iniciar e encerrar sessões.",
            "Faça revisões periódicas para não esquecer.",
            "Respeite seus horários livres como compromissos."
        )),
        DicaCategory("🌿", "Tempo Livre", listOf(
            "Descanso é parte do aprendizado, não desperdício.",
            "Pratique atividades físicas nos intervalos.",
            "Evite estudar 7 dias sem pausa alguma.",
            "Socialize — reduz estresse e melhora o foco."
        )),
        DicaCategory("🔁", "Revisão", listOf(
            "Revise 1 dia, 7 dias e 30 dias após estudar.",
            "Flashcards são ótimos para revisão rápida.",
            "Explicar o conteúdo para alguém fixa melhor.",
            "Simulados revisam vários assuntos de uma vez."
        )),
        DicaCategory("💪", "Motivação", listOf(
            "Comemore pequenas conquistas no caminho.",
            "Visualize seu objetivo final todos os dias.",
            "Defina recompensas ao cumprir metas semanais.",
            "Encontre um parceiro de estudos."
        ))
    )

    fun coinsFor(streak: Int) = maxOf(2, streak * 2)
}
