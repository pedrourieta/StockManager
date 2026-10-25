package mx.edu.utez.stockmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.stockmanager.data.local.entity.Movimiento
import mx.edu.utez.stockmanager.data.local.model.MovimientoDetalle

@Dao
interface MovimientoDao {

    // CREATE
    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    @Insert
    suspend fun insertarVarios(movimientos: List<Movimiento>): List<Long>

    // READ
    @Query("SELECT * FROM movimientos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Movimiento?

    /** Lista reciente con LIMIT para no cargar todo el historial en pantalla. */
    @Query(
        """
        SELECT m.id, m.codigoProducto, p.nombre, m.tipo, m.cantidad, m.fecha, m.sincronizado
        FROM movimientos m
        INNER JOIN productos p ON p.codigo = m.codigoProducto
        ORDER BY m.fecha DESC
        LIMIT :limite
        """
    )
    fun observarRecientes(limite: Int = 100): Flow<List<MovimientoDetalle>>

    /** Existencia actual de un producto (usa el índice de codigoProducto). */
    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN tipo = 'ENTRADA' THEN cantidad ELSE -cantidad END), 0)
        FROM movimientos WHERE codigoProducto = :codigo
        """
    )
    suspend fun existenciaDe(codigo: String): Int

    /** Lote de pendientes de sincronizar (usa el índice de sincronizado). */
    @Query("SELECT * FROM movimientos WHERE sincronizado = 0 ORDER BY fecha LIMIT :lote")
    suspend fun obtenerPendientes(lote: Int): List<Movimiento>

    @Query("SELECT COUNT(*) FROM movimientos WHERE sincronizado = 0")
    fun observarTotalPendientes(): Flow<Int>

    // UPDATE
    @Update
    suspend fun actualizar(movimiento: Movimiento): Int

    /** Una sola sentencia UPDATE para todo el lote enviado al servidor. */
    @Query("UPDATE movimientos SET sincronizado = 1 WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<Long>): Int

    // DELETE
    @Delete
    suspend fun eliminar(movimiento: Movimiento): Int
}
