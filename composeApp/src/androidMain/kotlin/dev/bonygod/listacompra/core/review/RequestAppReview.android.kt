package dev.bonygod.listacompra.core.review

import android.app.Activity
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import dev.bonygod.listacompra.appContext
import kotlinx.coroutines.CancellationException
import java.lang.ref.WeakReference

private var activityActual: WeakReference<Activity>? = null

// launchReviewFlow exige una Activity; appContext no lo es.
internal fun registrarActivityParaResena(activity: Activity?) {
    activityActual = activity?.let { WeakReference(it) }
}

actual suspend fun requestAppReview() {
    try {
        val activity = activityActual?.get() ?: return
        val manager = ReviewManagerFactory.create(appContext)
        val info = manager.requestReview()
        manager.launchReview(activity, info)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Cosmético: un fallo aquí no puede afectar a la pantalla.
    }
}
