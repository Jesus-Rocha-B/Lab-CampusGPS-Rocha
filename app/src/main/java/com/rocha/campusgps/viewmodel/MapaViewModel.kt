package com.rocha.campusgps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocha.campusgps.data.LugaresRepository
import com.rocha.campusgps.data.UbicacionRepository
import com.rocha.campusgps.model.Lugar
import com.rocha.campusgps.model.LugarConDistancia
import com.rocha.campusgps.model.Posicion
import com.rocha.campusgps.model.TipoError
import com.rocha.campusgps.model.UbicacionUi
import com.rocha.campusgps.util.distanciaMetros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    val categorias: List<String> = listOf("Todos", "Estudio", "Comida", "Eventos", "Servicios", "Deportes")

    private val _categoriaSeleccionada = MutableStateFlow("Todos")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    val lugaresFiltrados: StateFlow<List<LugarConDistancia>> = combine(
        _lugares,
        _categoriaSeleccionada
    ) { lista, cat ->
        if (cat == "Todos") {
            lista
        } else {
            lista.filter { it.lugar.categoria.equals(cat, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _lugares.value)

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    fun buscar() {
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                mensaje = "Este dispositivo no tiene Google Play Services.",
                tipo = TipoError.SIN_PLAY_SERVICES
            )
            return
        }

        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure {
                    _estado.value = UbicacionUi.Error(
                        mensaje = mensajeDe(it),
                        tipo = tipoErrorDe(it)
                    )
                }
        }
    }

    private fun actualizarDistancias(pos: Posicion) {
        val listaConDistancias = lugaresRepo.obtenerLugares().map { lugar ->
            val metros = distanciaMetros(pos, lugar)
            LugarConDistancia(lugar, metros)
        }.sortedBy { it.metros ?: Float.MAX_VALUE }

        _lugares.value = listaConDistancias
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            mensaje = "Sin permiso de ubicación no puedo mostrar dónde estás",
            tipo = TipoError.SIN_PERMISO
        )
    }

    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }

    val masCercano: StateFlow<Lugar?> = lugaresFiltrados.map { lista ->
        lista.firstOrNull { it.metros != null }?.lugar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun mensajeDe(e: Throwable): String = when (e) {
        is SecurityException -> "Falta el permiso de ubicación"
        is IllegalStateException -> e.message ?: "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo"
        else -> e.message ?: "No se pudo obtener la ubicación"
    }

    private fun tipoErrorDe(e: Throwable): TipoError = when (e) {
        is SecurityException -> TipoError.SIN_PERMISO
        is IllegalStateException -> TipoError.GPS_APAGADO
        else -> TipoError.OTRO
    }
}
