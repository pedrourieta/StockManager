package mx.edu.utez.stockmanager.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.stockmanager.data.local.StockDatabase
import mx.edu.utez.stockmanager.data.local.entity.Movimiento
import mx.edu.utez.stockmanager.data.local.entity.Producto
import mx.edu.utez.stockmanager.data.local.entity.TipoMovimiento
import mx.edu.utez.stockmanager.data.local.model.ExistenciaProducto
import mx.edu.utez.stockmanager.data.local.model.MovimientoDetalle
import mx.edu.utez.stockmanager.data.remote.ServidorCentralApi

class InventarioException(mensaje: String) : Exception(mensaje)

/**
 * Punto único de acceso a los datos. Aplica las reglas de integridad del negocio
 * y coordina la base local con el servidor central.
 */
class InventarioRepository(
    private val db: StockDatabase,
    private val api: ServidorCentralApi
) {
    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoDao()

    val existencias: Flow<List<ExistenciaProducto>> = productoDao.observarExistencias()
    val movimientos: Flow<List<MovimientoDetalle>> = movimientoDao.observarRecientes()
    val pendientes: Flow<Int> = movimientoDao.observarTotalPendientes()

    /** CREATE: registra el movimiento; da de alta el producto si es nuevo. */
    suspend fun registrarMovimiento(
        codigo: String, nombre: String, tipo: TipoMovimiento, cantidad: Int
    ): Long {
        val cod = codigo.trim().uppercase()
        validarDatos(cod, cantidad)
        // Transacción: la validación de existencia y la inserción ocurren de forma atómica
        return db.withTransaction {
            val producto = productoDao.buscarPorCodigo(cod)
            if (producto == null) {
                if (nombre.isBlank()) throw InventarioException("Producto nuevo: escribe su nombre")
                productoDao.insertar(Producto(cod, nombre.trim()))
            }
            if (tipo == TipoMovimiento.SALIDA && movimientoDao.existenciaDe(cod) < cantidad) {
                throw InventarioException("Existencia insuficiente para la salida")
            }
            movimientoDao.insertar(Movimiento(codigoProducto = cod, tipo = tipo, cantidad = cantidad))
        }
    }

    /** UPDATE: solo se editan movimientos que aún no llegan al servidor. */
    suspend fun actualizarMovimiento(id: Long, tipo: TipoMovimiento, cantidad: Int) {
        db.withTransaction {
            val actual = movimientoDao.buscarPorId(id) ?: throw InventarioException("No existe el movimiento")
            if (actual.sincronizado) throw InventarioException("Ya sincronizado: no se puede editar")
            validarDatos(actual.codigoProducto, cantidad)
            // Existencia sin contar el movimiento que se está editando
            val efectoActual = if (actual.tipo == TipoMovimiento.ENTRADA) actual.cantidad else -actual.cantidad
            val base = movimientoDao.existenciaDe(actual.codigoProducto) - efectoActual
            if (tipo == TipoMovimiento.SALIDA && base < cantidad) {
                throw InventarioException("Existencia insuficiente para la salida")
            }
            movimientoDao.actualizar(actual.copy(tipo = tipo, cantidad = cantidad))
        }
    }

    /** DELETE: solo movimientos pendientes y sin dejar existencias negativas. */
    suspend fun eliminarMovimiento(id: Long) {
        db.withTransaction {
            val actual = movimientoDao.buscarPorId(id) ?: return@withTransaction
            if (actual.sincronizado) throw InventarioException("Ya sincronizado: no se puede eliminar")
            if (actual.tipo == TipoMovimiento.ENTRADA &&
                movimientoDao.existenciaDe(actual.codigoProducto) < actual.cantidad
            ) throw InventarioException("La existencia quedaría negativa")
            movimientoDao.eliminar(actual)
        }
    }

    /** Envía los pendientes por lotes y marca como sincronizados los aceptados. */
    suspend fun sincronizar(tamanoLote: Int = 50): Int {
        var total = 0
        while (true) {
            val lote = movimientoDao.obtenerPendientes(tamanoLote)
            if (lote.isEmpty()) break
            val aceptados = api.enviarMovimientos(lote)
            if (aceptados.isEmpty()) break
            total += movimientoDao.marcarSincronizados(aceptados)
            if (lote.size < tamanoLote) break
        }
        return total
    }

    private fun validarDatos(codigo: String, cantidad: Int) {
        if (codigo.isBlank()) throw InventarioException("El código es obligatorio")
        if (cantidad <= 0) throw InventarioException("La cantidad debe ser mayor a cero")
    }
}
