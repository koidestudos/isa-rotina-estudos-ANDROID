# ISA Rotina de Estudos — Android

Aplicativo **nativo** Android (Kotlin + Jetpack Compose + Firebase) do projeto [isa-rotina-estudos](https://github.com/koidestudos/isa-rotina-estudos).

> Não é WebView. UI nativa Material 3, com o mesmo backend Firebase (Auth + Firestore).

## Funcionalidades

| Recurso | Status |
|---------|--------|
| Login / registro (e-mail) | ✅ |
| Quiz de 26 perguntas + geração de rotina | ✅ |
| Cronograma semanal, moedas e streak | ✅ |
| Como estudar, descanso, recomendações | ✅ |
| Calendário escolar ISA 2026 + anotações | ✅ |
| Avisos, ranking, perfil, série escolar | ✅ |
| Admin — eventos (admin + escolar) | ✅ |
| Flashcards (visualizar conjuntos da nuvem) | ✅ parcial |
| UI profissional (tema web, abas, FABs) | ✅ |
| Chat, loja, timer estudando | 🔜 próximas versões |

## Publicar no GitHub

O código do app está na **raiz** deste repositório (`isa-rotina-estudos-ANDROID`).

Para enviar ao GitHub:
```bash
git add .
git commit -m "Adiciona app Android nativo ISA Rotina de Estudos"
git push origin main
```

Ou use o script (Linux/macOS/Git Bash):
```bash
chmod +x push-android-repo.sh
./push-android-repo.sh
```

## Requisitos

- Android Studio Ladybug ou newer
- JDK 17
- Conta Firebase `isa-estudos`

## Configuração Firebase

1. Abra [Firebase Console](https://console.firebase.google.com/) → projeto **isa-estudos**
2. Adicione app **Android** com package `br.com.isa.rotinaestudos`
3. Baixe `google-services.json` e substitua `app/google-services.json`
4. Ative **Authentication** → E-mail/senha (e Google se quiser depois)
5. Firestore rules iguais ao projeto web (`firestore.rules` no repo web)

## Build local

```bash
chmod +x gradlew
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Release assinado:

```bash
./gradlew assembleRelease
```

## CI

Cada push na `main` gera APK debug como artefato em **Actions → Build Android APK**.

## Estrutura

```
app/src/main/java/br/com/isa/rotinaestudos/
├── domain/          # Quiz, gerador de rotina, calendário 2026
├── data/            # Modelos + repositórios Firestore
├── ui/              # ViewModel + telas Compose
└── MainActivity.kt
```

## Repositórios relacionados

- Web: https://github.com/koidestudos/isa-rotina-estudos
- Android: https://github.com/koidestudos/isa-rotina-estudos-ANDROID

## Licença

Mesmo projeto ISA — uso escolar.
