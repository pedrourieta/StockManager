package mx.edu.utez.stockmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import mx.edu.utez.stockmanager.ui.InventarioScreen
import mx.edu.utez.stockmanager.ui.InventarioViewModel
import mx.edu.utez.stockmanager.ui.theme.StockManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as StockManagerApp
        val fabrica = viewModelFactory {
            initializer { InventarioViewModel(app.repositorio, app::hayConexion) }
        }
        setContent {
            StockManagerTheme {
                val vm: InventarioViewModel = viewModel(factory = fabrica)
                val estado = vm.estado.collectAsStateWithLifecycle().value
                val form = vm.form.collectAsStateWithLifecycle().value
                InventarioScreen(
                    estado = estado,
                    form = form,
                    onFormulario = vm::cambiarFormulario,
                    onGuardar = { vm.guardar() },
                    onCancelar = vm::cancelarEdicion,
                    onEditar = vm::editar,
                    onEliminar = { vm.eliminar(it) },
                    onSincronizar = { vm.sincronizar() },
                    onMensajeMostrado = vm::mensajeMostrado
                )
            }
        }
    }
}
