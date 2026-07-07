package br.com.isa.rotinaestudos

import android.app.Application
import br.com.isa.rotinaestudos.data.local.IsaPreferences
import com.google.firebase.FirebaseApp

class IsaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        IsaPreferences.init(this)
    }
}
