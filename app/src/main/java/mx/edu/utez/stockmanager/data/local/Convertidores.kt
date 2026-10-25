package mx.edu.utez.stockmanager.data.local

import androidx.room.TypeConverter
import mx.edu.utez.stockmanager.data.local.entity.TipoMovimiento

class Convertidores {
    @TypeConverter
    fun tipoATexto(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun textoATipo(valor: String): TipoMovimiento = TipoMovimiento.valueOf(valor)
}
