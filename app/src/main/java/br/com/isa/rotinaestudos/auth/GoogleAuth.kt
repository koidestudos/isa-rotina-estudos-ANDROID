package br.com.isa.rotinaestudos.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import br.com.isa.rotinaestudos.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleAuth {

    private const val TAG = "GoogleAuth"

    suspend fun signIn(context: Context): Result<String> {
        val activity = context.findActivity()
        val webClientId = activity.getString(R.string.default_web_client_id)
        val manager = CredentialManager.create(activity)
        Log.d(TAG, "Iniciando login Google (webClientId=${webClientId.take(20)}...)")

        // 1) Fluxo do botão "Continuar com Google" — abre o seletor de contas
        try {
            return requestCredential(
                manager = manager,
                activity = activity,
                label = "SignInWithGoogle",
                request = buildSignInWithGoogleRequest(webClientId)
            )
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Login Google cancelado pelo usuário")
            return Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "SignInWithGoogle sem credencial, tentando GetGoogleIdOption", e)
        } catch (e: Exception) {
            Log.e(TAG, "Falha no SignInWithGoogle", e)
            return Result.failure(e)
        }

        // 2) Fallback: contas já usadas no app (one-tap)
        try {
            return requestCredential(
                manager = manager,
                activity = activity,
                label = "GoogleId-authorized",
                request = buildGoogleIdRequest(webClientId, filterAuthorized = true)
            )
        } catch (e: GetCredentialCancellationException) {
            return Result.failure(e)
        } catch (e: NoCredentialException) {
            Log.w(TAG, "Sem conta autorizada, tentando todas as contas Google", e)
        } catch (e: Exception) {
            Log.e(TAG, "Falha no GoogleId autorizado", e)
            return Result.failure(e)
        }

        // 3) Fallback: qualquer conta Google no aparelho
        return try {
            requestCredential(
                manager = manager,
                activity = activity,
                label = "GoogleId-all",
                request = buildGoogleIdRequest(webClientId, filterAuthorized = false)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Falha em todos os fluxos Google", e)
            Result.failure(e)
        }
    }

    private suspend fun requestCredential(
        manager: CredentialManager,
        activity: Activity,
        label: String,
        request: GetCredentialRequest
    ): Result<String> {
        val response = manager.getCredential(request = request, context = activity)
        val token = parseIdToken(response.credential)
        Log.d(TAG, "Token Google obtido ($label)")
        return Result.success(token)
    }

    private fun buildSignInWithGoogleRequest(webClientId: String): GetCredentialRequest {
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    private fun buildGoogleIdRequest(webClientId: String, filterAuthorized: Boolean): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterAuthorized)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    fun errorMessage(error: Throwable): String = when (error) {
        is GetCredentialCancellationException -> ""
        is NoCredentialException ->
            "Não foi possível abrir o login Google. Verifique: (1) conta Google no aparelho, " +
                "(2) em Configurações da conta Google → 'Entrar com Google' está ativo, " +
                "(3) se instalou o APK pelo GitHub Actions, adicione o SHA-1 de debug do CI no Firebase."
        is GetCredentialProviderConfigurationException ->
            "Google Play Services não está disponível. Use um celular físico ou emulador com Play Store."
        is GetCredentialException -> {
            val code = error.type
            val detail = error.message?.takeIf { it.isNotBlank() }
            when {
                code.contains("developer", ignoreCase = true) ||
                    code.contains("config", ignoreCase = true) ||
                    detail?.contains("10:", ignoreCase = true) == true ->
                    "Configuração Google incorreta. No Firebase Console, adicione o SHA-1 do APK instalado " +
                        "(Android Studio: Gradle → signingReport) e baixe o google-services.json atualizado."
                detail != null -> "Google: $detail"
                else -> "Erro Google ($code). Tente de novo."
            }
        }
        is IllegalStateException ->
            error.message ?: "Tela de login inválida. Feche e abra o app novamente."
        else -> {
            val detail = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
            "Erro ao entrar com Google: $detail"
        }
    }

    private fun parseIdToken(credential: Credential): String {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        error("Credencial Google inválida (${credential::class.simpleName}).")
    }

    suspend fun signOut(context: Context) {
        runCatching {
            val activity = context.findActivity()
            CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private tailrec fun Context.findActivity(): Activity {
        return when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> error("Context não é uma Activity — não é possível abrir o login Google.")
        }
    }
}
