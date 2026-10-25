package mx.edu.utez.stockmanager.data.local.model

import mx.edu.utez.stockmanager.data.local.entity.TipoMovimiento

/** Proyección: solo las columnas que necesita la pantalla de existencias. */
data class ExistenciaProducto(
    val codigo: String,
    val nombre: String,
    val existencia: Int
)

/** Proyección del movimiento con el nombre del producto (JOIN). */
data class MovimientoDetalle(
    val id: Long,
    val codigoProducto: String,
    val nombre: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long,
    val sincronizado: Boolean
)
