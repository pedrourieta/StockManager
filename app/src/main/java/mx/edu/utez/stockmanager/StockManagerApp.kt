package mx.edu.utez.stockmanager

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import mx.edu.utez.stockmanager.data.local.StockDatabase
import mx.edu.utez.stockmanager.data.remote.ServidorCentralSimulado
import mx.edu.utez.stockmanager.data.repository.InventarioRepository

/** Crea una sola vez la base de datos y el repositorio para toda la aplicación. */
class StockManagerApp : Application() {

    val repositorio by lazy {
        InventarioRepository(StockDatabase.obtener(this), ServidorCentralSimulado())
    }

    /** Indica si el teléfono tiene internet en este momento. */
    fun hayConexion(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java)
        return cm.getNetworkCapabilities(cm.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
