package cl.duoc.appmovil2.viewmodel

import androidx.lifecycle.ViewModel

import cl.duoc.appmovil2.navegation.AppScreens
import cl.duoc.appmovil2.navegation.NavegationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _navegationEvents = MutableSharedFlow<NavegationEvent>()

    val navegationEvents : SharedFlow<NavegationEvent> = _navegationEvents.asSharedFlow()

    fun navegateTo(screens: AppScreens) {
        CoroutineScope(Dispatchers.Main).launch {
            _navegationEvents.emit(NavegationEvent.NavegateTo(route = screens))
        }
    }

    fun navegateBack(){
        CoroutineScope(Dispatchers.Main).launch {
            _navegationEvents.emit(NavegationEvent.PopBackStack)
        }
    }

    fun navegateUp(){
        CoroutineScope(Dispatchers.Main).launch {
            _navegationEvents.emit(NavegationEvent.NavegateUp)
        }
    }

}