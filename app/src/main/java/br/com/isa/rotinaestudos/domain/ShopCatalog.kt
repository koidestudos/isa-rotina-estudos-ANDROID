package br.com.isa.rotinaestudos.domain

data class ShopItem(
    val id: String,
    val icon: String,
    val name: String,
    val desc: String,
    val price: Int,
    val cat: String,
    val slot: String,
    val rarity: String
)

data class ShopCategory(val id: String, val label: String)

object ShopCatalog {
    val categories = listOf(
        ShopCategory("all", "🛍️ Todos"),
        ShopCategory("personalize", "🎨 Personalização"),
        ShopCategory("achievements", "🏆 Conquistas"),
        ShopCategory("mascots", "🐾 Mascotes"),
        ShopCategory("accessories", "🎩 Acessórios"),
        ShopCategory("effects", "✨ Efeitos"),
        ShopCategory("titles", "📜 Títulos"),
        ShopCategory("funny", "🤖 Engraçados")
    )

    val items = listOf(
        ShopItem("theme_ocean", "🌊", "Tema Oceano", "Site em tons azul profundo", 80, "theme", "theme", "common"),
        ShopItem("theme_sunset", "🌅", "Tema Pôr do Sol", "Laranja e vermelho quentes", 80, "theme", "theme", "common"),
        ShopItem("theme_galaxy", "🌌", "Tema Galáxia", "Roxo cósmico no site", 120, "theme", "theme", "rare"),
        ShopItem("theme_rose", "🌸", "Tema Rosa", "Rosa elegante e suave", 80, "theme", "theme", "common"),
        ShopItem("theme_midnight", "🌙", "Tema Meia-Noite", "Escuro sofisticado", 100, "theme", "theme", "rare"),
        ShopItem("color_emerald", "💚", "Nome Esmeralda", "Cor verde no seu nome", 40, "namefx", "nameColor", "common"),
        ShopItem("color_gold", "💛", "Nome Dourado", "Nome brilhando em ouro", 50, "namefx", "nameColor", "rare"),
        ShopItem("color_purple", "💜", "Nome Roxo Real", "Roxo nobre no perfil", 50, "namefx", "nameColor", "rare"),
        ShopItem("color_coral", "🧡", "Nome Coral", "Tom laranja vibrante", 40, "namefx", "nameColor", "common"),
        ShopItem("color_cyan", "💙", "Nome Ciano", "Azul-turquesa refrescante", 45, "namefx", "nameColor", "common"),
        ShopItem("name_glow", "✨", "Brilho no Nome", "Seu nome brilha no perfil", 60, "namefx", "nameGlow", "rare"),
        ShopItem("name_rainbow", "🌈", "Nome Arco-íris", "Gradiente colorido no nome", 100, "namefx", "nameColor", "epic"),
        ShopItem("name_emoji_fire", "🔥", "Emoji Fogo", "Fogo ao lado do nome", 25, "namefx", "nameEmoji", "common"),
        ShopItem("name_emoji_star", "⭐", "Emoji Estrela", "Estrela ao lado do nome", 25, "namefx", "nameEmoji", "common"),
        ShopItem("name_emoji_book", "📖", "Emoji Livro", "Livro ao lado do nome", 25, "namefx", "nameEmoji", "common"),
        ShopItem("frame_gold", "🥇", "Moldura Dourada", "Borda dourada no avatar", 150, "frame", "frame", "rare"),
        ShopItem("frame_diamond", "💎", "Moldura Diamante", "Brilho azul de diamante", 200, "frame", "frame", "epic"),
        ShopItem("frame_fire", "🔥", "Moldura de Fogo", "Chamas ao redor do avatar", 180, "frame", "frame", "epic"),
        ShopItem("frame_rainbow", "🌈", "Moldura Arco-íris", "Borda multicolorida", 250, "frame", "frame", "legend"),
        ShopItem("frame_legend", "👑", "Moldura Lendária", "A mais rara de todas", 350, "frame", "frame", "legend"),
        ShopItem("icon_star", "⭐", "Ícone Estrela", "Estrela no banner", 30, "icon", "profileIcon", "common"),
        ShopItem("icon_book", "📚", "Ícone Livro", "Livros no banner", 30, "icon", "profileIcon", "common"),
        ShopItem("icon_brain", "🧠", "Ícone Cérebro", "Inteligência no banner", 40, "icon", "profileIcon", "common"),
        ShopItem("icon_rocket", "🚀", "Ícone Foguete", "Decolando nos estudos", 45, "icon", "profileIcon", "rare"),
        ShopItem("icon_crown", "👑", "Ícone Coroa", "Realeza no banner", 60, "icon", "profileIcon", "rare"),
        ShopItem("bg_stars", "✨", "Fundo Estrelado", "Painel com estrelas", 90, "bg", "panelBg", "rare"),
        ShopItem("bg_forest", "🌲", "Fundo Floresta", "Verde natureza no painel", 90, "bg", "panelBg", "rare"),
        ShopItem("bg_sunset", "🌇", "Fundo Entardecer", "Pôr do sol no painel", 100, "bg", "panelBg", "rare"),
        ShopItem("bg_galaxy", "🌀", "Fundo Galáxia", "Cosmos no painel", 120, "bg", "panelBg", "epic"),
        ShopItem("bg_candy", "🍬", "Fundo Doce", "Rosa doce no painel", 80, "bg", "panelBg", "common"),
        ShopItem("banner_ocean", "🌊", "Banner Oceano", "Banner azul marinho", 75, "banner", "banner", "common"),
        ShopItem("banner_sunset", "🌅", "Banner Entardecer", "Banner laranja e vermelho", 75, "banner", "banner", "common"),
        ShopItem("banner_galaxy", "🌌", "Banner Galáxia", "Banner cósmico", 100, "banner", "banner", "rare"),
        ShopItem("banner_forest", "🌲", "Banner Floresta", "Banner verde natural", 75, "banner", "banner", "common"),
        ShopItem("banner_gold", "✨", "Banner Dourado", "Banner luxuoso em ouro", 120, "banner", "banner", "epic"),
        ShopItem("title_novato", "🌱", "Título: Novato", "Começando a jornada", 15, "title", "title", "common"),
        ShopItem("title_estudante", "📖", "Título: Estudante", "No caminho certo", 25, "title", "title", "common"),
        ShopItem("title_dedicado", "💪", "Título: Dedicado", "Esforço reconhecido", 50, "title", "title", "common"),
        ShopItem("title_genio", "🧠", "Título: Gênio", "Mente brilhante", 100, "title", "title", "rare"),
        ShopItem("title_mestre", "🎓", "Título: Mestre dos Estudos", "Domínio total", 150, "title", "title", "epic"),
        ShopItem("title_mat", "➗", "Título: Matemático", "Rei dos números", 80, "title", "title", "rare"),
        ShopItem("title_cient", "🔬", "Título: Cientista", "Laboratório na mente", 80, "title", "title", "rare"),
        ShopItem("title_leitor", "📚", "Título: Leitor", "Devorador de livros", 60, "title", "title", "common"),
        ShopItem("title_lenda", "⚡", "Título: Lenda", "Status lendário", 300, "title", "title", "legend"),
        ShopItem("title_imperador", "👑", "Título: Imperador do Conhecimento", "O ápice dos estudos", 500, "title", "title", "legend"),
        ShopItem("badge_streak", "🔥", "Distintivo Sequência", "Sequência de estudos forte", 70, "badge", "badges", "rare"),
        ShopItem("badge_coins", "🪙", "Distintivo Colecionador", "Amante de moedas", 70, "badge", "badges", "rare"),
        ShopItem("badge_early", "🌅", "Distintivo Madrugador", "Estuda cedo", 55, "badge", "badges", "common"),
        ShopItem("badge_night", "🌙", "Distintivo Coruja", "Estuda à noite", 55, "badge", "badges", "common"),
        ShopItem("badge_master", "🏅", "Distintivo Mestre", "Excelência comprovada", 90, "badge", "badges", "epic"),
        ShopItem("trophy_bronze", "🥉", "Troféu Bronze", "Primeira conquista", 100, "trophy", "trophy", "common"),
        ShopItem("trophy_silver", "🥈", "Troféu Prata", "Segundo lugar no coração", 180, "trophy", "trophy", "rare"),
        ShopItem("trophy_gold", "🥇", "Troféu Ouro", "Campeão dos estudos", 300, "trophy", "trophy", "legend"),
        ShopItem("medal_rare", "🏅", "Medalha Rara", "Conquista especial", 120, "medal", "medal", "rare"),
        ShopItem("medal_epic", "🎖️", "Medalha Épica", "Feito memorável", 200, "medal", "medal", "epic"),
        ShopItem("medal_legend", "🏆", "Medalha Lendária", "Ultra rara", 400, "medal", "medal", "legend"),
        ShopItem("mascot_dog", "🐕", "Mascote Cachorro", "Fiel companheiro de estudos", 150, "mascot", "mascot", "common"),
        ShopItem("mascot_cat", "🐱", "Mascote Gato", "Ronrona enquanto você estuda", 150, "mascot", "mascot", "common"),
        ShopItem("mascot_owl", "🦉", "Coruja Estudiosa", "Sabedoria noturna", 180, "mascot", "mascot", "rare"),
        ShopItem("mascot_fox", "🦊", "Mascote Raposa", "Esperta e focada", 180, "mascot", "mascot", "rare"),
        ShopItem("mascot_dragon", "🐉", "Mascote Dragão", "Poder lendário", 350, "mascot", "mascot", "legend"),
        ShopItem("mascot_penguin", "🐧", "Mascote Pinguim", "Frio na hora H", 160, "mascot", "mascot", "common"),
        ShopItem("mascot_axolotl", "🦎", "Mascote Axolote", "Fofo e raro", 200, "mascot", "mascot", "epic"),
        ShopItem("mascot_capibara", "🦫", "Mascote Capivara", "Mais calmo do reino", 220, "mascot", "mascot", "epic"),
        ShopItem("mascot_ghost", "👻", "Mascote Fantasma", "Assombra as provas", 250, "mascot", "mascot", "epic"),
        ShopItem("mascot_robot", "🤖", "Mascote Robô", "Estudos automatizados", 280, "mascot", "mascot", "legend"),
        ShopItem("cap_grad", "🎓", "Chapéu de Formando", "Celebre a formatura!", 20, "accessory", "accessories", "common"),
        ShopItem("acc_hat", "🎩", "Chapéu", "Classe no avatar", 35, "accessory", "accessories", "common"),
        ShopItem("acc_glasses", "👓", "Óculos", "Visão de estudante", 40, "accessory", "accessories", "common"),
        ShopItem("acc_backpack", "🎒", "Mochila", "Material sempre à mão", 55, "accessory", "accessories", "common"),
        ShopItem("acc_tie", "👔", "Gravata", "Formal nos estudos", 45, "accessory", "accessories", "common"),
        ShopItem("acc_crown", "👑", "Coroa", "Rei da sala", 120, "accessory", "accessories", "epic"),
        ShopItem("acc_wing", "🪽", "Asa", "Voando nos conteúdos", 150, "accessory", "accessories", "epic"),
        ShopItem("acc_sword", "⚔️", "Espada de Brinquedo", "Batalha contra a prova", 80, "accessory", "accessories", "rare"),
        ShopItem("acc_headphone", "🎧", "Fone de Ouvido", "Foco total", 65, "accessory", "accessories", "common"),
        ShopItem("fx_rain", "🌧️", "Efeito Chuva", "Chuva no perfil", 90, "effect", "effect", "rare"),
        ShopItem("fx_snow", "❄️", "Efeito Neve", "Flocos caindo", 90, "effect", "effect", "rare"),
        ShopItem("fx_fireworks", "🎆", "Efeito Fogos de Artifício", "Celebração no perfil", 120, "effect", "effect", "epic"),
        ShopItem("fx_stars", "⭐", "Efeito Estrelas", "Estrelas brilhantes", 70, "effect", "effect", "common"),
        ShopItem("fx_lightning", "⚡", "Efeito Raios", "Energia elétrica", 110, "effect", "effect", "epic"),
        ShopItem("fx_aura", "💫", "Aura Colorida", "Brilho ao redor do painel", 130, "effect", "effect", "epic"),
        ShopItem("fx_particles", "✨", "Partículas", "Partículas mágicas", 85, "effect", "effect", "common"),
        ShopItem("funny_banana", "🍌", "Banana Dourada", "Item místico e engraçado", 45, "funny", "funny", "common"),
        ShopItem("funny_duck", "🦆", "Patinho de Borracha", "Quack nos estudos", 40, "funny", "funny", "common"),
        ShopItem("funny_king_capy", "👑", "Capivara Rei", "Majestade aquática", 150, "funny", "funny", "epic"),
        ShopItem("funny_book", "📕", "Livro Infinito", "Nunca acaba de ler", 80, "funny", "funny", "rare"),
        ShopItem("funny_paper_crown", "📜", "Coroa de Papel", "Rei improvisado", 30, "funny", "funny", "common"),
        ShopItem("funny_calc", "🔢", "Calculadora Lendária", "Conta tudo", 95, "funny", "funny", "rare"),
        ShopItem("funny_eraser", "🧹", "Borracha Mágica", "Apaga qualquer erro", 50, "funny", "funny", "common"),
        ShopItem("funny_pencil", "✏️", "Lápis Supremo", "Escreve a história", 55, "funny", "funny", "common")
    )

    val byId: Map<String, ShopItem> = items.associateBy { it.id }

    fun matchesFilter(item: ShopItem, filter: String): Boolean = when (filter) {
        "all" -> true
        "personalize" -> item.cat in listOf("theme", "namefx", "banner", "frame", "icon", "bg")
        "achievements" -> item.cat in listOf("badge", "trophy", "medal")
        "mascots" -> item.cat == "mascot"
        "accessories" -> item.cat == "accessory"
        "effects" -> item.cat == "effect"
        "titles" -> item.cat == "title"
        "funny" -> item.cat == "funny"
        else -> true
    }

    fun rarityLabel(r: String) = when (r) {
        "legend" -> "Lendário"
        "epic" -> "Épico"
        "rare" -> "Raro"
        else -> "Comum"
    }

    fun coinsFor(streak: Int) = maxOf(2, streak * 2)
}
