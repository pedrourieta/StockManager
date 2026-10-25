package mx.edu.utez.stockmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.stockmanager.data.local.entity.TipoMovimiento
import mx.edu.utez.stockmanager.data.local.model.ExistenciaProducto
import mx.edu.utez.stockmanager.data.local.model.MovimientoDetalle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    estado: InventarioUiState,
    form: FormularioState,
    onFormulario: (FormularioState) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    onEditar: (MovimientoDetalle) -> Unit,
    onEliminar: (MovimientoDetalle) -> Unit,
    onSincronizar: () -> Unit,
    onMensajeMostrado: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { snackbar.showSnackbar(it); onMensajeMostrado() }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("StockManager", fontWeight = FontWeight.Bold) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pendientes por sincronizar: ${estado.pendientes}", Modifier.weight(1f))
                    OutlinedButton(onClick = onSincronizar, enabled = estado.pendientes > 0) {
                        Text("Sincronizar")
                    }
                }
            }
            item { Formulario(form, onFormulario, onGuardar, onCancelar) }
            item { Titulo("Existencias") }
            if (estado.existencias.isEmpty()) item { Text("Sin productos registrados") }
            items(estado.existencias, key = { "e" + it.codigo }) { FilaExistencia(it) }
            item { Titulo("Movimientos recientes") }
            items(estado.movimientos, key = { "m" + it.id }) { FilaMovimiento(it, onEditar, onEliminar) }
        }
    }
}

@Composable
private fun Formulario(
    f: FormularioState,
    onCambio: (FormularioState) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val editando = f.editandoId != null
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (editando) "Editar movimiento #${f.editandoId}" else "Registrar movimiento",
            fontWeight = FontWeight.Bold, fontSize = 16.sp)
        OutlinedTextField(f.codigo, { onCambio(f.copy(codigo = it)) }, Modifier.fillMaxWidth(),
            label = { Text("Código del producto") }, singleLine = true, enabled = !editando)
        OutlinedTextField(f.nombre, { onCambio(f.copy(nombre = it)) }, Modifier.fillMaxWidth(),
            label = { Text("Nombre (solo productos nuevos)") }, singleLine = true, enabled = !editando)
        OutlinedTextField(f.cantidad, { onCambio(f.copy(cantidad = it.filter(Char::isDigit))) },
            Modifier.fillMaxWidth(), label = { Text("Cantidad") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TipoMovimiento.entries.forEach { t ->
                RadioButton(selected = f.tipo == t, onClick = { onCambio(f.copy(tipo = t)) })
                Text(if (t == TipoMovimiento.ENTRADA) "Entrada" else "Salida")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onGuardar) { Text(if (editando) "Actualizar" else "Guardar") }
            if (editando) OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
}

@Composable
private fun FilaExistencia(e: ExistenciaProducto) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(e.nombre)
            Text(e.codigo, fontSize = 12.sp)
        }
        Text("${e.existencia} pzas")
    }
}

@Composable
private fun FilaMovimiento(
    m: MovimientoDetalle,
    onEditar: (MovimientoDetalle) -> Unit,
    onEliminar: (MovimientoDetalle) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${if (m.tipo == TipoMovimiento.ENTRADA) "Entrada" else "Salida"}: ${m.cantidad}  ${m.nombre}")
                Text("${m.codigoProducto} · ${formatoFecha.format(Date(m.fecha))}", fontSize = 12.sp)
            }
            if (!m.sincronizado) {
                IconButton(onClick = { onEditar(m) }) { Icon(Icons.Filled.Edit, "Editar") }
                IconButton(onClick = { onEliminar(m) }) { Icon(Icons.Filled.Delete, "Eliminar") }
            }
        }
        HorizontalDivider()
    }
}
