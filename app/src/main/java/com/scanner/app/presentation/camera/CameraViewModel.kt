import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraPreviewViewModel : ViewModel() {
    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    // Adiciona um StateFlow para controlar a lente da câmera
    private val _cameraSelector = MutableStateFlow(DEFAULT_BACK_CAMERA) // Inicia com a câmera traseira
    val cameraSelector: StateFlow<CameraSelector> = _cameraSelector

    private var cameraProvider: ProcessCameraProvider? = null
    private var currentLifecycleOwner: LifecycleOwner? = null

    // O caso de uso Preview é criado uma vez
    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        cameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        currentLifecycleOwner = lifecycleOwner // Guarda o lifecycleOwner

        // Lança uma coroutine para observar _cameraSelector e re-vincular a câmera
        viewModelScope.launch {
            _cameraSelector.collect { selectedCamera ->
                rebindCamera(selectedCamera)
            }
        }

        // awaitCancellation() mantém a coroutine viva até ser cancelada
        try { awaitCancellation() } finally {
            cameraProvider?.unbindAll()
            cameraProvider = null
            currentLifecycleOwner = null
        }
    }

    // Nova função para re-vincular a câmera com a lente selecionada
    private fun rebindCamera(selectedCamera: CameraSelector) {
        val provider = cameraProvider ?: return
        val lifecycle = currentLifecycleOwner ?: return

        // Desvincula todos os casos de uso para que possamos vincular com a nova lente
        provider.unbindAll()

        try {
            provider.bindToLifecycle(
                lifecycle,
                selectedCamera, // Usa a lente selecionada
                cameraPreviewUseCase
            )
        } catch (exc: Exception) {
            // Trate exceções aqui, como se a câmera já estivesse em uso
            // Log.e("CameraViewModel", "Falha ao vincular a câmera: ${exc.message}")
        }
    }

    // Função para alternar a câmera
    fun switchCamera() {
        _cameraSelector.update { currentSelector ->
            if (currentSelector == DEFAULT_BACK_CAMERA) {
                DEFAULT_FRONT_CAMERA
            } else {
                DEFAULT_BACK_CAMERA
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // O unbindAll já é feito no finally de bindToCamera, mas aqui garante limpeza
        cameraProvider?.unbindAll()
    }
}