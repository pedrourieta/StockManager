package mx.edu.utez.stockmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.stockmanager.data.local.entity.Producto
import mx.edu.utez.stockmanager.data.local.model.ExistenciaProducto

@Dao
interface ProductoDao {

    // CREATE: si el código ya existe no se duplica (IGNORE devuelve -1)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(producto: Producto): Long

    // READ
    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Producto?

    @Query("SELECT * FROM productos ORDER BY nombre")
    fun observarTodos(): Flow<List<Producto>>

    // UPDATE
    @Update
    suspend fun actualizar(producto: Producto): Int

    // DELETE (borra en cascada sus movimientos)
    @Delete
    suspend fun eliminar(producto: Producto): Int

    /**
     * Existencia calculada directamente en SQLite con SUM/CASE,
     * en lugar de cargar todos los movimientos en memoria.
     */
    @Query(
        """
        SELECT p.codigo, p.nombre,
               COALESCE(SUM(CASE WHEN m.tipo = 'ENTRADA' THEN m.cantidad
                                 ELSE -m.cantidad END), 0) AS existencia
        FROM productos p
        LEFT JOIN movimientos m ON m.codigoProducto = p.codigo
        GROUP BY p.codigo, p.nombre
        ORDER BY p.nombre
        """
    )
    fun observarExistencias(): Flow<List<ExistenciaProducto>>
}
