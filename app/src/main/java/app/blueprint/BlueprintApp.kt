package app.blueprint

import android.app.Application
import app.blueprint.data.Repository

class BlueprintApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.init(this)
        Repository.onChanged = { ctx -> AppSync.onDataChanged(ctx) }
    }
}
