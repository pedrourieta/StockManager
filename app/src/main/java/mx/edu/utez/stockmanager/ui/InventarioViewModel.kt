package mx.edu.utez.stockmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.stockmanager.data.local.entity.TipoMovimiento
import mx.edu.utez.stockmanager.data.local.model.ExistenciaProducto
import mx.edu.utez.stockmanager.data.local.model.MovimientoDetalle
import mx.edu.utez.stockmanager.data.repository.InventarioRepository

data class FormularioState(
    val codigo: String = "",
    val nombre: String = "",
    val cantidad: String = "",
    val tipo: TipoMovimiento = TipoMovimiento.ENTRADA,
    val editandoId: Long? = null
)

data class InventarioUiState(
    val existencias: List<ExistenciaProducto> = emptyList(),
    val movimientos: List<MovimientoDetalle> = emptyList(),
    val pendientes: Int = 0,
    val mensaje: String? = null
)

class InventarioViewModel(
    private val repo: InventarioRepository,
    private val hayConexion: () -> Boolean
) : ViewModel() {

    private val formulario = MutableStateFlow(FormularioState())
    val form: StateFlow<FormularioState> = formulario

    private val mensaje = MutableStateFlow<String?>(null)

    val estado: StateFlow<InventarioUiState> = combine(
        repo.existencias, repo.movimientos, repo.pendientes, mensaje
    ) { ex, mov, pend, msg -> InventarioUiState(ex, mov, pend, msg) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventarioUiState())

    fun cambiarFormulario(nuevo: FormularioState) { formulario.value = nuevo }

    fun guardar() = viewModelScope.launch {
        val f = formulario.value
        val cantidad = f.cantidad.toIntOrNull() ?: 0
        runCatching {
            if (f.editandoId == null) repo.registrarMovimiento(f.codigo, f.nombre, f.tipo, cantidad)
            else repo.actualizarMovimiento(f.editandoId, f.tipo, cantidad)
        }.onSuccess {
            mensaje.value = if (f.editandoId == null) "Movimiento guardado" else "Movimiento actualizado"
            formulario.value = FormularioState()
        }.onFailure { mensaje.value = it.message ?: "Error al guardar" }
    }

    fun editar(m: MovimientoDetalle) {
        formulario.value = FormularioState(m.codigoProducto, m.nombre, m.cantidad.toString(), m.tipo, m.id)
    }

    fun cancelarEdicion() { formulario.value = FormularioState() }

    fun eliminar(m: MovimientoDetalle) = viewModelScope.launch {
        runCatching { repo.eliminarMovimiento(m.id) }
            .onSuccess { mensaje.value = "Movimiento eliminado" }
            .onFailure { mensaje.value = it.message ?: "Error al eliminar" }
    }

    /** Envía los pendientes solo si hay internet; si no, se conservan para después. */
    fun sincronizar() = viewModelScope.launch {
        if (!hayConexion()) {
            mensaje.value = "Sin conexión: los datos se enviarán más tarde"
            return@launch
        }
        runCatching { repo.sincronizar() }
            .onSuccess { mensaje.value = "$it movimientos enviados al servidor" }
            .onFailure { mensaje.value = "No fue posible sincronizar" }
    }

    fun mensajeMostrado() { mensaje.value = null }
}
