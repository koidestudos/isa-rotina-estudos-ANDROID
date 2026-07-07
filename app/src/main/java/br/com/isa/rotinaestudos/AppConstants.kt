package br.com.isa.rotinaestudos

object AppConstants {
    val ADMIN_EMAILS = listOf(
        "tiagogamerplayer133@gmail.com",
        "prefeito964@gmail.com"
    )

    val SERIES_OPTIONS = listOf(
        "1º Ano", "2º Ano", "3º Ano", "4º Ano", "5º Ano", "6º Ano",
        "7º Ano", "8º Ano", "9º Ano", "1º EM", "2º EM", "3º EM"
    )

    const val CLASSAPP_PACKAGE = "br.com.classapp.ClassApp"

    val DAYS = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")

    val MAT_COLORS = listOf(
        "#e74c3c", "#e67e22", "#3498db", "#9b59b6", "#1abc9c",
        "#e91e63", "#ff5722", "#607d8b", "#16a085", "#8e44ad",
        "#c0392b", "#27ae60", "#2980b9", "#d35400", "#7f8c8d"
    )

    val CAL_EVENT_LABELS = mapOf(
        "inicio_1" to "Início 1º Semestre",
        "inicio_2" to "Início 2º Semestre",
        "avaliacao" to "Avaliação",
        "simulado_isa" to "Simulado ISA",
        "simulado_bern" to "Simulado Bernoulli",
        "plantao" to "Plantão Escolar",
        "prova_final" to "Prova Final",
        "recuperacao" to "Aulas de Recuperação",
        "av_recuperacao" to "Av. de Recuperação",
        "resultado" to "Resultado",
        "ferias" to "Férias",
        "feriado" to "Feriado",
        "aulas" to "Aulas"
    )

    val CAL_MES_CURTO = listOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )

    fun isAdminEmail(email: String?) =
        email?.lowercase()?.let { e -> ADMIN_EMAILS.any { it.equals(e, ignoreCase = true) } } == true
}
