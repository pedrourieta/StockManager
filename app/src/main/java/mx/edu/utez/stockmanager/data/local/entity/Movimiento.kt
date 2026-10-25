package mx.edu.utez.stockmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoMovimiento { ENTRADA, SALIDA }

/**
 * Registro de entrada o salida de mercancía capturado sin conexión.
 * - La llave foránea garantiza que no existan movimientos de productos inexistentes.
 * - Los índices aceleran las consultas por producto, por estado de sincronización
 *   (índice compuesto sincronizado + fecha) y por fecha.
 */
@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["codigo"],
            childColumns = ["codigoProducto"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("codigoProducto"),
        Index("sincronizado", "fecha"),
        Index("fecha")
    ]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoProducto: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false
)
