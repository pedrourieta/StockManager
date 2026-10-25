package mx.edu.utez.stockmanager.data.remote

import kotlinx.coroutines.delay
import mx.edu.utez.stockmanager.data.local.entity.Movimiento

/** Contrato con el servidor central de la empresa. */
interface ServidorCentralApi {
    /** Envía un lote de movimientos y devuelve los id que el servidor aceptó. */
    suspend fun enviarMovimientos(movimientos: List<Movimiento>): List<Long>
}

/**
 * Servidor simulado para la práctica. Para producción se sustituye por una
 * implementación con Retrofit (POST /api/movimientos) sin modificar el resto del código.
 */
class ServidorCentralSimulado(private val latenciaMs: Long = 600) : ServidorCentralApi {
    val recibidos = mutableListOf<Movimiento>()

    override suspend fun enviarMovimientos(movimientos: List<Movimiento>): List<Long> {
        delay(latenciaMs)
        recibidos += movimientos
        return movimientos.map { it.id }
    }
}
