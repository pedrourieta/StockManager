package mx.edu.utez.stockmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mx.edu.utez.stockmanager.data.local.dao.MovimientoDao
import mx.edu.utez.stockmanager.data.local.dao.ProductoDao
import mx.edu.utez.stockmanager.data.local.entity.Movimiento
import mx.edu.utez.stockmanager.data.local.entity.Producto

@Database(entities = [Producto::class, Movimiento::class], version = 1, exportSchema = true)
@TypeConverters(Convertidores::class)
abstract class StockDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        const val NOMBRE = "stockmanager.db"

        @Volatile
        private var instancia: StockDatabase? = null

        /** Singleton: una sola conexión abierta para toda la aplicación. */
        fun obtener(context: Context): StockDatabase =
            instancia ?: synchronized(this) {
                instancia ?: construir(context).also { instancia = it }
            }

        fun construir(context: Context, nombre: String = NOMBRE): StockDatabase =
            Room.databaseBuilder(context.applicationContext, StockDatabase::class.java, nombre)
                // WAL permite leer mientras se escribe (lista + sincronización simultáneas)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
    }
}
