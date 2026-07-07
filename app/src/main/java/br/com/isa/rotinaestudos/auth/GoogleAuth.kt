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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleAuth {

    private const val TAG = "GoogleAuth"

    suspend fun signIn(context: Context): Result<String> = try {
        val activity = context.findActivity()
        val webClientId = activity.getString(R.string.default_web_client_id)
        Log.d(TAG, "Iniciando login Google (webClientId=${webClientId.take(20)}...)")

        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val manager = CredentialManager.create(activity)
        val response = manager.getCredential(request = request, context = activity)
        val token = parseIdToken(response.credential)
        Log.d(TAG, "Token Google obtido com sucesso")
        Result.success(token)
    } catch (e: GetCredentialCancellationException) {
        Log.d(TAG, "Login Google cancelado pelo usuário")
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "Falha no login Google", e)
        Result.failure(e)
    }

    fun errorMessage(error: Throwable): String = when (error) {
        is GetCredentialCancellationException -> ""
        is NoCredentialException ->
            "Nenhuma conta Google disponível. Adicione uma conta Google no aparelho e tente de novo."
        is GetCredentialProviderConfigurationException ->
            "Google Play Services não está disponível. Use um celular físico ou emulador com Play Store."
        is GetCredentialException -> {
            val code = error.type
            val detail = error.message?.takeIf { it.isNotBlank() }
            when {
                code.contains("developer", ignoreCase = true) || code.contains("config", ignoreCase = true) ->
                    "Configuração Google incorreta. Verifique SHA-1 no Firebase e o google-services.json."
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
