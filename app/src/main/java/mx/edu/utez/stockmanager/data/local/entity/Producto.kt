package mx.edu.utez.stockmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catálogo de productos. El código (SKU o código de barras) es la llave primaria. */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val codigo: String,
    val nombre: String
)
